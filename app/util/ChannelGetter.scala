package util

import java.util.Date
import play.api.libs.ws._
import play.api.Play.current
import scala.concurrent.Future
import scala.concurrent.Future._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.xml.XML

package channel {

  abstract class ChannelGetter[C <: model.Channel] {

    /* 抽象メンバー */
    // ニュースのURL
    def endPoint: String

    // ニュースの名前を定義する
    def entryName: String

    // キャッシュの更新方法を実装する
    def updateCache(implicit reader: XMLReader[C]): Future[Unit] =
      requestChannel(saveCache)

    // ニュースを一度読み込んだらキャッシュしておく。
    // lastBuildDateの変更があればキャッシュを更新する
    @volatile private var cache: Option[C] = None

    /*
    Http通信の失敗，パース時になんらかのエラーが出る可能性があるので
    データはOptionに包む。
    */
    def get(implicit reader: XMLReader[C]): Future[Option[C]] = cache match {
      case Some(_) => Future(cache)
      case None => requestChannel { channel =>
          saveCache(channel)
          Some(channel)
        }
    }

    /*
    Httpでニュースを取得し，CのXMLReaderでCにパース
    その後関数で自由に操作できる。
    */
    private def requestChannel[A](afterGet: C => A)(implicit reader: XMLReader[C]): Future[A] =
      for {
        xmlString <- WS.url(endPoint + entryName).get()
        channel <- Future(reader.read(xmlString.body))
      }
      yield afterGet(channel)

    private def saveCache(channel: C) {
      cache = Some(channel)
    }
  }

  package livedoor {
    import model._
    abstract class LivedoorGetter extends ChannelGetter[livedoor.Channel] {
      def endPoint = "http://news.livedoor.com/topics/rss/"
    }
    object Top extends LivedoorGetter { def entryName = "top.xml" }
    object Dom extends LivedoorGetter { def entryName = "dom.xml" }
    object Int extends LivedoorGetter { def entryName = "int.xml" }
    object Eco extends LivedoorGetter { def entryName = "eco.xml" }
  }

}
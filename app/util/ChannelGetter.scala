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

    // キャッシュの更新方法
    // 数分おきに更新するだけよりもいい方法があれば
    // 子クラスでオーバーライドして実装する
    def updateCache(xmlString: String, reader: XMLReader[C]): Unit =
      for {
        channel <- cache
        latestNews <- channel.newsCache.headOption
      }
      yield {
        val xml = XML.loadString(xmlString) \ "channel"

      }

    // キャッシュの更新を許可する間隔
    val updateDuration = 15 * 60 * 1000

    @volatile protected var lastUpdateDate: Option[Date] = None
    @volatile protected var cache: Option[C] = None

    /*
    Http通信の失敗，パース時になんらかのエラーが出る可能性があるので
    データはOptionに包む。
    */
    def get(implicit reader: XMLReader[C]): Future[Option[C]] = cache match {
      case Some(_) =>
        checkChannelUpdated
        Future(cache)
      case None => requestChannel { channel =>
          saveCache(channel)
          Some(channel)
        } recover {
          case e: Exception => None
        }
    }

    /*
    非同期で確認してキャッシュの更新作業を行うので，ブロックしない
    */
    private def checkChannelUpdated(implicit reader: XMLReader[C]) = Future {
      val current = JavaDate.getCurrent
      val needUpdate = lastUpdateDate map {date =>
        current.getTime - date.getTime > updateDuration
      }
      if (needUpdate.getOrElse(true)) {
        lastUpdateDate = Some(current)
        runUpdateCache(reader)
      }
    }

    /*
    HttpでXMLを取得し，String型でResponse#bodyを取り出す。
    その後，引数に渡される関数でmapして返す。
    */
    private def requestChannelXML[A](afterGet: String => A): Future[A] =
      WS.url(endPoint + entryName).get().map(x => afterGet(x.body))

    /*
    requestChannelXML
    その後関数で自由に操作できる。
    */
    private def requestChannel[A](afterGet: C => A)(implicit reader: XMLReader[C]) =
      for {
        channel <- requestChannelXML(reader.read)
      }
      yield afterGet(channel)

    private def runUpdateCache(reader: XMLReader[C]): Future[Unit] =
      requestChannelXML(xml => updateCache(xml, reader))

    private def saveCache(channel: C) {
      cache = Some(channel)
    }
  }
}

object Channel {
  import model._
  import util.channel.ChannelGetter

  abstract class LivedoorGetter extends ChannelGetter[livedoor.Channel] {
    def endPoint = "http://news.livedoor.com/topics/rss/"
  }
  val Livedoor = Map(
    "top" -> new LivedoorGetter { def entryName = "top.xml" },
    "dom" -> new LivedoorGetter { def entryName = "dom.xml" }
    // object Int extends LivedoorGetter { def entryName = "int.xml" }
    // object Eco extends LivedoorGetter { def entryName = "eco.xml" }
  )
}


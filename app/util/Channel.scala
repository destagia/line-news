package util

import java.util.Date
import play.api.libs.ws._
import play.api.Play.current
import scala.concurrent.Future
import scala.concurrent.Future._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.xml.XML

package model {
  case class Channel (
    val lang: Language,
    val title: String,
    val link: String,
    val generator: String,
    val description: String,
    val lastBuildDate: Date,
    val items: List[News]
  )
  object Channel {
    def parse(s: String): Future[Channel] = for {
      xmlChannel <- Future(XML.loadString(s) \ "channel")
      xmlItems <- Future((xmlChannel \ "item").toSeq)
      items <- sequence(xmlItems.map(x => News.parse(x.toString)))
    }
    yield
    {
      val lang = Language.parse((xmlChannel \ "language").text)
      val title = (xmlChannel \ "title").text
      val link = (xmlChannel \ "link").text
      val generator = (xmlChannel \ "generator").text
      val description = (xmlChannel \ "description").text
      val lastBuildDate = JavaDate.parse((xmlChannel \ "lastBuildDate").text)
      Channel(lang, title, link, generator, description, lastBuildDate, items.toList)
    }
  }
}

package channel {
  import util.model._

  abstract class ChannelGetter {

    // ニュースの名前を定義する
    def entryName: String

    // ニュースを一度読み込んだらキャッシュしておく。
    // lastBuildDateの変更があればキャッシュを更新する
    @volatile private var cache: Option[Channel] = None

    /*
    Http通信の失敗，パース時になんらかのエラーが出る可能性があるので
    データはOptionに包む。
    */
    def get: Future[Option[Channel]] = cache match {
      case Some(_) => Future(cache)
      case None =>
        for {
          xmlString <- WS.url("http://news.livedoor.com/topics/rss/" + entryName).get()
          channel <- Channel.parse(xmlString.body)
        }
        yield saveCache(Some(channel))
    }

    private def saveCache(channel: Option[Channel]): Option[Channel] = {
      cache = channel
      cache
    }
  }

  object Top extends ChannelGetter { def entryName = "top.xml" }

}
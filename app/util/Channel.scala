package util

import java.util.Date
import play.api.libs.ws._
import play.api.Play.current
import scala.concurrent.Future
import scala.concurrent.Future._
import scala.concurrent.ExecutionContext.Implicits.global

package channel {
  import scala.xml.XML
  import util.News

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

   private abstract class ChannelGetter {
    def entryName: String

    @volatile var cache: Option[Channel] = None

    /*
    Http通信の失敗，パース時になんらかのエラーが出る可能性があるので
    データはOptionに包む。
    */
    def getChannel:Future[Option[Channel]] = cache match {
      case Some(_) => Future(cache)
      case None =>
        for {
          xmlString <- WS.url("http://news.livedoor.com/topics/rss/" + entryName).get()
          channel <- Channel.parse(xmlString.body)
        }
        yield Some(channel)

    }
  }

}
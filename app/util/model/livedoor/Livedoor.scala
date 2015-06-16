package util.model.livedoor

import util.XMLReader
import util.model
import util.model.Language
import java.util.Date
import scala.xml.XML
import util.JavaDate
import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

case class Channel (
  lang: Language,
  title: String,
  link: String,
  generator: String,
  description: String,
  lastBuildDate: Date,
  genre: model.Genre
) extends model.Channel {
  def toHTML = {
    title + "\n" + newsCache.mkString(", ")
  }

  def newsRead(s: String): model.News = {
    val news = XML.loadString(s)
    val title = (news \ "title").text
    val link = (news \ "link").text
    val description_ = (news \ "description").text
    val description =
      if (genre == model.映画)
          description_
            .replace("-s.jpg", ".jpg")
            .replace("<a href", "<p><a href")
            .replace("</a>", "</a></p>")
            .replace("全文", "全文を読む")
      else description_
    val date = util.JavaDate.parse((news \ "pubDate").text)
    val guid = (news \ "guid").text
    News(title, link, description, date, guid, this)
  }
}
object Channel {
  implicit val reader = new XMLReader[Channel]  {
    def read(genre: model.Genre)(s: String): Channel = {
      val xmlChannel = XML.loadString(s) \ "channel"
      val lang = Language.parse((xmlChannel \ "language").text)
      val title = (xmlChannel \ "title").text
      val link = (xmlChannel \ "link").text
      val generator = (xmlChannel \ "generator").text
      val description = (xmlChannel \ "description").text
      val dateString = (xmlChannel \ "lastBuildDate").text
      val lastBuildDate =
        if (dateString != "")
          JavaDate.parse(dateString)
        else
          new java.util.Date()
      val channel = Channel(lang, title, link, generator, description, lastBuildDate, genre)
      (xmlChannel \ "item").foreach {
        n => channel.newsCache += channel.newsRead(n.toString())
      }
      channel
    }
  }
}

case class News (
  title: String,
  link: String,
  description: String,
  date: Date,
  guid: String,
  channel: model.Channel
) extends model.News {
  def contentString =
      title + "\n" + (try {
        XML.loadString("<xml>" + description + "</xml>").text
      }
      catch {
        case e: Exception => description
      })

}

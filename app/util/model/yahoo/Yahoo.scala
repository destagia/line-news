package util.model.yahoo

import util.XMLReader
import util.model
import util.model.Language
import java.util.Date
import scala.xml.XML
import util.JavaDate

case class Channel (
  val lang: Language,
  val title: String,
  val link: String,
  val description: String,
  val pubDate: Date
) extends model.Channel {
  def toHTML = {
    title + "\n" + newsCache.mkString(", ")
  }

  def newsRead(s: String): model.News = {
    val news = XML.loadString(s)
    val title = (news \ "title").text
    val link = (news \ "link").text
    val date = util.JavaDate.parse((news \ "pubDate").text)
    val guid = (news \ "guid").text
    News(title, link, date, guid)
  }
}
object Channel {
  implicit val reader = new XMLReader[Channel]  {
    def read(s: String): Channel = {
      val xmlChannel = XML.loadString(s) \ "channel"
      val lang = Language.parse((xmlChannel \ "language").text)
      val title = (xmlChannel \ "title").text
      val link = (xmlChannel \ "link").text
      val description = (xmlChannel \ "description").text
      val pubDate = JavaDate.parse((xmlChannel \ "pubDate").text)
      val channel = Channel(lang, title, link, description, pubDate)
      (xmlChannel \ "item").foreach {
        n => channel.newsCache += channel.newsRead(n.toString)
      }
      channel
    }
  }
}

case class News (
  title: String,
  link: String,
  date: Date,
  guid: String
) extends model.News {
  def contentString = title
}
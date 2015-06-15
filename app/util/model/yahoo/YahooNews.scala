package util.model.yahoo.news

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
  val lastBuildDate: Date
) extends model.Channel {
  def toHTML = {
    title + "\n" + newsCache.mkString(", ")
  }

  def newsRead(s: String): model.News = {
    val news  = XML.loadString(s)
    val title = (news \ "title").text
    val link  = (news \ "link").text
    val date  = util.JavaDate.parse((news \ "pubDate").text)
    News(title, link, date)
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
      val lastBuildDate = JavaDate.parse((xmlChannel \ "lastBuildDate").text)
      val channel = Channel(lang, title, link, description, lastBuildDate)
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
  date: Date
) extends model.News {
  def guid = link
  def contentString = title
}
package util.model.livedoor

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
  val generator: String,
  val description: String,
  val lastBuildDate: Date
) extends model.Channel {
  def toHTML = {
    title + "\n" + newsCache.mkString(", ")
  }

  def newsRead(s: String): model.News = {
    val news = XML.loadString(s)
    val title = (news \ "title").text
    val link = (news \ "link").text
    val description = (news \ "description").text
    val mobile = (news \ "mobile").text.toInt
    val date = util.JavaDate.parse((news \ "pubDate").text)
    val guid = (news \ "guid").text
    News(title, link, description, mobile, date, guid)
  }
}
object Channel {
  implicit val reader = new XMLReader[Channel]  {
    def read(s: String): Channel = {
      val xmlChannel = XML.loadString(s) \ "channel"

      val lang = Language.parse((xmlChannel \ "language").text)
      val title = (xmlChannel \ "title").text
      val link = (xmlChannel \ "link").text
      val generator = (xmlChannel \ "generator").text
      val description = (xmlChannel \ "description").text
      val lastBuildDate = JavaDate.parse((xmlChannel \ "lastBuildDate").text)
      val channel = Channel(lang, title, link, generator, description, lastBuildDate)

      val xmlItems = (xmlChannel \ "item").toSeq
      xmlItems.foreach { x =>
         channel.newsCache += channel.newsRead(x.toString)
      }
      channel
    }
  }
}

case class News (
  title: String,
  link: String,
  description: String,
  mobile: Int,
  date: Date,
  guid: String
) extends model.News {
  def getInfo = model.NewsInfo(title, date, guid)
  def contentString = title + "\n" + description
}

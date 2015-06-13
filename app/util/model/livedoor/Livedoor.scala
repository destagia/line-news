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
  val lastBuildDate: Date,
  val items: List[News]
)
object Channel {
  implicit val reader = new XMLReader[Channel]  {
    def read(s: String): Channel = {
      val xmlChannel = XML.loadString(s) \ "channel"
      val xmlItems = (xmlChannel \ "item").toSeq
      val items = xmlItems.map(x => News.reader.read(x.toString))
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

case class News (
  title: String,
  link: String,
  description: String,
  mobile: Int,
  date: Date,
  guid: String
)
object News {
  implicit val reader = new XMLReader[News] {
    def read(s: String): News = {
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
}
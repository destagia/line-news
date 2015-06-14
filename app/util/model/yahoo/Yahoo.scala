// package util.model.yahoo

// import util.XMLReader
// import util.model
// import util.model.Language
// import java.util.Date
// import scala.xml.XML
// import util.JavaDate

// case class Channel (
//   val lang: Language,
//   val title: String,
//   val link: String,
//   val description: String,
//   val pubDate: Date,
//   val items: List[News]
// ) extends model.Channel {
//   def toHTML = {
//     title + "\n" + items.mkString(", ")
//   }
// }
// object Channel {
//   implicit val reader = new XMLReader[Channel]  {
//     def read(s: String): Channel = {
//       val xmlChannel = XML.loadString(s) \ "channel"
//       val xmlItems = (xmlChannel \ "item").toSeq
//       val items = xmlItems.map(x => News.reader.read(x.toString))
//       val lang = Language.parse((xmlChannel \ "language").text)
//       val title = (xmlChannel \ "title").text
//       val link = (xmlChannel \ "link").text
//       val description = (xmlChannel \ "description").text
//       val pubDate = JavaDate.parse((xmlChannel \ "pubDate").text)
//       Channel(lang, title, link, description, pubDate, items.toList)
//     }
//   }
// }

// case class News (
//   title: String,
//   link: String,
//   description: String,
//   date: Date,
//   guid: String
// )
// object News {
//   val reader = new XMLReader[News] {
//     def read(s: String): News = {
//       val news = XML.loadString(s)
//       val title = (news \ "title").text
//       val link = (news \ "link").text
//       val description = (news \ "description").text
//       val date = util.JavaDate.parse((news \ "pubDate").text)
//       val guid = (news \ "guid").text
//       News(title, link, description, date, guid)
//     }
//   }
// }
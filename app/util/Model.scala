package util.model

import java.util.{Date, Calendar}
import java.text.SimpleDateFormat
import scala.xml.XML
import play.api.libs.ws._
import play.api.Play.current
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

sealed trait Language
object Language {
  def parse(s: String) = s match {
    case "ja" => Ja
    case "en" => En
    case "es" => Es
    case "zh" => Zh
    case "ko" => Ko
    case _ => Ja
  }
}
case object Ja extends Language
case object En extends Language
case object Es extends Language
case object Zh extends Language
case object Ko extends Language

case class News (
  title: String,
  link: String,
  description: String,
  mobile: Int,
  date: Date,
  guid: String
)
object News {
  def parse(s: String): Future[News] = Future {
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
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

trait Channel[A] {
}

trait News[A] {
}

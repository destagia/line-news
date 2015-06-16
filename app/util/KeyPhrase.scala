package util

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.ws.WS
import play.api.Play.current
import scala.xml.XML

package model.keyphrase {
  case class Result (
    keyPhrase: String,
    score: Int
  )
}

/*
Yahoo APIを利用して文章の中から重要な語句を抽出する
関連記事の算出に利用する
*/
object KeyPhrase {
  private val endPoint = "http://jlp.yahooapis.jp/KeyphraseService/V1/extract"
  private val appid = "dj0zaiZpPUZ5TUZwUjd4RUhkcyZzPWNvbnN1bWVyc2VjcmV0Jng9NmY-"
  import model._

  def get(sentence: String):Future[List[keyphrase.Result]] =
    WS.url(endPoint).withQueryString("appid" -> appid, "sentence" -> sentence).get()
    .map { res =>
      (XML.loadString(res.body) \ "Result").toList.map { node =>
        keyphrase.Result((node \ "Keyphrase").text, (node \ "Score").text.toInt)
      }
    } recover {
      case e: Exception => Nil
    }

}
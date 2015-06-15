package util

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import java.net.URL
import collection.JavaConversions._

object Hatena {
  def getSimilar(words: Array[String]): Future[List[String]] =
    Future(hatena.SimilarWord.get(words).toList)
}
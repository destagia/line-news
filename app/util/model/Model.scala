package util.model

import java.util.Date
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.xml.{XML, NodeSeq}
import scala.collection.mutable.{ListBuffer => MList}

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

trait Channel {
  // ニュースのキャッシュ
  val newsCache: MList[News] = MList[News]()

  def toHTML: String

  /*
  ニュースをパースして，さらに関連記事を見つける必要があるので
  戻り値がFutureで包まれている
  */
  def newsRead(xmlString: String): News

  def getAllNews: List[News] = newsCache.toList

  def updateNewsCache(xml: NodeSeq) = {

    def newNews(guid: String, nodes: NodeSeq): List[News] = {
      nodes.headOption match {
        case None => Nil
        case Some(node) =>
          if ((node \ "guid").text != guid) {
            val fNews = newsRead(node.toString)
            if (nodes.size > 2)
              fNews :: newNews(guid, nodes.tail)
            else
              List(fNews)
          }
          else
            Nil
      }
    }

    lazy val nodes = { xml \ "item" }
    // 先頭のguidから新しい分だけをキャッシュに追加する仕組み
    newsCache.headOption match {
      case None =>
      case Some(news) =>
        newNews(news.guid, nodes).foreach(_ +=: newsCache)
    }
  }
}

trait News {
  /*
  関連記事を算出する際，このニュースの検索対象となる文章
  */
  def contentString: String
  def title: String
  def link: String
  def date: Date
  def guid: String

  lazy val id: Int = util.ID.getUnique

  val keyPhrase: Future[List[keyphrase.Result]] =
    util.KeyPhrase.get(contentString)

  lazy val similarKeys = for {
      keys <- keyPhrase.map(_.take(5).map(_.keyPhrase))
      similars <- util.Hatena.getSimilar(keys.toArray)
    }
    yield keys ++ similars

  lazy val relatives = {
    for {
      keys <- similarKeys
      relative <- util.Channel.searchRelativeNews(this, keys)
    }
    yield
      relative
  }
}

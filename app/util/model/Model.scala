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

sealed trait Genre
case object トップ extends Genre
case object 国内 extends Genre
case object 海外 extends Genre
case object 経済 extends Genre
case object 芸能 extends Genre
case object スポーツ extends Genre
case object 映画 extends Genre
case object グルメ extends Genre
case object 女子 extends Genre
case object トレンド extends Genre

case class ChannelSet (channel: Channel, genre: Genre)

trait Channel {
  // ニュースのキャッシュ
  val newsCache: MList[News] = MList[News]()
  @volatile var checking: Boolean = false

  def toHTML: String
  def genre: Genre

  /*
  ニュースをパースして，さらに関連記事を見つける必要があるので
  戻り値がFutureで包まれている
  */
  def newsRead(xmlString: String): News

  def getAllNews: List[News] = newsCache.toList

  def updateNewsCache(xml: NodeSeq) = if (!checking) {
    checking = true
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
          else Nil
      }
    }
    lazy val nodes = { xml \ "item" }
    // 先頭のguidから新しい分だけをキャッシュに追加する仕組み
    newsCache.headOption map { news =>
      val latest = newNews(news.guid, nodes)
      latest.reverse.foreach(_ +=: newsCache)
      newsCache.trimEnd(latest.size)
    }
    checking = false
  }
}

trait News {
  /*
  関連記事を算出する際，このニュースの検索対象となる文章
  */
  def contentString: String

  /*
  ニュースの実装必須項目
  */
  def title: String
  def link: String
  def date: Date
  def guid: String
  def channel: Channel

  val id: Int = util.ID.getUnique
  val keyPhrase: Future[List[keyphrase.Result]] =
    util.KeyPhrase.get(contentString)

  lazy val similarKeys = for {
      keys <- keyPhrase.map(_.map(_.keyPhrase))
      key5 = keys.take(5)
      similars <- util.Hatena.getSimilar(key5.toArray)
    }
    yield key5 ++ similars ++ keys.drop(5)

  lazy val relatives = for {
      keys <- similarKeys
      relative <- util.Channel.searchRelativeNews(this, keys)
    }
    yield relative
}

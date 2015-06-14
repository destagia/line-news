package util.model

import java.util.Date
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
  @volatile var latestNewsGuid: Option[String] = None

  def toHTML: String
  // def getAllNews: List[NewsInfo]

  def newsRead(xmlString: String): News

  def updateNewsCache(xmlString: String) {
    val xml = XML.loadString(xmlString) \ "channel"
    val nodes = xml \ "item"

    def run(guid: String, nodes: NodeSeq): Unit =
      for(node <- nodes.headOption) yield
        if ((node \ "guid").text != guid) {
          newsCache += newsRead(node.toString)
          if (nodes.size > 2)
            run(guid, nodes.tail)
        }

    for(head <- newsCache.headOption)
      yield run(head.getInfo.guid, nodes)
  }
}

/*
Webサイトに関わらず，必ず持っているはずの基本情報
*/
case class NewsInfo (
  title: String,
  date: Date,
  guid: String
)

trait News {
  def getInfo: NewsInfo

  /*
  関連記事を算出する際，このニュースの検索対象となる文章
  */
  def contentString: String
}

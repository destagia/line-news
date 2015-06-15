package util

import java.util.Date
import play.api.libs.ws._
import play.api.Play.current
import scala.concurrent.Future
import scala.concurrent.Future._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.xml.XML

package channel {

  abstract class ChannelGetter[C <: model.Channel] {

    /* 抽象メンバー */
    // ニュースのURL
    def endPoint: String

    // ニュースの名前を定義する
    def entryName: String

    // キャッシュの更新を許可する間隔
    val updateDuration = 15 * 60 * 1000

    @volatile protected var lastUpdateDate: Option[Date] = None
    @volatile protected var cache: Option[C] = None

    /*
    Http通信の失敗，パース時になんらかのエラーが出る可能性があるので
    データはOptionに包む。
    */
    def get(implicit reader: XMLReader[C]): Future[Option[C]] = cache match {
      case Some(_) =>
        checkChannelUpdated
        Future(cache)
      case None => requestChannel { channel =>
          saveCache(channel)
          Some(channel)
        } recover {
          case e: Exception => None
        }
    }

    /*
    非同期で確認してキャッシュの更新作業を行うので，ブロックしない
    */
    private def checkChannelUpdated(implicit reader: XMLReader[C]) = Future {
      val current = JavaDate.getCurrent
      val needUpdate = lastUpdateDate map {date =>
        current.getTime - date.getTime > updateDuration
      }
      if (needUpdate.getOrElse(true)) {
        lastUpdateDate = Some(current)
        runUpdateCache(reader)
      }
    }

    /*
    cacheからChannelを取り出して，util.model.Channel#updateNewsCache
    を実行する。
    */
    private def updateCache(xmlString: String, reader: XMLReader[C]): Unit =
      for {
        channel <- cache
      }
      yield
        channel.updateNewsCache(XML.loadString(xmlString) \ "channel")

    /*
    HttpでXMLを取得し，String型でResponse#bodyを取り出す。
    その後，引数に渡される関数でmapして返す。
    */
    private def requestChannelXML[A](afterGet: String => A): Future[A] =
      WS.url(endPoint + entryName).get().map(x => afterGet(x.body))

    /*
    requestChannelXML
    その後関数で自由に操作できる。
    */
    private def requestChannel[A](afterGet: C => A)(implicit reader: XMLReader[C]) =
      for {
        channel <- requestChannelXML(reader.read)
      }
      yield afterGet(channel)

    private def runUpdateCache(reader: XMLReader[C]): Future[Unit] =
      requestChannelXML(xml => updateCache(xml, reader))

    private def saveCache(channel: C) {
      cache = Some(channel)
    }
  }
}

object Channel {
  import model._
  import util.channel.ChannelGetter

  type RelativeNews = (News, News, News)
  def searchRelativeNews(selfId: Int, keys: List[keyphrase.Result]): Future[RelativeNews] =
    for {
      news <- util.Channel.getAllChannelNews
    }
    yield {
      println(keys)
      val res = keys.foldRight(List[News]()) {(a, b) =>
        news.filter { n =>
          n.contentString.contains(a.keyPhrase) &&
          n.id != selfId
        } ++ b
      }

      (res(0), res(1), res(2))
    }

  def getAllNewsFromChannel[C <: model.Channel]
    (m: Map[String, ChannelGetter[C]])(implicit r: XMLReader[C]) =
      m.foldRight(Future(List[model.News]())){(a, b) =>
        for {
          x <- a._2.get
          y <- b
        }
        yield {
          x.map(_.getAllNews).getOrElse(Nil) ++ y
        }
      }

  def getAllChannelNews: Future[List[model.News]] = for {
    l <- getAllNewsFromChannel(Livedoor)
    y <- getAllNewsFromChannel(Yahoo)
  }
  yield l ++ y


  abstract class LivedoorGetter extends ChannelGetter[livedoor.Channel] {
    def endPoint = "http://news.livedoor.com/topics/rss/"
  }
  val Livedoor = Map(
    "top"     -> new LivedoorGetter { def entryName = "top.xml" },
    "dom"     -> new LivedoorGetter { def entryName = "dom.xml" },
    "int"     -> new LivedoorGetter { def entryName = "int.xml" },
    "eco"     -> new LivedoorGetter { def entryName = "eco.xml" },
    "ent"     -> new LivedoorGetter { def entryName = "ent.xml" },
    "spo"     -> new LivedoorGetter { def entryName = "spo.xml" },
    "52"      -> new LivedoorGetter { def entryName = "52.xml" },
    "gourmet" -> new LivedoorGetter { def entryName = "gourmet.xml" },
    "love"    -> new LivedoorGetter { def entryName = "love.xml" },
    "trend"   -> new LivedoorGetter { def entryName = "trend.xml" }
  )

  abstract class YahooGetter extends ChannelGetter[yahoo.Channel] {
    def endPoint = "http://rss.dailynews.yahoo.co.jp/fc/"
  }
  val Yahoo = Map(
    "top"           -> new YahooGetter { def entryName = "rss.xml" },
    "domestic"      -> new YahooGetter { def entryName = "domestic/rss.xml" },
    "world"         -> new YahooGetter { def entryName = "world/rss.xml" },
    "entertainment" -> new YahooGetter { def entryName = "entertainment/rss.xml" },
    "sports"        -> new YahooGetter { def entryName = "sports/rss.xml" },
    "computer"      -> new YahooGetter { def entryName = "computer/rss.xml" },
    "local"         -> new YahooGetter { def entryName = "local/rss.xml" },
    "economy"       -> new YahooGetter { def entryName = "economy/rss.xml" },
    "science"       -> new YahooGetter { def entryName = "science/rss.xml" }
  )
}


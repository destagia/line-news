/*
すべてのチェンネルに関する操作を行う。
Channelオブジェクトに実際のニュースを取得するためのChannelGetterが
インスタンス化されている。
*/
package util

import scala.collection.mutable.ListBuffer
import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Channel {
  import model._
  import util.ChannelGetter

  /*
  あるニュースに対して関連度のたかそうなNewsを検索する
  */
  def searchRelativeNews(target: model.News, keys: List[String]): Future[List[model.News]] =
    for {
      cs <- filterChannelsBy(target.channel.genre)
    }
    yield {
      val news = cs.map(_.getAllNews).foldRight(List[model.News]())(_ ++ _)
      val buffer = ListBuffer[model.News]()
      keys.foreach { key =>
        news.foreach { n =>
          if ( buffer.size < 3
            && n.id != target.id
            && n.link != target.link
            && n.contentString.contains(key)
            && buffer.find(_.guid == n.guid).map(x => false).getOrElse(true))
            buffer += n
        }
      }
      buffer.toList
    }

  /*
  存在するすべてのニュースを取得する
  */
  def getAllNewsFromChannel[C <: model.Channel]
    (m: Map[String, ChannelGetter[C]])(implicit r: XMLReader[C]): Future[List[model.News]] =
      for (cs <- getChannels(m))
      yield cs.map(_.getAllNews).foldRight(Nil:List[model.News])(_ ++ _)

  def getChannels[C <: model.Channel](mp: Map[String, ChannelGetter[C]])
    (implicit r: XMLReader[C]): Future[List[model.Channel]] =
    mp.toList.foldRight(Future(List[model.Channel]())) { (t, xsf) =>
      for {
        c <- t._2.get
        xs <- xsf
      }
      yield
        c match {
          case Some (channel) => channel :: xs
          case None => xs
        }
    }

  /*
  サイトは関係なく，存在するすべてのチャンネルを集める
  */
  def getAllChannel: Future[List[model.Channel]] = for {
    lc <- getChannels(Livedoor)
    yc <- getChannels(YahooNews)
  }
  yield lc ++ yc

  def filterChannelsBy(genre: Genre): Future[List[model.Channel]] =
    for {
      xs <- getAllChannel
    }
    yield xs.filter(x => x.genre == genre)

  /*
  すべてのサイトのすべてのチャンネルからすべてのニュースを拾う。
  一見重たそうだが，すべてのニュースはキャッシュされているので，
  そんなに重くない。
  */
  def getAllChannelNews: Future[List[model.News]] = for {
    yn <- getAllNewsFromChannel(YahooNews)
    l <- getAllNewsFromChannel(Livedoor)
  }
  yield (l ++ yn).foldRight(List[model.News]()) {(x, xs) =>
    xs.find(n => n.link == x.link) match {
      case Some(_) => xs
      case None => x :: xs
    }
  }

  abstract class LivedoorGetter extends ChannelGetter[livedoor.Channel] {
    def endPoint = "http://news.livedoor.com/"
  }
  val Livedoor = Map(
    "dom"     -> new LivedoorGetter { def entryName = "topics/rss/dom.xml" ; def genre = 国内 },
    "int"     -> new LivedoorGetter { def entryName = "topics/rss/int.xml" ; def genre = 海外 },
    "eco"     -> new LivedoorGetter { def entryName = "topics/rss/eco.xml" ; def genre = 経済 },
    "ent"     -> new LivedoorGetter { def entryName = "topics/rss/ent.xml" ; def genre = 芸能 },
    "spo"     -> new LivedoorGetter { def entryName = "topics/rss/spo.xml" ; def genre = スポーツ },
    "movie"   -> new LivedoorGetter { def entryName = "rss/summary/52.xml" ; def genre = 映画 },
    "gourmet" -> new LivedoorGetter { def entryName = "topics/rss/gourmet.xml" ; def genre = グルメ },
    "love"    -> new LivedoorGetter { def entryName = "topics/rss/love.xml" ; def genre = 女子 },
    "trend"   -> new LivedoorGetter { def entryName = "topics/rss/trend.xml" ; def genre = トレンド },
    "top"     -> new LivedoorGetter { def entryName = "topics/rss/top.xml" ; def genre = トップ}
  )

  abstract class YahooNewsGetter extends ChannelGetter[yahoo.Channel] {
    def endPoint = "http://headlines.yahoo.co.jp/rss/"
  }
  val YahooNews = Map(
    "san-dom" -> new YahooNewsGetter { def entryName = "san-dom.xml" ; def genre = 国内 },
    "nishinp-dom" -> new YahooNewsGetter { def entryName = "nishinp-dom.xml" ; def genre = 国内 },
    "zdn-dom" -> new YahooNewsGetter { def entryName = "zdn_mkt-dom.xml" ; def genre = 国内 },
    "page-dom" -> new YahooNewsGetter { def entryName = "wordleaf-dom.xml" ; def genre = 国内 },

    "afpbbnews" -> new YahooNewsGetter { def entryName = "afpbbnewsv-c_int.xml" ; def genre = 海外 },
    "san-int" -> new YahooNewsGetter { def entryName = "san-c_int.xml" ; def genre = 海外 },
    "asahi-int" -> new YahooNewsGetter { def entryName = "asahik-c_int.xml" ; def genre = 海外 },
    "fuji-int" -> new YahooNewsGetter { def entryName = "ykf-c_int.xml" ; def genre = 海外 },

    "it" ->  new YahooNewsGetter { def entryName = "zdn_ait-c_sci.xml" ; def genre = 経済 },
    "sh_mon-bus" -> new YahooNewsGetter { def entryName = "sh_mon-bus.xml" ; def genre = 経済 },
    "asahi-bus" -> new YahooNewsGetter { def entryName = "asahik-bus.xml" ; def genre = 経済 },
    "scn-bus" -> new YahooNewsGetter { def entryName = "scn-bus.xml" ; def genre = 経済 },

    "natalieo-ent" -> new YahooNewsGetter { def entryName = "natalieo-c_ent.xml" ; def genre = 芸能 },
    "nkgendai-c-ent" -> new YahooNewsGetter { def entryName = "nkgendai-c_ent.xml" ; def genre = 芸能 },
    "natalien-ent" -> new YahooNewsGetter { def entryName = "natalien-c_ent.xml" ; def genre = 芸能 },
    "zdn-ent" -> new YahooNewsGetter { def entryName = "zdn_n-c_ent.xml" ; def genre = 芸能 },

    "spnavi-spo" -> new YahooNewsGetter { def entryName = "spnavi-c_spo.xml" ; def genre = スポーツ },
    "sanspo" -> new YahooNewsGetter { def entryName = "sanspo-c_spo.xml" ; def genre = スポーツ },
    "nishispo" -> new YahooNewsGetter { def entryName = "nishispo-c_spo.xml" ; def genre = スポーツ },
    "nksports" -> new YahooNewsGetter { def entryName = "nksports-c_spo.xml" ; def genre = スポーツ }
  )

}

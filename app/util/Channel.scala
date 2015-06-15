/*
すべてのチェンネルに関する操作を行う。
Channelオブジェクトに実際のニュースを取得するためのChannelGetterが
インスタンス化されている。
*/
package util

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Channel {
  import model._
  import util.ChannelGetter

  /*
  あるニュースに対して関連度のたかそうなNewsを検索する
  */
  def searchRelativeNews(target: model.News, keys: List[String]): Future[List[model.News]] =
    for {
      news <- util.Channel.getAllChannelNews
    }
    yield {
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

  /*
  すべてのサイトのすべてのチャンネルからすべてのニュースを拾う。
  一見重たそうだが，すべてのニュースはキャッシュされているので，
  そんなに重くない。
  */
  def getAllChannelNews: Future[List[model.News]] = for {
    yn <- getAllNewsFromChannel(YahooNews)
    l <- getAllNewsFromChannel(Livedoor)
    // y <- getAllNewsFromChannel(Yahoo) 主旨と違う
  }
  yield (l ++ yn).foldRight(List[model.News]()) {(x, xs) =>
    xs.find(n => n.link == x.link) match {
      case Some(_) => xs
      case None => x :: xs
    }
  }


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

  abstract class YahooNewsGetter extends ChannelGetter[yahoo.news.Channel] {
    def endPoint = "http://headlines.yahoo.co.jp/rss/"
  }
  val YahooNews = Map(
    "san-dom" -> new YahooNewsGetter { def entryName = "san-dom.xml" },
    "nishinp-dom" -> new YahooNewsGetter { def entryName = "nishinp-dom.xml" },
    "zdn-dom" -> new YahooNewsGetter { def entryName = "zdn_mkt-dom.xml" },

    "afpbbnews" -> new YahooNewsGetter { def entryName = "afpbbnewsv-c_int.xml" },
    "san-int" -> new YahooNewsGetter { def entryName = "san-c_int.xml" },
    "asahi-int" -> new YahooNewsGetter { def entryName = "asahik-c_int.xml" },
    "fuji-int" -> new YahooNewsGetter { def entryName = "ykf-c_int.xml" },

    "sh_mon-bus" -> new YahooNewsGetter { def entryName = "sh_mon-bus.xml" },
    "asahi-bus" -> new YahooNewsGetter { def entryName = "asahik-bus.xml" },
    "scn-bus" -> new YahooNewsGetter { def entryName = "scn-bus.xml" },

    "natalieo-ent" -> new YahooNewsGetter { def entryName = "natalieo-c_ent.xml" },
    "nkgendai-c-ent" -> new YahooNewsGetter { def entryName = "nkgendai-c_ent.xml" },
    "natalien-ent" -> new YahooNewsGetter { def entryName = "natalien-c_ent.xml" },
    "zdn-ent" -> new YahooNewsGetter { def entryName = "zdn_n-c_ent.xml" },

    "spnavi-spo" -> new YahooNewsGetter { def entryName = "spnavi-c_spo.xml" },
    "sanspo" -> new YahooNewsGetter { def entryName = "sanspo-c_spo.xml" },
    "nishispo" -> new YahooNewsGetter { def entryName = "nishispo-c_spo.xml" },
    "nksports" -> new YahooNewsGetter { def entryName = "nksports-c_spo.xml" }
  )
}

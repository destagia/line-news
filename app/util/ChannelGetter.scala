package util

import java.util.Date
import scala.collection.mutable.ListBuffer
import play.api.libs.ws._
import play.api.Play.current
import scala.concurrent.Future
import scala.concurrent.Future._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.xml.XML
import model._

abstract class ChannelGetter[C <: model.Channel] {

  /* 抽象メンバー */
  // ニュースのURL
  def endPoint: String

  // ニュースの名前を定義する
  def entryName: String

  def genre: model.Genre

  // キャッシュの更新を許可する間隔(5分)
  val updateDuration = 5 * 60 * 1000

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

  private def runUpdateCache(reader: XMLReader[C]): Future[Unit] =
    requestChannelXML(xml => updateCache(xml, reader))

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
      channel <- requestChannelXML(reader.read(genre))
    }
    yield afterGet(channel)


  private def saveCache(channel: C) {
    cache = Some(channel)
  }
}

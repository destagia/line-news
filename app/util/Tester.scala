package util.tester

import play.api.libs.ws._
import play.api.Play.current
import scala.concurrent.{Future, Await}
import scala.concurrent.Future._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.xml.XML

object Tester {
  import model._

  def checkSpeed(entryName: String, howMany: Int):SpeedResult  = {
    val times = for ( i <- 0 until howMany)
    yield {
      val start = System.currentTimeMillis()
      val f = WS.url(entryName).get()
      Await.result(f, Duration.Inf)
      val end = System.currentTimeMillis()
      (end - start).toInt
    }
    val time = times.foldRight(0)(_ + _) / 1000
    SpeedResult(entryName, howMany, time)
  }

}

package model {

  case class SpeedResult (
    entryName: String,
    howMany: Int,
    time: Double
  )

}
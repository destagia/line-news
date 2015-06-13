package controllers

import play.api._
import play.api.mvc._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import util._

class Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def news = Action.async {
    channel.Top.get.map(x => Ok(x.toString))
  }

  def speedTest(times: Int) = Action {
    Ok(tester.Tester.checkSpeed("http://localhost:9000/news", times).toString)
  }

}

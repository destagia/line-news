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
    Channel.Livedoor("top").get.map {x =>
      Ok(views.html.news(x.map(_.getAllNews).getOrElse(Nil)))
    }
  }

  def newsOne(id: Int) = Action.async {
    Channel.getAllChannelNews.flatMap { news =>
      news.find(_.id == id) match {
        case Some(n) =>
          for
            (relatives <- n.relatives)
          yield
            Ok(views.html.newsOne(n, relatives))
        case None => Future(NotFound("not found id"))
      }
    }
  }

  def allNews() = Action.async {
    Channel.getAllChannelNews.map(x => Ok(x.toString))
  }

  def speedTest(times: Int) = Action {
    Ok(tester.Tester.checkSpeed("http://localhost:9000/news", times).toString)
  }

}

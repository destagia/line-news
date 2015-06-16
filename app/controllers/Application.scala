package controllers

import play.api._
import play.api.mvc._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import util._

class Application extends Controller {



  def index = news("top")

  def news(tag: String) = Action.async {
    for {
      newsOpt <- Channel.Livedoor(tag).get
    }
    yield
      Ok(views.html.news(tag, newsOpt.map(_.getAllNews).getOrElse(Nil)))
  }

  def newsOne(tag: String, id: Int) = Action.async {
    Channel.Livedoor(tag).get.map(_.map(_.getAllNews).getOrElse(Nil)).flatMap { news =>
      news.find(_.id == id) match {
        case Some(n) =>
          for
            (relatives <- n.relatives)
          yield
            n match {
              case ln@model.livedoor.News(title, link, description, date, _, _) =>
                Ok(views.html.newsOne(tag, ln, relatives))
              case _ =>
                NotFound(views.html.notFound())
            }

        case None => Future(NotFound(views.html.notFound()))
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

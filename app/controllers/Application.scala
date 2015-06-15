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

  def news(tag: String) = Action.async {
    for {
      newsOpt <- Channel.Livedoor(tag).get
    }
    yield
      Ok(views.html.news(newsOpt.map(_.getAllNews).getOrElse(Nil)))
  }

  def newsOne(tag: String, id: Int) = Action.async {
    Channel.getAllNewsFromChannel(Channel.Livedoor).flatMap { news =>
      news.find(_.id == id) match {
        case Some(n) =>
          for
            (relatives <- n.relatives)
          yield
            n match {
              case model.livedoor.News(title, link, description, _, date, _) =>
                Ok(views.html.newsOne(n.title, description + "(" + n.id + ")", link, relatives))
              case _ =>
                NotFound("this is not livedoor news")
            }

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

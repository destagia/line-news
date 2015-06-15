package util

import scala.util.Random

object ID {
  val chars = ('0' to '9')
  def getUnique: Int = {
    val list =
      for (i <- (0 to 8)) yield {
        Random.shuffle(if (i == 0) chars.tail
        else chars).headOption.getOrElse(1)
      }
    list.foldRight("")(_.toString() + _).toInt
  }
}
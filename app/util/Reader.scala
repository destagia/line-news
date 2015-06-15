package util

/*
一般化を図ったが結局Channelのパースに特化してしまった。
*/
trait XMLReader[A] {
  def read(genre: model.Genre)(s: String): A
}
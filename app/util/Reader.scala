package util

trait XMLReader[A] {
  def read(s: String): A
}
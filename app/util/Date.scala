package util

import java.util.{Date, Calendar, TimeZone, Locale}
import java.text.SimpleDateFormat

object JavaDate {

  def simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US)
  def getCalendar = {
    val calendar = Calendar.getInstance()
    calendar.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"))
    calendar
  }

  def parse(s: String): Date = {
    // スレッドセーフにするために，呼び出しごとにcalendarは
    // 独立していなければならない。
    val c = getCalendar
    c.setTime(simpleDateFormat.parse(s))
    c.getTime()
  }

  def getCurrent: Date = {
    val d = new Date()
    val c = getCalendar
    c.setTime(d)
    c.getTime()
  }
}
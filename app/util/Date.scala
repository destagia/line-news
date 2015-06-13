package util

import java.util.{Date, Calendar, TimeZone, Locale}
import java.text.SimpleDateFormat

object JavaDate {
  def parse(s: String): Date = {
    // スレッドセーフにするために，呼び出しごとにcalendarは
    // 独立していなければならない。
    val simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US)
    val calendar = Calendar.getInstance()
    calendar.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"))
    calendar.setTime(simpleDateFormat.parse(s))
    calendar.getTime()
  }
}
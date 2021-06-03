package me.zeab.tvtuner.service

import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}

trait DateTimeFormatters {

  val standardDateTimeFormat: DateTimeFormatter =
    new DateTimeFormatterBuilder().appendPattern("yyyyMMddHHmmss")
      .appendLiteral(" ")
      .appendPattern("Z")
      .toFormatter()

}

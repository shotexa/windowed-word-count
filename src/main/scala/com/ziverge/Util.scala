package com.ziverge

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object Util {
  def format(timestamp: Long): String = {
    val formatter = DateTimeFormatter
      .ofPattern("yyyy-MM-dd HH:mm:ss")
      .withZone(ZoneOffset.UTC)

    formatter.format(Instant.ofEpochMilli(timestamp * 1000))
  }

  object Log {
    def success(text: String): Unit = println(
      s"[${Console.GREEN}SUCCESS${Console.RESET}] $text"
    )

    def warn(text: String): Unit = println(
      s"[${Console.YELLOW}WARN${Console.RESET}] $text"
    )

    def error(text: String): Unit = println(
      s"[${Console.RED}ERROR${Console.RESET}] $text"
    )

    def info(text: String): Unit = println(
      s"[${Console.BLUE}INFO${Console.RESET}] $text"
    )

  }
}

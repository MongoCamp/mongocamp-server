package dev.mongocamp.server.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.pattern.color.{ ANSIConstants, ForegroundCompositeConverterBase }

class HighlightingColorConverter extends ForegroundCompositeConverterBase[ILoggingEvent] {

  override protected def getForegroundColorCode(event: ILoggingEvent): String = {
    val level = event.getLevel
    level.toInt match {
      case Level.ERROR_INT =>
        ANSIConstants.BOLD + ANSIConstants.RED_FG // same as default color scheme

      case Level.WARN_INT =>
        ANSIConstants.YELLOW_FG

      case Level.INFO_INT =>
        ANSIConstants.CYAN_FG

      case Level.DEBUG_INT =>
        ANSIConstants.BLUE_FG

      case Level.TRACE_INT =>
        ANSIConstants.GREEN_FG

      case _ =>
        ANSIConstants.DEFAULT_FG
    }
  }

}

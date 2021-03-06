package com.ziverge
package entities

import upickle.default.ReadWriter
import upickle.default.macroRW

final case class WorldCount(event_type: String, world_count: Int)
object WorldCount {
  implicit val rw: ReadWriter[WorldCount] = macroRW
}

final case class Event(event_type: String, data: String, timestamp: Long)
object Event {
  implicit val rw: ReadWriter[Event] = macroRW
}

final case class EventPresentation(event_type: String, data: String, timestamp: String)
object EventPresentation {
  implicit val rw: ReadWriter[EventPresentation] = macroRW
}

final case class Window(events: List[Event], startTime: Long, endTime: Long)
object Window {
  implicit val rw: ReadWriter[Window] = macroRW
}

final case class WindowPresentation(
    events: List[EventPresentation],
    startDate: String,
    endDate: String
  )
object WindowPresentation {
  implicit val rw: ReadWriter[WindowPresentation] = macroRW
}

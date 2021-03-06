package com.ziverge

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success
import scala.util.Try

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl._
import akka.util.ByteString
import upickle.default._

import collection.mutable.{ Map => MutMap }

private object Main extends App {

  def format(timestamp: Long): String = {
    val formatter = DateTimeFormatter
      .ofPattern("yyyy-MM-dd HH:mm:ss")
      .withZone(ZoneOffset.UTC)

    formatter.format(Instant.ofEpochMilli(timestamp * 1000))
  }
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

  implicit val system: ActorSystem = ActorSystem("word-count")

  val proc = os
    .proc("/bin/sh", "-c", "./blackbox")
    .spawn()

  var windowHistory: List[Window] = Nil

  StreamConverters
    .fromInputStream { () => proc.stdout }                        // create stream source from process stdout
    .via(Framing.delimiter(ByteString("\n"), Int.MaxValue, true)) // split by new line
    .map(_.utf8String)
    .map(_.trim)
    .map { str => Try(read[Event](str)) } // try to parse json
    .collect { case Success(event) => event } // filter out invalid events
    .statefulMapConcat(() => { // windowing
      val windowWidth    = 5  // seconds
      val waterMarkDelay = 10 // seconds
      // key is the leftmost boundary of the window (start time)
      val windows: MutMap[Long, List[Event]] = MutMap.empty

      def roundToLowerBoundary(width: Int, timestamp: Long): Long = timestamp - (timestamp % width)
      var currentMaxTimestamp: Long                               = 0

      (event: Event) => {
        currentMaxTimestamp = currentMaxTimestamp.max(event.timestamp)
        // only process events which are not coming too late according to formula:
        // latest (by time) event that has been seen by the system - watermark delay
        if (event.timestamp > (currentMaxTimestamp - waterMarkDelay)) {
          val normalizedTimestamp = roundToLowerBoundary(windowWidth, event.timestamp)
          windows.updateWith(normalizedTimestamp) { // add an event to the window
            case None     => Some(event :: Nil)
            case Some(vs) => Some(event :: vs)
          }
        }
        // get closed windows
        val filtered = windows.filter { x =>
          x._1 <= roundToLowerBoundary(windowWidth, currentMaxTimestamp - waterMarkDelay)
        }
        // remove closed windows from the window set
        windows --= filtered.keys

        // return all closed windows
        filtered
          .toList
          .map { x =>
            val (startTime, events) = x
            Window(events, startTime, startTime + windowWidth)
          }
          .sortBy(_.startTime)

      }
    })
    .runWith(Sink.foreach { window =>
      windowHistory = window :: windowHistory
    })

  val route: Route = get {
    pathPrefix("word-count") {
      concat(
        path("current") {

          val worldCounts = windowHistory
            .flatMap(_.events)
            .foldLeft(Map.empty[String, Int]) { (acc, curr) =>
              val currCount = Some(curr.data.split(" ").size)
              acc.updatedWith(curr.event_type) {
                case None    => currCount
                case Some(v) => currCount.map(_ + v)
              }
            }
            .map { x =>
              val (eventType, wordCount) = x
              WorldCount(eventType, wordCount)
            }

          complete(write(worldCounts))
        },
        path("history") {

          val history = windowHistory.reverse.map { win =>
            WindowPresentation(
              win
                .events
                .map(x =>
                  EventPresentation(
                    x.event_type,
                    x.data,
                    format(x.timestamp)
                  )
                ),
              format(win.startTime),
              format(win.endTime)
            )
          }

          complete {
            write(history)
          }
        }
      )
    }
  }

  val binding = Http().newServerAt("localhost", 8080).bind(route)
  io.StdIn.readLine("Press Enter to terminate the server\n")
  binding.flatMap(_.unbind()).onComplete { _ =>
    proc.destroy()
    proc.destroyForcibly()
    system.terminate()
  }

}

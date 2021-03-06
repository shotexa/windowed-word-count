package com.ziverge

import scala.collection.mutable.{ Map => MutMap }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success
import scala.util.Try

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.util.ByteString
import upickle.default.read

import Util._
import entities._

private object Main extends App {

  implicit val system: ActorSystem = ActorSystem("word-count")

  Blackbox
    .start()                                                      // create stream source from process stdout
    .via(Framing.delimiter(ByteString("\n"), Int.MaxValue, true)) // split by new line
    .map(_.utf8String)
    .map(_.trim)
    .map { str => Try(read[Event](str)) } // try to parse json
    .collect { case Success(event) => event } // filter out invalid events
    .wireTap { x => // log received event
      Log.success(s"Received event: $x")
    }
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
      Repository.addWindow(window)
    })

  val binding = HttpServer.start("localhost", 8080)
  io.StdIn.readLine("Press Enter to terminate the server\n")
  binding.flatMap(_.unbind()).onComplete { _ =>
    Blackbox.stop()
    system.terminate()
  }

}

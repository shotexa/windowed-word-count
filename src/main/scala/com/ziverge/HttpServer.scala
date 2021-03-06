package com.ziverge
import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import upickle.default.write

import Util._
import entities._

object HttpServer {

  val route: Route = get {
    pathPrefix("word-count") {
      concat(
        path("current") {

          val worldCounts = Repository
            .getAllWindows
            .flatMap(_.events)
            .foldLeft(Map.empty[String, Int]) { (acc, curr) =>
              // assuming data is not a single word by sometimes multiply words split by space
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

          complete {
            write(worldCounts)
          }
        },
        path("history") {

          val history = Repository
            .getAllWindows
            .reverse
            .map { win =>
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

  def start(host: String, port: Int)(implicit system: ActorSystem): Future[Http.ServerBinding] =
    Http().newServerAt(host, port).bind(route)
}

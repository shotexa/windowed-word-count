package com.ziverge
package test

import entities._
import com.ziverge.Repository
import akka.http.scaladsl.testkit.ScalatestRouteTest
import upickle.default.read

class WorldCountTestSuite extends TestSuite with ScalatestRouteTest {

  override protected def afterEach(): Unit = {
    Repository.truncate()
  }

  describe("Repository") {

    describe("When calling method addWindow with one Window") {
      it("should add one Window") {
        val window = Window(
          events = List(Event("foo", "bar", 1L)),
          startTime = 1L,
          endTime = 2L
        )
        Repository.addWindow(window)
        Repository.getAllWindows shouldBe List(window)
      }
    }

    describe("Date formatter") {
      it("Should format a timestamp to yyyy-MM-dd HH:mm:ss") {
        Util.format(1615142839) shouldBe "2021-03-07 18:47:19"
      }
    }

    describe("Router") {
      val router = HttpServer.route
      describe("When making get request to word-count/history") {
        val time      = 1615142839
        val formatted = Util.format(time)
        val windows = List(
          Window(
            events = Event("foo", "bar", time) :: Event(
              "foo",
              "baz",
              time
            ) :: Nil,
            startTime = time,
            endTime = time
          ),
          Window(
            events = Event("foo", "bar", time) :: Event(
              "foo",
              "baz",
              time
            ) :: Nil,
            startTime = time,
            endTime = time
          )
        )

        it("Should return all windows that exist in the Repository with formatted dates") {
          windows.foreach(Repository.addWindow(_))
          Get("/word-count/history") ~> router ~> check {
            val response = read[List[WindowPresentation]](responseAs[String])
            val expectedResponse = List(
              WindowPresentation(
                events = EventPresentation("foo", "bar", formatted) :: EventPresentation(
                  "foo",
                  "baz",
                  formatted
                ) :: Nil,
                startDate = formatted,
                endDate = formatted
              ),
              WindowPresentation(
                events = EventPresentation("foo", "bar", formatted) :: EventPresentation(
                  "foo",
                  "baz",
                  formatted
                ) :: Nil,
                startDate = formatted,
                endDate = formatted
              )
            )
            response shouldBe expectedResponse
          }
        }
      }
      describe("When making get request to word-count/current") {
        val time = 1615142839
        val windows = List(
          Window(
            events = Event("foo", "bar", time) :: Event(
              "foo",
              "baz",
              time
            ) :: Nil,
            startTime = time,
            endTime = time
          ),
          Window(
            events = Event("foo", "bar", time) :: Event(
              "foo",
              "baz bar",
              time
            ) :: Nil,
            startTime = time,
            endTime = time
          )
        )
        it(
          "should calculate a word count grouped by event type based on the current state of Repository"
        ) {
          windows.foreach(Repository.addWindow(_))

          Get("/word-count/current") ~> router ~> check {
            val response = read[List[WorldCount]](responseAs[String])
            val expectedResponse = List(
              WorldCount("foo", 5)
            )
            response shouldBe expectedResponse
          }
        }
      }

    }

  }
}

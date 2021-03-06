import Util._

val AkkaVersion     = "2.6.8"
val AkkaHttpVersion = "10.2.2"

lazy val `word-count` = project
  .in(file("."))
  .settings(
    name := "word-count"
  )
  .settings(dependencies)

lazy val dependencies = Seq(
  libraryDependencies ++= Seq(
    Dependencies.Core.com.lihaoyi.`os-lib`,
    Dependencies.Core.com.lihaoyi.upickle,
    "com.typesafe.akka" %% "akka-stream"      % AkkaVersion,
    "com.typesafe.akka" %% "akka-http"        % AkkaHttpVersion,
    "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
  ),
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http-testkit"   % AkkaHttpVersion,
    Dependencies.Test.org.scalatest.scalatest,
    Dependencies.Test.org.scalatestplus.`scalacheck-1-14`
  ).map(_ % Test)
)

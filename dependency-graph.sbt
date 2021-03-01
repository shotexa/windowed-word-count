import Util._

lazy val `word-count` = project
  .in(file("."))
  .settings(
    name := "word-count"
  )
  .settings(dependencies)

lazy val dependencies = Seq(
  libraryDependencies ++= Seq(
    Dependencies.Core.com.lihaoyi.`os-lib`
  ),

  libraryDependencies ++= Seq(
    Dependencies.Test.org.scalatest.scalatest,
    Dependencies.Test.org.scalatestplus.`scalacheck-1-14`
  ).map(_ % Test)
)  

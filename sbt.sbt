import Util._

Global / onChangedBuildSource := ReloadOnSourceChanges
Global / excludeLintKeys ++= Set(
  name,
  autoStartServer
)

Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oSD")
Test / parallelExecution := false

ThisBuild / autoStartServer := false
ThisBuild / turbo := true
ThisBuild / watchForceTriggerOnAnyChange := true
ThisBuild / watchBeforeCommand := Watch.clearScreen
ThisBuild / watchTriggeredMessage := Watch.clearScreenOnTrigger
ThisBuild / shellPrompt := { state => s"${prompt(projectName(state))}> " }


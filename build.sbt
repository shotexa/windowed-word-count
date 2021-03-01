import WarningSettings._
import WarningSettings.Keys._

ThisBuild / organization := "com.ziverge"
ThisBuild / scalaVersion := "2.13.5"
ThisBuild / version := "0.0.1-SNAPSHOT"
ThisBuild / scalacWarningSeverity := WarningSeverity.High
ThisBuild / wartRemoverWarningSeverity := WarningSeverity.Low

ThisBuild / scalacOptions ++= Seq("-encoding", "utf8") // encoding
ThisBuild / scalacOptions ++= Seq(                     // warnings
  "-language:_",
  "-Wunused:_",
  "-Xlint:_",
  "-Wdead-code"
)
ThisBuild / scalacOptions += { // scalac warnings
  (ThisBuild / scalacWarningSeverity).value match {
    case WarningSeverity.Low    => "-Wconf:any:silent"
    case WarningSeverity.Medium => "-Wconf:any:warning"
    case WarningSeverity.High   => "-Wconf:any:error"
  }
}

ThisBuild / wartremoverErrors ++= {
  (ThisBuild / wartRemoverWarningSeverity).value match {
    case WarningSeverity.Low    => hightPriorityWarts
    case WarningSeverity.Medium => hightPriorityWarts
    case WarningSeverity.High   => hightPriorityWarts ++ mediumPriorityWarts
  }
}

ThisBuild / wartremoverWarnings ++= {
  (ThisBuild / wartRemoverWarningSeverity).value match {
    case WarningSeverity.Low    => Seq()
    case WarningSeverity.Medium => mediumPriorityWarts
    case WarningSeverity.High   => lowPriorityWarts
  }
}

lazy val hightPriorityWarts = Seq(
  Wart.ArrayEquals,
  Wart.PublicInference,
  Wart.ExplicitImplicitTypes,
  Wart.FinalCaseClass,
  Wart.JavaConversions,
  Wart.Return
)

lazy val mediumPriorityWarts = Seq(
  Wart.NonUnitStatements,
  Wart.StringPlusAny,
  Wart.AnyVal,
  Wart.AsInstanceOf,
  Wart.IsInstanceOf,
  Wart.EitherProjectionPartial,
  Wart.Enumeration,
  Wart.OptionPartial,
  Wart.Option2Iterable,
  Wart.Product,
  Wart.Recursion,
  Wart.Serializable,
  Wart.TraversableOps,
  Wart.TryPartial,
  Wart.Null
)

lazy val lowPriorityWarts = Seq(
  Wart.DefaultArguments,
  Wart.Equals,
  Wart.FinalVal,
  Wart.ImplicitConversion,
  Wart.ImplicitParameter,
  Wart.JavaSerializable,
  Wart.LeakingSealed,
  Wart.MutableDataStructures,
  Wart.Nothing,
  Wart.Overloading,
  Wart.Throw,
  Wart.ToString,
  Wart.Var,
  Wart.While,
  Wart.Any
)

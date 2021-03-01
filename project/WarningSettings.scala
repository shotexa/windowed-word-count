import sbt._

object WarningSettings {

  sealed trait WarningSeverity
  object WarningSeverity {
    case object Low    extends WarningSeverity
    case object Medium extends WarningSeverity
    case object High   extends WarningSeverity
  }

  object Keys {
    val scalacWarningSeverity = settingKey[WarningSeverity]("Severity level of scalac warnings")
    val wartRemoverWarningSeverity =
      settingKey[WarningSeverity]("Severity level of wartermover warnings")
  }

}

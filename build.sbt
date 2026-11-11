import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.DefaultBuildSettings.targetJvm
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val appName: String = "verify-your-identity-for-a-trust-frontend"

resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")

ThisBuild / scalaVersion := "3.9.0"
ThisBuild / majorVersion := 1
ThisBuild / targetJvm := "jvm-21"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    name := appName,
    RoutesKeys.routesImport += "models.*",
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "play.twirl.api.HtmlFormat.*",
      "uk.gov.hmrc.govukfrontend.views.html.components.*",
      "uk.gov.hmrc.hmrcfrontend.views.html.components.*",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers.*",
      "views.ViewUtils.*",
      "models.Mode",
      "controllers.routes.*"
    ),
    PlayKeys.playDefaultPort := 9789,
    libraryDependencies ++= AppDependencies(),
    scalacOptions ++= Seq(
      "-Wconf:src=routes/.*:s",
      "-Wconf:msg=unused import&src=html/.*:s"
    )
  )
  .settings(CodeCoverageSettings())

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt")

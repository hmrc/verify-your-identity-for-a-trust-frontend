import sbt._

object AppDependencies {
  import play.core.PlayVersion

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "org.reactivemongo" %% "play2-reactivemongo"            % "0.18.6-play27",
    "uk.gov.hmrc"       %% "logback-json-logger"            % "4.8.0",
    "uk.gov.hmrc"       %% "govuk-template"                 % "5.60.0-play-27",
    "uk.gov.hmrc"       %% "play-health"                    % "3.15.0-play-27",
    "uk.gov.hmrc"       %% "play-ui"                        % "8.12.0-play-27",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping"  % "1.3.0-play-26",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-27"     % "3.3.0"
  )

  val test = Seq(
    "org.scalatest"               %% "scalatest"          % "3.0.8",
    "org.scalatestplus.play"      %% "scalatestplus-play" % "4.0.3",
    "org.pegdown"                 %  "pegdown"            % "1.6.0",
    "org.jsoup"                   %  "jsoup"              % "1.10.3",
    "com.typesafe.play"           %% "play-test"          % PlayVersion.current,
    "org.mockito"                 %  "mockito-all"        % "1.10.19",
    "org.scalacheck"              %% "scalacheck"         % "1.14.0",
    "com.github.tomakehurst"      % "wiremock-standalone"      % "2.17.0"
  ).map(_ % Test)

  val akkaVersion = "2.6.7"
  val akkaHttpVersion = "10.1.12"

  val overrides = Seq(
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-protobuf" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion
  )

  def apply(): Seq[ModuleID] = compile ++ test
}

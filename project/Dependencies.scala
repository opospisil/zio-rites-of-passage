import sbt._

object Dependencies {
  import Groups._

  val app = zioGroup ++ configGroup ++ testGroup ++ loggingGroup
  val model              = Seq.empty[ModuleID]
  val service            = zioGroup ++ loggingGroup ++ testGroup
  val config             = configGroup
  val foundations       = zioGroup ++ loggingGroup ++ testGroup ++ tapirGroup ++ dbGroup
}

private object Version {
  val zio     = "2.0.22"
  val zioJson = "0.6.2"

  val logback = "1.3.5"
  val zioLogging = "2.2.3"

  val pureConfig = "0.17.4"

  val tapir    = "1.10.6"
  val sttp     = "3.8.8"
  val javaMail = "1.6.2"
  val stripe   = "24.3.0"
}

private object Groups {
  import Libs._

  val configGroup  = Seq(pureConfig)
  val zioGroup     = Seq(zio, zioStreams, zioJson, zioMacro)
  val testGroup    = Seq(zioTest, zioTestSbt, zioTestMagnolia, zioTestJunit, zioMock, tapirStub)
  val loggingGroup = Seq(logback, zioLogging, zioLoggingSlf4j)
  val tapirGroup   = Seq(tapirSttp, tapirJsonZio, sttpZio, tapirZio, tapirHttp, tapirSwagger)
  val dbGroup      = Seq(quillZio, postgresql, flywaydb, testContainersPostgres)
}

private object Libs {

  val zio             = "dev.zio" %% "zio"               % Version.zio
  val zioStreams      = "dev.zio" %% "zio-streams"       % Version.zio
  val zioJson         = "dev.zio" %% "zio-json"          % Version.zioJson
  val zioLogging      = "dev.zio" %% "zio-logging"       % Version.zioLogging
  val zioLoggingSlf4j = "dev.zio" %% "zio-logging-slf4j" % Version.zioLogging
  val zioMacro        = "dev.zio" %% "zio-macros"        % Version.zio

  val logback = "ch.qos.logback" % "logback-classic" % Version.logback

  val pureConfig = "com.github.pureconfig" %% "pureconfig" % Version.pureConfig

  val zioTest         = "dev.zio"                     %% "zio-test"               % Version.zio   % Test
  val zioTestSbt      = "dev.zio"                     %% "zio-test-sbt"           % Version.zio   % Test
  val zioTestMagnolia = "dev.zio"                     %% "zio-test-magnolia"      % Version.zio   % Test
  val zioTestJunit    = "dev.zio"                     %% "zio-test-junit"         % Version.zio   % Test
  val zioMock         = "dev.zio"                     %% "zio-mock"               % "1.0.0-RC9"   % Test
  val tapirStub       = "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % Version.tapir % Test

  val tapirSttp    = "com.softwaremill.sttp.tapir"   %% "tapir-sttp-client"       % Version.tapir
  val tapirJsonZio = "com.softwaremill.sttp.tapir"   %% "tapir-json-zio"          % Version.tapir
  val sttpZio      = "com.softwaremill.sttp.client3" %% "zio"                     % Version.sttp
  val tapirZio     = "com.softwaremill.sttp.tapir"   %% "tapir-zio"               % Version.tapir
  val tapirHttp    = "com.softwaremill.sttp.tapir"   %% "tapir-zio-http-server"   % Version.tapir
  val tapirSwagger = "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui-bundle" % Version.tapir

  val quillZio               = "io.getquill"           %% "quill-jdbc-zio"                    % "4.7.3"
  val postgresql             = "org.postgresql"         % "postgresql"                        % "42.5.0"
  val flywaydb               = "org.flywaydb"           % "flyway-core"                       % "9.7.0"
  val testContainersPostgres = "io.github.scottweaver" %% "zio-2-0-testcontainers-postgresql" % "0.9.0"
  val zioPrelude             = "dev.zio"               %% "zio-prelude"                       % "1.0.0-RC23"
  val jwt                    = "com.auth0"              % "java-jwt"                          % "4.2.1"
  val javaMail               = "com.sun.mail"           % "javax.mail"                        % Version.javaMail
  val stripe                 = "com.stripe"             % "stripe-java"                       % Version.stripe

}

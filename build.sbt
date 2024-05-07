lazy val grabGitSha = taskKey[Unit]("stores the latest git sha to a resource file")

lazy val commonSettings = Seq(
  version       := "0.0.1",
  scalaVersion  := "2.12.17",
  scalacOptions := "-Xasync" :: "-Ywarn-unused-import" :: "-encoding" :: "utf8" :: "-Xfatal-warnings" :: "-deprecation" :: "-unchecked" :: "-language:higherKinds" :: "-feature" :: "-Ypartial-unification" :: Nil,
  organization  := "opos",
  testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  libraryDependencies ++= {
    import Ordering.Implicits._
    if (VersionNumber(scalaVersion.value).numbers >= Seq(2L, 13L)) {
      Nil
    } else {
      Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full))
    }
  }
)

lazy val app = project
  .settings(
    commonSettings,
    publish / skip := false,
    libraryDependencies ++= Dependencies.app,
    grabGitSha := {
      val proc   = scala.sys.process.Process("git rev-parse HEAD")
      val commit = if (proc.run().exitValue() == 0) proc.lineStream.head else "[not a git repo]"
      IO.write(new File("app/src/main/resources/commit"), commit)
    },
    Compile / compile := ((Compile / compile) dependsOn grabGitSha).value
  )
  .dependsOn(service, config)

lazy val model = project
  .settings(
    commonSettings
  )

lazy val service = project
  .settings(
    commonSettings,
    libraryDependencies ++= Dependencies.service
  )
  .dependsOn(
    model,
    config
  )

lazy val config = project
  .settings(
    commonSettings,
    libraryDependencies ++= Dependencies.config
  )

lazy val foundations = project
  .settings(
    commonSettings,
    libraryDependencies ++= Dependencies.foundations
  )

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "zio-rites-of-passage",
    commonSettings,
    publish / skip      := true,
    Compile / mainClass := Some("com.opos.zrp.Runner")
  )
  .aggregate(foundations)
  .dependsOn(
    foundations
  )

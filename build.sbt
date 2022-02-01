lazy val root = project
  .in(file("."))
  .enablePlugins(JavaAppPackaging, DockerPlugin, AshScriptPlugin)
  .settings(
    commonSettings,
    name := "docker-demo",
    Universal / mappings += file("src/main/resources/logback.xml") -> "logback.xml",
    dockerBaseImage := "adoptopenjdk/openjdk11:jre-11.0.14_9-alpine",
    inConfig(IntegrationTest)(org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings),
    version := "0.1.0",
    Defaults.itSettings,
    libraryDependencies ++= Seq(
      Libraries.`doobie-core`,
      Libraries.`doobie-hikari`,
      Libraries.`doobie-postgres`,
      Libraries.log4cats,
      Libraries.logback,
      Libraries.`fs2-kafka`,
      Libraries.postgres,
      Libraries.redis4cats,
      Libraries.`redis4cats-logs`,
      Libraries.Test.`munit-effect` % IntegrationTest
    )
  )
  .configs(IntegrationTest)
  .dependsOn(
    grpc % "compile->compile;test->test"
  )

lazy val grpc = project
  .in(file("grpc"))
  .settings(
    commonSettings,
    Compile / PB.targets := Seq(
      scalapb.gen(grpc = false) -> (Compile / sourceManaged).value
    ),
    name := "grpc"
  )

lazy val commonSettings = Seq(
  scalaVersion := "3.1.1",
  Compile / scalacOptions ++= Seq(
    "-feature",
    "-unchecked",
    "-deprecation"
  ),
  testFrameworks += new TestFramework("munit.Framework")
)
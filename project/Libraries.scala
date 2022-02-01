import sbt._

object Libraries {
  val `doobie-core`     = "org.tpolecat"     %% "doobie-core"         % Version.doobie
  val `doobie-hikari`   = "org.tpolecat"     %% "doobie-hikari"       % Version.doobie
  val `doobie-postgres` = "org.tpolecat"     %% "doobie-postgres"     % Version.doobie
  val `fs2-kafka`       = "com.github.fd4s"  %% "fs2-kafka"           % Version.fs2Kafka
  val log4cats          = "org.typelevel"    %% "log4cats-slf4j"      % Version.log4cats
  val logback           = "ch.qos.logback"   % "logback-classic"      % Version.logback
  val postgres          = "org.postgresql"   % "postgresql"           % Version.postgres
  val redis4cats        = "dev.profunktor"   %% "redis4cats-effects"  % Version.redis4cats
  val `redis4cats-logs` = "dev.profunktor"   %% "redis4cats-log4cats" % Version.redis4cats

  object Test {
    val `munit-effect` = "org.typelevel" %% "munit-cats-effect-3" % Version.munitEffect
  }

  object Compiler {
    val `better-monadic-for` = "com.olegpy"    %% "better-monadic-for" % Version.betterMonadicFor
    val `kind-projector`     = "org.typelevel" %% "kind-projector"     % Version.kindProjector
  }

  object Version {
    val betterMonadicFor = "0.3.1"
    val kindProjector    = "0.13.2"
    val doobie           = "1.0.0-RC2"
    val fs2Kafka         = "3.0.0-M4"
    val grpc             = "1.26.0"
    val http4s           = "1.0.0-M30"
    val log4cats         = "2.2.0"
    val logback          = "1.2.10"
    val postgres         = "42.3.1"
    val munit            = "0.7.27"
    val munitEffect      = "1.0.7"
    val redis4cats       = "1.0.0"
  }
}

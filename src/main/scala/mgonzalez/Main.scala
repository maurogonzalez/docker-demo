package mgonzalez

import cats.effect.{ IO, IOApp }
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp.Simple:
  val run = Slf4jLogger.create[IO].flatMap(_.info("Hi!"))

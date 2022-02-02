package mgonzalez

import cats.effect.unsafe.IORuntime.global
import cats.effect.{ IO, IOApp, Resource }
import cats.syntax.all.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.*
import org.http4s.HttpRoutes
import org.http4s.Method.GET
import org.http4s.implicits.*
import org.http4s.server.{ Router, Server } 
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.http4s.HttpApp

object Main extends IOApp.Simple:
  val helloWorldService: HttpRoutes[IO] =
    HttpRoutes.of[IO] { case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name.")
    }

  val httpApp: HttpApp[IO] = Router("/" -> helloWorldService).orNotFound

  val server: Resource[IO, Server] = EmberServerBuilder.default[IO].withHttpApp(httpApp).build

  val run = Slf4jLogger.create[IO].flatMap(_.info("Running my app")) *>
    server.use(_ => IO.never)

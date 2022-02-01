package mgonzalez.clients

import cats.effect.{ IO, Resource }
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object PostgresClient:
  def transactor(user: String, pwd: String): Resource[IO, HikariTransactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](8)
      xa <- HikariTransactor.newHikariTransactor[IO](
              "org.postgresql.Driver",
              "jdbc:postgresql://localhost:5432/oddisey",
              user,
              pwd,
              ce
            )
    } yield xa

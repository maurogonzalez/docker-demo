package mgonzalez.clients

import cats.effect.{ IO, Resource }
import dev.profunktor.redis4cats.connection.{ RedisClient => RClient }
import dev.profunktor.redis4cats.data.RedisCodec
import dev.profunktor.redis4cats.log4cats.log4CatsInstance
import dev.profunktor.redis4cats.{ Redis, RedisCommands }
import org.typelevel.log4cats.Logger

object RedisClient:
  def redisStandalone[K, V](host: String, codec: RedisCodec[K, V])(using
    Logger[IO]
  ): Resource[IO, RedisCommands[IO, K, V]] =
    for {
      client <- RClient[IO].from(s"redis://$host")
      redis  <- Redis[IO].fromClient(client, codec)
    } yield redis

  val stringCodec: RedisCodec[String, String] = RedisCodec.Utf8

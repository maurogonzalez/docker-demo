package mgonzalez.clients

import cats.effect.IO
import cats.syntax.applicative._
import fs2.kafka.{ Deserializer, Serializer }
import mgonzalez.BaseSpec
import oddisey.grpc.example.Odysseus

trait KafkaBaseSpec extends BaseSpec:
  protected val host                 = "localhost"
  protected val port                 = 9092
  protected val odysseusDeserializer = Deserializer.lift(bs => Odysseus.parseFrom(bs).pure[IO])
  protected val odysseusSerializer   = Serializer.lift[IO, Odysseus](_.toByteArray.pure[IO])

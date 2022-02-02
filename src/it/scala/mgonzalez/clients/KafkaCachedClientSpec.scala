package mgonzalez.clients

import cats.effect.{ IO, Ref }
import cats.syntax.applicative.*
import dev.profunktor.redis4cats.RedisCommands
import dev.profunktor.redis4cats.data.RedisCodec
import fs2.concurrent.SignallingRef
import fs2.kafka.{ CommittableConsumerRecord, Deserializer, ProducerRecord, ProducerRecords, Serializer }
import oddisey.grpc.example.Odysseus
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.util.UUID
import scala.concurrent.duration.DurationInt

class KafkaCachedClientSpec extends KafkaBaseSpec:
  given logger: Logger[IO] = Slf4jLogger.create[IO].unsafeRunSync()

  private val codec = RedisClient.stringCodec
  private val topic = s"OddiseyTopic${UUID.randomUUID()}"
  private val group = s"OddiseyConsumerGroup${UUID.randomUUID()}"

  private val keySerializer   = Serializer[IO, String]
  private val keyDeserializer = Deserializer[IO, String]

  private val producer = KafkaClient.kafkaProducer(host, port, keySerializer, odysseusSerializer)
  private val consumer = KafkaClient.kafkaConsumer(host, port, keyDeserializer, odysseusDeserializer, group)
  private val ids      = (1 to 10).map(_ => UUID.randomUUID().toString).toList
  private val record   = ids.map(i => ProducerRecord(topic, i, Odysseus(i)))

  private val produce = producer.use(p => p.produce(ProducerRecords(record)).flatten)

  def cache(redis: RedisCommands[IO, String, String], key: String): IO[Boolean] =
    for {
      isSet <- redis.hSetNx(key, "field", "value")
      _     <- redis.expire(key, 1.second)
    } yield isSet

  def consume(
    redis: RedisCommands[IO, String, String],
    msg: Ref[IO, List[Odysseus]],
    externalSignal: SignallingRef[IO, Boolean]
  ): IO[Unit] =
    consumer
      .evalTap(_.subscribeTo(topic))
      .evalTap(_ => logger.info("Starting consumer"))
      .use(c =>
        SignallingRef[IO, Boolean](false).flatMap(localSignal =>
          c.stream
            .evalMap(m =>
              cache(redis, m.record.key)
                .flatMap(isNew => handler(m, msg).whenA(isNew))
            )
            .evalTap(_ => msg.get.flatMap(ct => localSignal.set(true).whenA(ct.nonEmpty && ct.size % 5 == 0)))
            .handleErrorWith(err => fs2.Stream.eval(logger.error(err)(s"Error in consumer")))
            .interruptWhen(localSignal)
            .interruptWhen(externalSignal)
            .compile
            .drain
        )
      )

  def handler(
    message: CommittableConsumerRecord[IO, String, Odysseus],
    msgs: Ref[IO, List[Odysseus]]
  ): IO[Unit] =
    msgs.update(message.record.value :: _) *> message.offset.commit

  test("kafka-cached") {
    RedisClient.redisStandalone("localhost", codec).use(redis =>
      for {
        msgs   <- Ref.of[IO, List[Odysseus]](List())
        signal <- SignallingRef[IO, Boolean](false)
        _      <- produce.start
        c <- fs2.Stream
              .repeatEval(consume(redis, msgs, signal))
              .interruptWhen(signal)
              .compile
              .drain
              .start
        _ <- fs2.Stream
              .repeatEval(msgs.get)
              .evalMap(r1 => (signal.set(true) *> c.cancel).whenA(r1.size >= ids.size))
              .interruptWhen(signal)
              .interruptAfter(25.seconds)
              .compile
              .drain
        result <- msgs.get
      } yield assertEquals(result.map(_.message).toSet, ids.toSet)
    )
  }

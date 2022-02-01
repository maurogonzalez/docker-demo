package mgonzalez.clients

import cats.effect.{ Deferred, IO }
import fs2.concurrent.SignallingRef
import fs2.kafka.{ Deserializer, ProducerRecord, ProducerRecords, Serializer }
import oddisey.grpc.example.Odysseus

class KafkaClientSpec extends KafkaBaseSpec:

  test("producer/consumer") {
    val topic = "OdysseyTopic"
    val group = "OdysseyConsumerGroup"

    val keySerializer   = Serializer[IO, String]
    val keyDeserializer = Deserializer[IO, String]
    val producer        = KafkaClient.kafkaProducer(host, port, keySerializer, odysseusSerializer)
    val consumer        = KafkaClient.kafkaConsumer(host, port, keyDeserializer, odysseusDeserializer, group)

    val words  = "But be content with the food and drink aboard our ship ..."
    val record = ProducerRecord(topic, "first", Odysseus(words))

    val produce = producer.use(_.produce(ProducerRecords.one(record)).flatten)

    def consume(msg: Deferred[IO, Odysseus], signal: SignallingRef[IO, Boolean]) =
      consumer
        .evalTap(_.subscribeTo(topic))
        .use(c =>
          c.stream
            .evalMap(committable =>
              committable.offset.commit &>
              msg.complete(committable.record.value) &>
              signal.set(true)
            )
            .interruptWhen(signal)
            .compile
            .drain
        )

    val process = for {
      msg <- Deferred[IO, Odysseus]
      s   <- SignallingRef[IO, Boolean](false)
      p   <- produce.start
      c   <- consume(msg, s).start
      _   <- p.join
      _   <- c.join
    } yield msg

    process
      .flatMap(_.get)
      .map { msg =>
        assertEquals(msg.message, words)
      }
  }

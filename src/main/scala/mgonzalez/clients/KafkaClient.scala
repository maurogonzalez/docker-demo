package mgonzalez.clients

import java.util.UUID
import cats.effect.*
import fs2.kafka.*

object KafkaClient:

  def kafkaAdmin[F[_]: Async](host: String, port: Long): Resource[F, KafkaAdminClient[F]] =
    KafkaAdminClient.resource[F](AdminClientSettings(s"$host:$port"))

  def kafkaProducer[F[_]: Async, K, V](
    host: String,
    port: Long,
    keySerializer: Serializer[F, K],
    valueSerializer: Serializer[F, V]
  ): Resource[F, KafkaProducer[F, K, V]] =
    KafkaProducer.resource(producerSettings(host, port, keySerializer, valueSerializer))

  def kafkaConsumer[F[_]: Async, K, V](
    host: String,
    port: Long,
    keyDeserializer: Deserializer[F, K],
    valueDeserializer: Deserializer[F, V],
    consumerGroup: String
  ): Resource[F, KafkaConsumer[F, K, V]] =
    KafkaConsumer.resource(consumerSettings(host, port, consumerGroup, keyDeserializer, valueDeserializer))

  private def producerSettings[F[_]: Concurrent, K, V](
    host: String,
    port: Long,
    keySerializer: Serializer[F, K],
    valueSerializer: Serializer[F, V]
  ) =
    ProducerSettings(
      keySerializer   = keySerializer,
      valueSerializer = valueSerializer
    ).withBootstrapServers(s"$host:$port")

  private def consumerSettings[F[_]: Concurrent, K, V](
    host: String,
    port: Long,
    consumerGroup: String,
    keyDeserializer: Deserializer[F, K],
    valueDeserializer: Deserializer[F, V]
  ) =
    ConsumerSettings(
      keyDeserializer   = keyDeserializer,
      valueDeserializer = valueDeserializer
    ).withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers(s"$host:$port")
      .withGroupId(consumerGroup)
      .withClientId(s"$consumerGroup-${UUID.randomUUID()}")

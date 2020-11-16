import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer, StringSerializer}
import akka.actor.ActorSystem
import akka.kafka.{CommitterSettings, ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.Config
import org.apache.kafka.clients.producer.ProducerRecord
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}


object consumerApp extends App {
  println("Consumer service #1 has started")

  implicit val system: ActorSystem = ActorSystem("consumer-sample")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val consumerConfig: Config = system.settings.config.getConfig("akka.kafka.consumer")
  val producerConfig: Config = system.settings.config.getConfig("akka.kafka.producer")
  val server = system.settings.config.getString("akka.kafka.consumer.kafka-clients.server")
  val topic = system.settings.config.getString("akka.kafka.consumer.kafka-clients.topic")
  val committerConfig: Config = system.settings.config.getConfig("akka.kafka.committer")

  val consumerSettings =
    ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(server)
      .withGroupId("group1")

  val producerSettings =
    ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)
      .withBootstrapServers(server)

  val committerSettings = CommitterSettings(committerConfig)


  val consumer =
    Consumer
      .committableSource(consumerSettings, Subscriptions.topics("testTopic"))
      .map { msg =>
        println(s"Got message: ${msg.record.value()}")

        ProducerMessage.single(
          new ProducerRecord(topic, msg.record.key, (msg.record.value.toInt * 2).toString),
          msg.committableOffset
        )
      }
      .toMat(Producer.committableSink(producerSettings, committerSettings))(DrainingControl.apply)
      .run()
}



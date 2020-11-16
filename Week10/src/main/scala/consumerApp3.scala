import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.Config
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse}
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{CommitterSettings, ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import io.circe.syntax.EncoderOps
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}


object consumerApp3 extends App {
  println("Consumer service #3 has started")

  implicit val system: ActorSystem = ActorSystem("consumer-sample")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val consumerConfig: Config = system.settings.config.getConfig("akka.kafka.consumer2")
  val producerConfig: Config = system.settings.config.getConfig("akka.kafka.producer")
  val server = system.settings.config.getString("akka.kafka.consumer2.kafka-clients.server")
  val topic1 = system.settings.config.getString("akka.kafka.consumer.kafka-clients.topic")
  val topic2 = system.settings.config.getString("akka.kafka.consumer2.kafka-clients.topic")
  val committerConfig: Config = system.settings.config.getConfig("akka.kafka.committer")

  val consumerSettings =
    ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(server)
      .withGroupId("group1")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  var nums: List[String] = List()

  val consumer =
    Consumer
      .committableSource(consumerSettings, Subscriptions.topics(topic1, topic2))
      .toMat(Sink.foreach(msg => nums = nums:+ msg.record.value()))(DrainingControl.apply)
      .run()

  Thread.sleep(5000)
  consumer.drainAndShutdown()


    val responseFuture: Future[HttpResponse] = Http()(system).singleRequest(
      HttpRequest(
        method = HttpMethods.POST,
        uri = "http://127.0.0.1:8080/nums",
        entity = HttpEntity(ContentTypes.`application/json`, nums.asJson.toString())))
}


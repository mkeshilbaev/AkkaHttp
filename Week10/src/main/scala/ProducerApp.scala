import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, RunnableGraph, Sink, Source}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import scala.concurrent.Future
import scala.util.{Failure, Random, Success}


object ProducerApp extends  App {

  implicit val system = ActorSystem("QuickStart")
  implicit val ec = system.dispatcher

  val config = system.settings.config.getConfig("akka.kafka.producer")
  val server = system.settings.config.getString("akka.kafka.producer.kafka-clients.server")
  val topic  = system.settings.config.getString("akka.kafka.producer.kafka-clients.topic")

  val producerSettings =
    ProducerSettings(config, new StringSerializer, new StringSerializer)
      .withBootstrapServers(server)

  val done: Future[Done] =
    Source.fromIterator(() => Iterator.continually(Random.nextInt()))
      .map(_.toString)
      .map(value => new ProducerRecord[String, String](topic, value))
      .runWith(Producer.plainSink(producerSettings))

//  val done: Future[Done] =
//    Source(1 to 10)
//      .map(_.toString)
//      .map(value => new ProducerRecord[String, String](topic, value))
//      .runWith(Producer.plainSink(producerSettings))


  done.onComplete {
    case Success(value) => {
      println(value)
      system.terminate()
    }
    case Failure(exception) => {
      println(exception)
    }
  }
}

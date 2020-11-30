//package service
//
//import akka.actor.typed.scaladsl.Behaviors
//import akka.actor.typed.{ActorRef, Behavior}
//import node.Node.Pong
//
//object WordCountService {
//
//  final case class Ping(value: Int, replyTo: ActorRef[Pong])
//
//  def apply(nodeName: String): Behavior[Ping] = Behaviors.setup { context =>
//
//    Behaviors.receiveMessage { message =>
//      message match {
//        case Ping(value, replyTo) => {
//          val x = fibonacci(value)
//          //Thread.sleep(10000)
//          replyTo ! Pong(nodeName, x)
//          Behaviors.same
//        }
//      }
//    }
//  }
//
//  def readFile(): Map[String, Int] ={
//    val counter = scala.io.Source.fromFile("src/main/resources/words.txt")
//      .getLines
//      .flatMap(_.split("\\W+"))
//      .foldLeft(Map.empty[String, Int]){
//        (count, word) => count + (word -> (count.getOrElse(word, 0) + 1))
//      }
//    return counter
//  }
//
//  def printContent(counter : Map[String, Int]): Unit = {
//    for ((k,v) <- counter) printf("%s : %d\n", k, v)
//  }
//
//
//  def fibonacci(x: Int): BigInt = {
//    def fibHelper(x: Int, prev: BigInt = 0, next: BigInt = 1): BigInt = x match {
//      case 0 => prev
//      case 1 => next
//      case _ => fibHelper(x - 1, next, next + prev)
//    }
//    fibHelper(x)
//  }
//}
package sample.cluster.stats

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import sample.cluster.CborSerializable

import scala.concurrent.duration._

//#service
object WordCountService {

  sealed trait Command extends CborSerializable
  final case class ProcessText(text: String, replyTo: ActorRef[Response]) extends Command {
    require(text.nonEmpty)
  }
  case object Stop extends Command

  sealed trait Response extends CborSerializable
  final case class JobResult(meanWordLength: Double) extends Response
  final case class JobFailed(reason: String) extends Response

  def apply(workers: ActorRef[StatsWorker.Process]): Behavior[Command] =
    Behaviors.setup { ctx =>
      // if all workers would crash/stop we want to stop as well
      ctx.watch(workers)

      Behaviors.receiveMessage {
        case ProcessText(text, replyTo) =>
          ctx.log.info("Delegating request")
          val words = text.split(' ').toIndexedSeq
          // create per request actor that collects replies from workers
          ctx.spawnAnonymous(StatsAggregator(words, workers, replyTo))
          Behaviors.same
        case Stop =>
          Behaviors.stopped
      }
    }
}

object StatsAggregator {

  sealed trait Event
  private case object Timeout extends Event
  private case class CalculationComplete(length: Int) extends Event

  def apply(words: Seq[String], workers: ActorRef[StatsWorker.Process], replyTo: ActorRef[StatsService.Response]): Behavior[Event] =
    Behaviors.setup { ctx =>
      ctx.setReceiveTimeout(3.seconds, Timeout)
      val responseAdapter = ctx.messageAdapter[StatsWorker.Processed](processed =>
        CalculationComplete(processed.length)
      )

      words.foreach { word =>
        workers ! StatsWorker.Process(word, responseAdapter)
      }
      waiting(replyTo, words.size, Nil)
    }

  private def waiting(replyTo: ActorRef[StatsService.Response], expectedResponses: Int, results: List[Int]): Behavior[Event] =
    Behaviors.receiveMessage {
      case CalculationComplete(length) =>
        val newResults = results :+ length
        if (newResults.size == expectedResponses) {
          val meanWordLength = newResults.sum.toDouble / newResults.size
          replyTo ! StatsService.JobResult(meanWordLength)
          Behaviors.stopped
        } else {
          waiting(replyTo, expectedResponses, newResults)
        }
      case Timeout =>
        replyTo ! StatsService.JobFailed("Service unavailable, try again later")
        Behaviors.stopped
    }

}
//#service

package service

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import node.Node.Pong

object FibonacciService {

  final case class Ping(value: Int, replyTo: ActorRef[Pong])

  def apply(nodeName: String): Behavior[Ping] = Behaviors.setup { context =>

    Behaviors.receiveMessage { message =>
      message match {
        case Ping(value, replyTo) => {
          val x = fibonacci(value)
          //Thread.sleep(10000)
          replyTo ! Pong(nodeName, x)
          Behaviors.same
        }
      }
    }
  }

  def fibonacci(x: Int): BigInt = {
    def fibHelper(x: Int, prev: BigInt = 0, next: BigInt = 1): BigInt = x match {
      case 0 => prev
      case 1 => next
      case _ => fibHelper(x - 1, next, next + prev)
    }
    fibHelper(x)
  }
}
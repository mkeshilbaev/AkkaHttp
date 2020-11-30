package node

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.server.Directives.onSuccess
import akka.util.Timeout
import service.FibonacciService
import scala.concurrent.duration.DurationInt


object Node {

  val NodeServiceKey = ServiceKey[Command]("node-service-key")

  sealed trait Command

  final case class Pong(nodeName: String, res: BigInt) extends Command
  final case class FindNumberPing(n: Int) extends Command


  def apply(): Behavior[Command] = {
    Behaviors.setup[Command] { context =>

      implicit def system = context.system
      implicit def scheduler = context.system.scheduler
      implicit lazy val timeout = Timeout(5.seconds)

      context.system.receptionist ! Receptionist.Register(NodeServiceKey, context.self)
      val ps = context.spawnAnonymous(FibonacciService("node_x"))

      Behaviors.receiveMessage { message =>
        message match {
          case FindNumberPing(n) => {
            ps ! FibonacciService.Ping(n, context.self)
            Behaviors.same
          }
          case Pong(nodeName, res) =>{
            println(s"nodeName:${nodeName} result:${res}")
            Behaviors.same
          }
        }
      }
    }
  }
}
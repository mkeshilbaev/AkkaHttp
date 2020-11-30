package api

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{ActorContext, GroupRouter}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.path
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import node.Node
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt


trait Router {
  def route: Route
}


class NodeRouter(node:ActorRef[Node.Command])(implicit system: ActorSystem[_], ex:ExecutionContext)
  extends  Router
    with  Directives {

  override def route = pathPrefix("process") {
    path(IntNumber) { n =>
      get {
        node ! Node.FindNumberPing(n)
        complete("ok")
      }
    }
  }
}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.{Behaviors, Routers}
import com.typesafe.config.{Config, ConfigFactory}
import api.{NodeRouter, Server}
import node.Node
import org.slf4j.{Logger, LoggerFactory}


object Boot {

  val config: Config = ConfigFactory.load()
  val address = config.getString("http.ip")
  val port = config.getInt("http.port")
  val nodeId = config.getString("clustering.ip")



  def readFile(): Map[String, Int] ={
    val counter = scala.io.Source.fromFile("src/main/resources/words.txt")
      .getLines
      .flatMap(_.split("\\W+"))
      .foldLeft(Map.empty[String, Int]){
        (count, word) => count + (word -> (count.getOrElse(word, 0) + 1))
      }
    return counter
  }
  def printContent(counter : Map[String, Int]): Unit = {
    for ((k,v) <- counter) printf("%s : %d\n", k, v)
  }


  def main(args: Array[String]): Unit = {
    val map = Boot.readFile()
    Boot.printContent(map)



    implicit val log: Logger = LoggerFactory.getLogger(getClass)

    val rootBehavior = Behaviors.setup[Node.Command] { context =>
      context.spawnAnonymous(Node())
      val group = Routers.group(Node.NodeServiceKey).withRoundRobinRouting()
      val node = context.spawnAnonymous(group)
      val router = new NodeRouter(node)(context.system, context.executionContext)
      Server.startHttpServer(router.route, address, port)(context.system, context.executionContext)
      Behaviors.empty
    }
    ActorSystem[Node.Command](rootBehavior, "cluster-playground")

  }
}
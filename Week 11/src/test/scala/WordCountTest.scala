//import akka.actor.typed.{ActorSystem, Props}
//import akka.testkit.{ImplicitSender, TestKit}
//import org.scalatest.{MustMatchers, WordSpec}
//
//class WordCountTest extends TestKit(ActorSystem("test"))
//  with WordSpec
//  with MustMatchers
//  with StopSystemAfterAll
//  with ImplicitSender {
//  val receptionist = system.actorOf(Props[TestReceptionist],
//    JobReceptionist.name)
//  val words = List("this is a test ",
//    "this is a test",
//    "this is",
//    "this")
//  "The words system" must {
//    "count the occurrence of words in a text" in {
//      receptionist ! JobRequest("test2", words)
//      expectMsg(JobSuccess("test2", Map("this" -> 4,
//        "is"   -> 3,
//        "a"    -> 2,
//        "test" -> 2)))
//      expectNoMsg
//    }
//
//    "continue to process a job with intermittent failures" in {
//      val wordsWithFail = List("this", "is", "a", "test", "FAIL!")
//      receptionist ! JobRequest("test4", wordsWithFail)
//      expectMsg(JobSuccess("test4", Map("this" -> 1,
//        "is"   -> 1,
//        "a"    -> 1,
//        "test" -> 1)))
//      expectNoMsg
//    }
//  }
//}
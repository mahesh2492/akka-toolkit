package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._
import scala.util.Random

class BasicSpec extends TestKit(ActorSystem("BasicSpec"))
with ImplicitSender
with WordSpecLike
with BeforeAndAfterAll {
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import BasicSpec._
  "A simple actor" should {
    "send back the same message" in {
      val echoActor = system.actorOf(Props[SimpleActor])
      val message = "hello, test"
      echoActor ! message
      expectMsg(message)
    }
  }

  "A blackhole actor" should {
    "send back some message" in {
      val blackhole = system.actorOf(Props[BlackHole])
      val message = "hello, test"
      blackhole ! message
      expectNoMsg(1 seconds)
    }
  }

  "A LabTest actor" should {
    val labTestActor = system.actorOf(Props[LabTestActor])
    "turn a string into uppercase" in {
      val message = "I love Akka"
      labTestActor ! message
      expectMsg("I LOVE AKKA")
    }

    "reply to a greeting" in {
      labTestActor ! "greeting"
      expectMsgAnyOf("hi", "hello")
    }

    "reply with favourite tech" in {
       labTestActor ! "favouriteTech"
      expectMsgAllOf("Scala", "Akka")
    }

    "reply with cool tech in different way" in {
      labTestActor ! "favouriteTech"
      receiveN(2)
    }

    "reply with cool tech in fancy way" in {
      labTestActor ! "favouriteTech"
      expectMsgPF() {
        case "Scala" => // only care that pf is defined
        case "Akka"  =>
      }

    }
  }

}

object BasicSpec {
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message: String => sender() ! message
    }
  }

  class BlackHole extends Actor {
    override def receive: Receive = ???
  }

  class LabTestActor extends Actor {
    val random = new Random()
    override def receive: Receive = {
      case "greeting" =>
        if(random.nextBoolean()) sender() ! "hi" else sender() ! "hello"
      case "favouriteTech" =>
        sender() ! "Scala"
        sender() ! "Akka"
      case message: String => sender() ! message.toUpperCase
    }
  }
}

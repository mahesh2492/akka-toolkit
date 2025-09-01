package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._
import scala.util.Random

class TimedAssertionSpec extends TestKit(
  ActorSystem("TimedAssertionSpec", ConfigFactory.load().getConfig("specialTimedAssertionsConfig")))
with ImplicitSender
with WordSpecLike
with BeforeAndAfterAll {
  override def afterAll(): Unit = super.afterAll()

  import TimedAssertionSpec._

  "A worker actor" should {
    val workerActor = system.actorOf(Props[WorkerActor])

    "reply with the meaning of life in a timely manner" in {
      within(500 millis, 1 seconds) {
        workerActor ! "work"
        expectMsg(WorkResult(42))
      }
    }

    "reply with valid work at a reasonable cadence" in {
      within(1 seconds) {
        workerActor ! "workSequence"
        val result: Seq[Int] = receiveWhile[Int](max = 2 seconds, idle = 500 millis, messages = 10) {
          case WorkResult(result) => result
        }

        assert(result.sum > 5)
      }
    }

    "reply to TestProbe in a timely manner" in  {
       within(1 second) {
         val probe = TestProbe()
         probe.send(workerActor, "work")
         probe.expectMsg(WorkResult(42)) //timeout of 0.3 seconds
       }
    }
  }
}

object TimedAssertionSpec {

  case class WorkResult(result: Int)

  class WorkerActor extends Actor {
    override def receive: Receive = {
      case "work" =>
        //long computation
        Thread.sleep(500)
        sender() ! WorkResult(42)

      case "workSequence" =>
        val r = new Random()
        for( _ <- 1 to 10) {
          Thread.sleep(r.nextInt(50))
          sender() ! WorkResult(1)
        }
    }
  }
}

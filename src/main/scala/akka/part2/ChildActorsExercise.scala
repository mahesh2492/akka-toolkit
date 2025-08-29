package akka.part2

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.part2.ChildActorsExercise.WordCountMaster.InitializeChildren

object ChildActorsExercise extends App {

 // Distributed word counting

  object WordCountMaster {
    case class InitializeChildren(nChildren: Int)
    case class WordCountTask(id: Int, text: String)
    case class WordCountReply(id: Int, count: Int)
  }
  class WordCountMaster extends Actor {
    import WordCountMaster._
    override def receive: Receive = {
      case InitializeChildren(nChildren) =>
        println("[master] initializing..")
        val workers = (1 to nChildren).map { n =>
          context.actorOf(Props[WordCountWorker], s"worker-$n")
        }
        context.become(withChildren(workers, 0, 0, Map()))
    }

    def withChildren(childrenRefs: Seq[ActorRef], currentChildIndex: Int, currentTaskId: Int, requestMap: Map[Int, ActorRef]): Receive = {
      case text: String =>
        println(s"[master] i have received: $text - i will send it to $currentChildIndex")
        val originalSender = sender()
        val task = WordCountTask(currentTaskId, text)
        val childRef = childrenRefs(currentChildIndex)
        childRef ! task
        val nextChildIndex = (currentChildIndex + 1) % childrenRefs.length
        val newTaskId = currentTaskId + 1
        val newRequestMap = requestMap + (currentTaskId -> originalSender)
        context.become(withChildren(childrenRefs, nextChildIndex, newTaskId, newRequestMap))
      case WordCountReply(id, count) =>
        val originalSender = requestMap(id)
        originalSender ! count
        context.become(withChildren(childrenRefs, currentChildIndex, currentTaskId, requestMap - id))
    }
  }

  class WordCountWorker extends Actor {
    import WordCountMaster._
    override def receive: Receive = {
      case WordCountTask(id, text) =>
        println(s"${self.path} I have received task $id with $text")
        val count = text.split(" ").length
        sender ! WordCountReply(id, count)
    }
  }

  class TestActor extends Actor {
    override def receive: Receive = {
      case "go" =>
        val master = context.actorOf(Props[WordCountMaster], "master")
        master ! InitializeChildren(2)
        val texts = List("I love akka", "Everything you can imagine is real", "It is nice evening.")
        texts.foreach(text => master ! text)
      case count: Int =>
        println(s"[test actor] I received a reply: $count")

    }
  }

  val system = ActorSystem("childactorsexercise")
  val testActor = system.actorOf(Props[TestActor], "testActor")

  testActor ! "go"
}

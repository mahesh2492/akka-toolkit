package akka.part2

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.part2.ChangingActorBehaviour.FussyKid.{KidAccept, KidReject}
import akka.part2.ChangingActorBehaviour.Mom.MomStart

object ChangingActorBehaviour extends App {

  object FussyKid {
    case object KidAccept
    case object KidReject
    val HAPPY = "happy"
    val SAD = "sad"
  }

  class FussyKid extends Actor {
    import FussyKid._
    import Mom._
    var state = HAPPY
    override def receive: Receive = {
      case Food(VEGETABLE) => state = SAD
      case Food(CHOCOLATE) => state = HAPPY
      case Ask(message) =>
        if(state == HAPPY)
          sender() ! KidAccept
        else
          sender() ! KidReject
    }
  }

  class StatelessFussyKid extends Actor {
    import FussyKid._
    import Mom._
    override def receive: Receive = happyReceive
    def happyReceive: Receive = {
      case Food(CHOCOLATE) =>
      case Food(VEGETABLE) => context.become(sadReceive)
      case Ask(_) =>
        sender() ! KidAccept
    }
    def sadReceive: Receive = {
      case Food(CHOCOLATE) => context.become(happyReceive)
      case Food(VEGETABLE) =>
      case Ask(_) =>
        sender() ! KidReject
    }
  }



  object Mom {
    case class MomStart(kidRef: ActorRef)
    case class Food(food: String)
    case class Ask(message: String)
    val VEGETABLE = "vegetable"
    val CHOCOLATE = "chocolate"
  }

  class Mom extends Actor {
    import Mom._
    override def receive: Receive = {
      case MomStart(kidRef) =>
        kidRef ! Food(VEGETABLE)
        kidRef ! Ask("do you want to play?")
      case KidAccept => println("Yay! my kid is happy.")
      case KidReject => println("My Kid is sad but at least he is happy.")
    }
  }

  val system = ActorSystem("changingActorBehaviour")
  //val fussyKid = system.actorOf(Props[FussyKid], "fussyKid")
  val statelessFussyKid = system.actorOf(Props[StatelessFussyKid], "statelessFussyKid")
  val mom = system.actorOf(Props[Mom], "mom")

  mom ! MomStart(statelessFussyKid)

}


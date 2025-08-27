package akka.part2

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.part2.ChangingActorBehaviourExercises.StatelessCounter.{DECREMENT, INCREMENT, PRINT}

object ChangingActorBehaviourExercises extends App {
  object StatelessCounter {
    case object INCREMENT
    case object DECREMENT
    case object PRINT
  }
  class StatelessCounter extends Actor {
    import StatelessCounter._
    override def receive: Receive = countReceive(0)
    def countReceive(currentCount: Int):Receive = {
      case INCREMENT => context.become(countReceive(currentCount + 1))
      case DECREMENT => context.become(countReceive(currentCount - 1))
      case PRINT => println(s"counter: ${currentCount}")
    }
  }

  val system = ActorSystem("StatelessCounter")
  val counterActor = system.actorOf(Props[StatelessCounter], "counter")
  (1 to 5).foreach(_ => counterActor ! INCREMENT)
  (1 to 3).foreach(_ => counterActor ! DECREMENT)
  counterActor ! PRINT

  /**
   *  Exercise 2 - a simplified voting system
   */

  case class Vote(candidate: String)
  case object VoteStatusRequest
  case class VoteStatusReply(candidate: Option[String])

  class Citizen extends Actor {
    override def receive: Receive = {
      case Vote(c) => context.become(voted(c))
      case VoteStatusRequest => sender() ! VoteStatusReply(None)
    }

    def voted(candidate: String): Receive = {
      case VoteStatusRequest => sender() ! VoteStatusReply(Some(candidate))
    }
  }

  case class AggregateVotes(citizens: Set[ActorRef])
  class VoteAggregator extends Actor {

    override def receive: Receive = awaitingCommand

    def awaitingCommand: Receive = {
      case AggregateVotes(citizens) =>
        citizens.foreach(citizenRef => citizenRef ! VoteStatusRequest)
        context.become(awaitingStatuses(citizens, Map()))
    }

    def awaitingStatuses(stillWaiting: Set[ActorRef], currentStats: Map[String, Int]): Receive = {
      case VoteStatusReply(None) => sender() ! VoteStatusRequest
      case VoteStatusReply(Some(candidate)) =>
        val newStillWaiting = stillWaiting - sender()
        val currentVotesOfCandidate = currentStats.getOrElse(candidate, 0)
        val newStats = currentStats + (candidate -> (currentVotesOfCandidate + 1))
        if(newStillWaiting.isEmpty) {
          println(s"[aggregator] poll stats: $newStats")
        } else {
          context.become(awaitingStatuses(newStillWaiting, newStats))
        }
    }
  }

  val alice = system.actorOf(Props[Citizen], "alice")
  val bob = system.actorOf(Props[Citizen], "bob")
  val charlie = system.actorOf(Props[Citizen], "charlie")
  val daniel = system.actorOf(Props[Citizen], "daniel")

  alice   ! Vote("Martin")
  bob     ! Vote("Jonas")
  charlie ! Vote("Roland")
  daniel  ! Vote("Roland")

  val voteAggregator = system.actorOf(Props[VoteAggregator], "voteAggregator")
  voteAggregator ! AggregateVotes(Set(alice, bob, charlie, daniel))
  /*
      Print the status of the votes
      Martin -> 1
      Jonas -> 1
      Roland -> 2
   */
}


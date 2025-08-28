package akka.part2

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.part2.ChildActor.Parent.{CreateChild, TellChild}

object ChildActor extends App {

  object Parent {
    case class CreateChild(name: String)
    case class TellChild(message: String)
  }

  class Parent extends Actor {
    override def receive: Receive = {
      case CreateChild(name) =>
        println(s"${self.path} creating child")
        val childRef = context.actorOf(Props[Child], name)
        context.become(withChild(childRef))
    }

    def withChild(childRef: ActorRef): Receive = {
      case TellChild(message) => childRef forward message
    }
  }

  class Child extends Actor {
    override def receive: Receive = {
      case message: String => println(s"${self.path} I god: $message")
    }
  }

  val system = ActorSystem("ChildActors")
  val parent = system.actorOf(Props[Parent], "parent")

  parent ! CreateChild("child")
  parent ! TellChild("Hey Kid")

  /*
     Actor Selection
   */
  val childSelection = system.actorSelection("/user/parent/child")
  childSelection ! "I found you."

  /**
   * DANGER
   * Never pass mutable actor state, or 'this' reference to child actors.
   */
}

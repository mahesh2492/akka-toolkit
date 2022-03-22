package playground

import akka.actor.{Actor, ActorSystem, Props}
import Counter._

object Counter {
  trait Message
  case object Increment extends Message
  case object Decrement extends Message
  case object Print extends Message
}


class Counter extends Actor {
  import Counter._

  var count = 0
  override def receive: Receive = {
    case Increment => count = count + 1
    case Decrement => count = count - 1
    case Print => println(s"Value of counter: $count")
  }

  val system = ActorSystem("Akka-Counter")
  val counterActor = system.actorOf(Props[Counter], "counter")
  counterActor ! Print
  counterActor ! Increment
  counterActor ! Increment
  counterActor ! Print
  counterActor ! Decrement
  counterActor ! Print
}

/*
object Counter extends App {
  val system = ActorSystem("Akka-Counter")
  val counterActor = system.actorOf(Props[Counter], "counter")
  import Counter._

  counterActor ! Print
  counterActor ! Increment
  counterActor ! Increment
  counterActor ! Print
  counterActor ! Decrement
  counterActor ! Print

  system.terminate()
}*/

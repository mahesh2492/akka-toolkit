import akka.actor.{Actor, ActorSystem, Props}

object HelloAkka extends App {

  class Actors extends Actor {
    override def receive: Receive = {
      case message: String => println(s"I got the $message")
    }
  }

  val system = ActorSystem("HelloAkka")
  val actor = system.actorOf(Props[Actors], "hello")

  actor ! "Hey, how are you?"
}



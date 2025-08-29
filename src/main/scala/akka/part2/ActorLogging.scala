package akka.part2

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging

object ActorLogging extends App {

  class SimpleActorWithExplicitLogger extends Actor {
    val logger = Logging(context.system, this)
    override def receive: Receive = {
      /*
         1 - DEBUG
         2 - INFO
         3 - WARNING/WARN
         4 - ERROR
       */
      case message: String => logger.info(message.toString) //Log it
    }
  }

  val system = ActorSystem("LoggingDemo")
  val actor = system.actorOf(Props[SimpleActorWithExplicitLogger])

  actor ! "Logging a simple message"

  // #2 - ActorLogging
  class ActorWithLogging extends Actor with ActorLogging {
    override def receive: Receive = {
      case (a, b) => log.info("Two things: {} and {}", a, b)
      case message => log.info(message.toString)
    }
  }

  val simplerActor = system.actorOf(Props[ActorWithLogging])
  simplerActor ! "Logging using ActorLogging trait"
  simplerActor ! (2, 5)
}

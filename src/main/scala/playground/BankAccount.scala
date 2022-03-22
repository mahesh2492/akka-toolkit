package playground

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

case class Deposit(amount: Int)
case class WithDraw(amount: Int)
case object Statement

case class TransactionSuccess(message: String)
case class TransactionFailure(message: String)

class BankAccount extends Actor {

  var balance = 0

  override def receive: Receive = {
    case Deposit(money) =>
      if(money < 0)
       sender ! TransactionFailure("Invalid deposit amount")
      else {
        balance += money
        sender ! TransactionSuccess(s"Successfully deposited, total balance: $balance")
      }
    case WithDraw(money) =>
      if(balance == 0)
        sender ! TransactionFailure("There is no balance")
      else if (money > balance)
        sender() ! TransactionFailure("Insufficient funds")
      else
        {
          balance -= money
          sender ! TransactionSuccess(s"Successfully withdrawn, total balance: $balance")
        }

    case Statement => sender() ! s"Your balance is $balance"
  }
}

case class LiveTheLife(account: ActorRef)

class Person extends Actor {
  override def receive: Receive = {
    case LiveTheLife(account) =>
      account ! Deposit(1000)
      account ! WithDraw(9000)
      account ! WithDraw(500)
      account ! Statement
    case message => println(message.toString)
  }
}

object BankAccount extends App {
  val system = ActorSystem("Akka-Counter")
  val bankActor = system.actorOf(Props[BankAccount], "bank")
  implicit val timeout = Timeout(3.seconds)

  /*bankActor.ask(WithDraw(100)).map { res =>
    println(s"Response- $res")
  }

  bankActor.ask(Deposit(100)).map { res =>
    println(s"Response- $res")
  }

  bankActor.ask(WithDraw(30)).map { res =>
    println(s"Response- $res")
  }
*/

  val person = system.actorOf(Props[Person], "person")

  person ! LiveTheLife(bankActor)


  Thread.sleep(10000)
  system.terminate()
}

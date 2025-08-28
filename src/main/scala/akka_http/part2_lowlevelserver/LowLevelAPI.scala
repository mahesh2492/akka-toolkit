package akka_http.part2_lowlevelserver

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.IncomingConnection
import akka.http.scaladsl.model.headers.HttpEncoding
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCode, StatusCodes, Uri}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object LowLevelAPI extends App {

  implicit val system = ActorSystem("LowLevelServerAPI")
  import system.dispatcher

  val serverSource = Http().newServerAt("localhost", 9000).connectionSource()
  val connectionSink = Sink.foreach[IncomingConnection] { connection =>
    println(s"Accepted incoming connections from ${connection.remoteAddress}")
  }

  val serverBindingFuture = serverSource.to(connectionSink).run
  serverBindingFuture.onComplete {
    case Failure(exception) => println(s"Server binding failed: $exception")
    case Success(_) => println("Server binding successful.")
  }

  /*
     Method 1: synchronously server http response
   */

  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(HttpMethods.GET, _, _, _, _) =>
    HttpResponse(
      StatusCodes.OK,
      entity = HttpEntity(
        ContentTypes.`text/html(UTF-8)`,
        """
          |<html>
          | <body>
          |   Hello from Akka Http
          | </body>
          |</html>
          |""".stripMargin
      )
    )
    case request: HttpRequest =>
      request.discardEntityBytes()
      HttpResponse(
        StatusCodes.NotFound,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            | <body>
            |   OOPS! The resource can't be found.
            | </body>
            |</html>
            |""".stripMargin
        )
      )
  }

  Http().newServerAt("localhost", 8080).bindSync(requestHandler)

  /*
     Method 2 - serve back http response asynchronously
   */

  val asyncRequestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(HttpMethods.GET, Uri.Path("/home"), _, _, _) =>
     Future(HttpResponse(
        StatusCodes.OK,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            | <body>
            |   Hello from Akka Http
            | </body>
            |</html>
            |""".stripMargin
        )
      )
     )
    case request: HttpRequest =>
      request.discardEntityBytes()
      Future(HttpResponse(
        StatusCodes.NotFound,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            | <body>
            |   OOPS! The resource can't be found.
            | </body>
            |</html>
            |""".stripMargin
        )
        )
      )
  }

  Http().newServerAt("localhost", 8000).bind(asyncRequestHandler)

  val streamBasedRequestHandler: Flow[HttpRequest,HttpResponse, _] = Flow[HttpRequest].map {
    case HttpRequest(HttpMethods.GET, Uri.Path("/home"), _, _, _) =>
      HttpResponse(
        StatusCodes.OK,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            | <body>
            |   Hello from Akka Http
            | </body>
            |</html>
            |""".stripMargin
        )
      )
    case request: HttpRequest =>
      request.discardEntityBytes()
      HttpResponse(
        StatusCodes.NotFound,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            | <body>
            |   OOPS! The resource can't be found.
            | </body>
            |</html>
            |""".stripMargin
        )
      )
  }

  Http().newServerAt("localhost", 8082).bindFlow(streamBasedRequestHandler)
}

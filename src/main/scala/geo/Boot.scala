package geo

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import geo.route.CustomExceptionHandler._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

object Boot extends App {

  implicit val system: ActorSystem = ActorSystem.create("geo-service")
  implicit val materializer: ActorMaterializer = ActorMaterializer.create(system)
  implicit val dispatcher: ExecutionContext = system.dispatcher

  val router: Route = handleExceptions(exceptionHandler) {
    path("hello") {
      complete("world")
    }
  }

  val bindingFuture = Http().bindAndHandle(router, "localhost", 9090)

  Await.ready(system.whenTerminated, Duration.Inf)
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ ⇒ system.terminate())
}

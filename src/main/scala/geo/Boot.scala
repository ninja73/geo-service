package geo

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import geo.config.Config
import geo.entity.Entity._
import geo.route.CustomExceptionHandler._
import geo.route.GeoRoute
import geo.store.InMemoryStorage
import geo.util._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

/** Пример запуска проекта:
  * sbt "run
  * -u ./src/main/resources/user_labels.txt
  * -g ./src/main/resources/user_labels.txt
  * */
object Boot extends App with LazyLogging {

  ConfigParser.parser.parse(args, CmdConfig()) match {
    case Some(config) ⇒
      implicit val system: ActorSystem = ActorSystem.create("geo-service")
      implicit val materializer: ActorMaterializer = ActorMaterializer.create(system)
      implicit val dispatcher: ExecutionContext = system.dispatcher

      val userLabelLoader = new LoadData[LocationTag](config.userLabelsPath)
        with LocationTagTransform with LocationTagParser
      val gridLoader = new LoadData[Grid](config.gridPath)
        with GridTransform with GridParser

      val userStorage = InMemoryStorage[Long, LocationTag]()
      val gridStorage = InMemoryStorage[GridId, Grid]()

      val bindingFuture = for {
        _ ← userLabelLoader.load(tag ⇒ userStorage.update(tag.userId, tag))
        _ ← gridLoader.load(g ⇒ gridStorage.update(g.id, g))
        router = handleExceptions(exceptionHandler) {
          GeoRoute(userStorage, gridStorage).route
        }
        bind ← Http().bindAndHandle(router, Config.webServer.host, Config.webServer.port)
      } yield {
        logger.info(s"Started host: ${Config.webServer.host}, port: ${Config.webServer.port}")
        bind
      }

      Await.ready(system.whenTerminated, Duration.Inf)
      bindingFuture
        .flatMap(_.unbind())
        .onComplete(_ ⇒ system.terminate())
    case None ⇒
      logger.info("Parse args failed")
  }

}

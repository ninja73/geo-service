package geo

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import geo.actor.StorageActor
import geo.config.Config
import geo.entity.Entity._
import geo.route.CustomExceptionHandler._
import geo.route.GeoRoute
import geo.store.InMemoryStorage
import geo.util.GridStoreSupport._
import geo.util._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

/** Пример запуска проекта:
  * sbt "run
  * -u ./src/main/resources/user_labels.txt
  * -g ./src/main/resources/grid.txt
  * */
object Boot extends App with LazyLogging {

  ConfigParser.parser.parse(args, CmdConfig()) match {
    case Some(config) ⇒
      implicit val system: ActorSystem = ActorSystem.create("geo-service")
      implicit val materializer: ActorMaterializer = ActorMaterializer.create(system)
      implicit val dispatcher: ExecutionContext = system.dispatcher

      val userMarkLoader = new LoadData[UserMarker](config.userMarkersPath) with MarkerParser
      val gridLoader = new LoadData[GridPoint](config.gridPath) with GridParser

      val userMarkStorage = InMemoryStorage.create[Long, UserMarker]
      val gridStorage = InMemoryStorage.create[PointId, GridPoint]

      val init = for {
        _ ← gridLoader.load(g ⇒ gridStorage.update(g.id, g))
        _ ← userMarkLoader.load(tag ⇒
          userMarkStorage
            .update(tag.userId, tag)
            .flatMap(marker ⇒ gridStorage.incrementPoint(marker)))
        storageActor = system.actorOf(StorageActor.props(userMarkStorage, gridStorage))
        router = handleExceptions(exceptionHandler) (GeoRoute(storageActor).route)
        bind ← Http().bindAndHandle(router, Config.webServer.host, Config.webServer.port)
      } yield {
        logger.info(s"Started host: ${Config.webServer.host}, port: ${Config.webServer.port}")
        bind
      }

      Await.ready(system.whenTerminated, Duration.Inf)

      init
        .flatMap(_.unbind())
        .onComplete(_ ⇒ system.terminate())

    case None ⇒
      logger.info("Parse args failed")
  }

}

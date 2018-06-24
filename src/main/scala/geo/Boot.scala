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
import geo.store.{InMemoryStorage, SaveSnapshot}
import geo.util._
import geo.util.GridStoreSupport._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

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

      val userMarkLoader = new LoadData[UserMarker](config.userMarkersPath) with MarkerTransform with MarkerParser
      val gridLoader = new LoadData[GridPoint](config.gridPath) with GridTransform with GridParser

      val userMarkStorage = InMemoryStorage.create[Long, UserMarker]
      val gridStorage = InMemoryStorage.create[PointId, GridPoint]

      val init = for {
        _ ← gridLoader.load(g ⇒ gridStorage.update(g.id, g))
        _ ← userMarkLoader.load(tag ⇒
          userMarkStorage.update(tag.userId, tag).flatMap(marker ⇒
            marker.map(gridStorage.incrementPoint)
              .getOrElse(Future.successful(None))
          ))
        router = handleExceptions(exceptionHandler) {
          GeoRoute(userMarkStorage, gridStorage).route
        }
        bind ← Http().bindAndHandle(router, Config.webServer.host, Config.webServer.port)
      } yield {
        gridStorage.getStore.take(100).foreach(p ⇒ println(p.markerCount))
        logger.info(s"Started host: ${Config.webServer.host}, port: ${Config.webServer.port}")
        bind
      }

      system.actorOf(SaveSnapshot.props(userMarkStorage, gridStorage, userMarkLoader, gridLoader))
      Await.ready(system.whenTerminated, Duration.Inf)

      init.flatMap(_.unbind()).onComplete(_ ⇒ system.terminate())

    case None ⇒
      logger.info("Parse args failed")
  }

}

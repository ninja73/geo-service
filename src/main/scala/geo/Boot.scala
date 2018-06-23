package geo

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import geo.Message.{CellSearchException, Config, GridCell, LocationTag}
import geo.route.CustomExceptionHandler._
import geo.route.GeoRoute
import geo.store.Storage
import geo.util.{GridCellParser, GridCellTransform, LocationTagParser, LocationTagTransform}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

/** Пример запуска проекта:
  * sbt "run
  *     -u ./src/main/resources/user_labels.txt
  *     -g ./src/main/resources/user_labels.txt
  *     -c 4421353 --min-tile-x -30 --min-tile-y -90 --max-tile-x 30 --max-tile-y 90"
  * */
object Boot extends App with LazyLogging {

  ConfigParser.parser.parse(args, Config()) match {
    case Some(config) ⇒
      implicit val system: ActorSystem = ActorSystem.create("geo-service")
      implicit val materializer: ActorMaterializer = ActorMaterializer.create(system)
      implicit val dispatcher: ExecutionContext = system.dispatcher

      val calculateUserLabelIndex: Int ⇒ Int = i ⇒ i - 1

      val calculateGridIndex: ((Int, Int)) ⇒ Int = tile ⇒ {
        val (x, y) = tile
        if (x < config.gridMinTileX || x > config.gridMaxTileX || y < config.gridMinTileY || y > config.gridMaxTileY)
          throw CellSearchException()
        else
          (x - config.gridMinTileX) * (config.gridMinTileY.abs + config.gridMaxTileY) + (y - config.gridMinTileY)
      }

      val userLabelStore = new Storage[LocationTag, Int](
        fileName = config.userLabelsPath,
        chunkSize = 99,
        lastIndex = config.userLabelCount,
        calculateIndex = calculateUserLabelIndex)
        with LocationTagTransform with LocationTagParser

      val geographicGrid = new Storage[GridCell, (Int, Int)](
        fileName = config.gridPath,
        chunkSize = 132,
        lastIndex = 0,
        calculateIndex = calculateGridIndex)
        with GridCellTransform with GridCellParser

      val router: Route = handleExceptions(exceptionHandler) {
        GeoRoute(userLabelStore, geographicGrid).route
      }

      val bindingFuture = Http().bindAndHandle(router, "localhost", 9090)

      Await.ready(system.whenTerminated, Duration.Inf)
      bindingFuture
        .flatMap(_.unbind())
        .onComplete(_ ⇒ system.terminate())
    case None ⇒
      logger.info("Parse args failed")
  }

}

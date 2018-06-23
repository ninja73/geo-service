package geo.route

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.IOResult
import com.typesafe.scalalogging.LazyLogging
import geo.JsonSupport
import geo.Message._
import geo.store.Storage
import geo.util.Distance.calculate

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

class GeoRoute(userLabelStore: Storage[LocationTag, Int], geographicGrid: Storage[GridCell, (Int, Int)])
              (implicit actorSystem: ActorSystem)
  extends Directives with JsonSupport with LazyLogging {

  implicit val dispatcher: ExecutionContextExecutor = actorSystem.dispatcher

  def extractIOResult(ioResult: IOResult): UpdateDeleteStatus = {
    ioResult.status match {
      case Success(_) ⇒
        UpdateDeleteStatus(true)
      case Failure(e) ⇒
        logger.error(e.getMessage, e)
        UpdateDeleteStatus(false)
    }
  }

  val route: Route =
    path("find" / "distance") {
      post {
        entity(as[UserLocation]) { currentLocation ⇒
          val response = userLabelStore.readRow(currentLocation.userId).flatMap {
            case Some(label) ⇒
              val distance = calculate(currentLocation, label)
              val gridIndex = (label.lon.toInt, label.lat.toInt)
              geographicGrid.readRow(gridIndex).map {
                case Some(gridCell) ⇒
                  if (gridCell.distanceError < distance)
                    "вдали от метки"
                  else "рядом с меткой"
                case None ⇒
                  throw CellSearchException()
              }
            case None ⇒ Future.failed(LabelSearchException())
          }
          onComplete(response) {
            case Success(result) ⇒
              complete(LabelResponse(result))
            case Failure(e) ⇒
              complete(e.getMessage)
          }
        }
      }
    } ~ path("label" / "insert") {
      post {
        entity(as[LocationTag]) { locationTag ⇒
          val response = userLabelStore
            .insertRow(id ⇒ locationTag.copy(userId = Some(id)))
            .map(extractIOResult)

          onComplete(response) {
            case Success(r) ⇒ complete(r)
            case Failure(e) ⇒ complete(e.getMessage)
          }
        }
      }
    } ~ path("label" / "update") {
      post {
        entity(as[LocationTag]) { locationTag ⇒
          val response = locationTag.userId
            .map(id ⇒ userLabelStore.updateRow(id, locationTag).map(extractIOResult))
            .getOrElse(Future.failed(LabelSearchException()))

          onComplete(response) {
            case Success(r) ⇒ complete(r)
            case Failure(e) ⇒ complete(e.getMessage)
          }
        }
      }
    } ~ path("label" / "delete") {
      post {
        entity(as[LocationTag]) { locationTag ⇒
          val response = locationTag.userId
            .map(id ⇒ userLabelStore.deleteRow(id).map(extractIOResult))
            .getOrElse(Future.failed(LabelSearchException()))

          onComplete(response) {
            case Success(r) ⇒ complete(r)
            case Failure(e) ⇒ complete(e.getMessage)
          }
        }
      }
    } ~ path("cell" / "stats") {
      post {
        entity(as[GetStatistics]) { getStatistics ⇒
          val gridIndex = (getStatistics.lon.toInt, getStatistics.lat.toInt)
          val response = geographicGrid.readRow(gridIndex).flatMap {
            case Some(gridCell) ⇒
              userLabelStore.searchByValue { tag ⇒
                tag.lon.toInt == gridCell.tileX && tag.lat.toInt == gridCell.tileY
              }.map { i ⇒ StatisticsResponse(i) }
            case None ⇒
              Future.failed(CellSearchException())
          }
          onComplete(response) {
            case Success(r) ⇒ complete(r)
            case Failure(e) ⇒ complete(e.getMessage)
          }
        }
      }
    }
}

object GeoRoute {
  def apply(userLabelStore: Storage[LocationTag, Int], geographicGrid: Storage[GridCell, (Int, Int)])
           (implicit system: ActorSystem): GeoRoute =
    new GeoRoute(userLabelStore, geographicGrid)
}
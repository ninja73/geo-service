package geo.route

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{Directives, Route}
import com.typesafe.scalalogging.LazyLogging
import geo.entity.Entity._
import geo.store.InMemoryStorage
import geo.util.Distance.calculate

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.implicitConversions
import scala.util.{Failure, Success}
import geo.util.GridStoreSupport._

class GeoRoute(userMarkersStorage: InMemoryStorage[Long, UserMarker], gridStorage: InMemoryStorage[PointId, GridPoint])
              (implicit actorSystem: ActorSystem)
  extends Directives with JsonSupport with LazyLogging {

  implicit val dispatcher: ExecutionContextExecutor = actorSystem.dispatcher

  val route: Route =
    path("find" / "marker") {
      parameters('userId.as[Long], 'lon.as[Float], 'lat.as[Float]) { (userId, lon, lat) ⇒
        val currentLocation = UserMarker(userId, lon, lat)
        val response = userMarkersStorage.get(currentLocation.userId).flatMap {
          case Some(label) ⇒
            val distance = calculate(currentLocation, label)
            val pointId = PointId(label.lon, label.lat)
            gridStorage.get(pointId).map {
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
    } ~ path("marker" / "insert") {
      post {
        entity(as[UserMarker]) { locationTag ⇒
          val response = gridStorage.incrementPoint(locationTag)
            .flatMap(_ ⇒ userMarkersStorage.update(locationTag.userId, locationTag))
            .map(_.isDefined)
            .map(UpdateDeleteStatus)
          onComplete(response) {
            case Success(r) ⇒ complete(r)
            case Failure(e) ⇒ complete(e.getMessage)
          }
        }
      }
    } ~ path("marker" / "update") {
      put {
        entity(as[UserMarker]) { userMarker ⇒
          val response = for {
            oldUserMarker ← userMarkersStorage.get(userMarker.userId)
            _ ← gridStorage.decrementPoint(oldUserMarker)
            _ ← gridStorage.incrementPoint(userMarker)
            newUserMarker ← userMarkersStorage.update(userMarker.userId, userMarker)
          } yield UpdateDeleteStatus(newUserMarker.isDefined)

          onComplete(response) {
            case Success(r) ⇒ complete(r)
            case Failure(e) ⇒ complete(e.getMessage)
          }
        }
      }
    } ~ path("marker" / "delete") {
      delete {
        entity(as[UserMarker]) { locationTag ⇒
          val response = gridStorage.decrementPoint(Some(locationTag))
            .flatMap(_ ⇒ userMarkersStorage.delete(locationTag.userId))
            .map(_.isDefined)
            .map(UpdateDeleteStatus)
          onComplete(response) {
            case Success(r) ⇒ complete(r)
            case Failure(e) ⇒ complete(e.getMessage)
          }
        }
      }
    } ~ path("point" / "info") {
      parameters('lon.as[Int], 'lat.as[Int]) { (lon, lat) ⇒
        val pointId = PointId(lon, lat)
        val response = gridStorage.get(pointId).collect {
          case Some(point) ⇒ StatisticsResponse(point.markerCount)
        }
        onComplete(response) {
          case Success(r) ⇒ complete(r)
          case Failure(e) ⇒ complete(e.getMessage)
        }
      }
    }

}

object GeoRoute {
  def apply(userStorage: InMemoryStorage[Long, UserMarker], gridStorage: InMemoryStorage[PointId, GridPoint])
           (implicit system: ActorSystem): GeoRoute =
    new GeoRoute(userStorage, gridStorage)
}
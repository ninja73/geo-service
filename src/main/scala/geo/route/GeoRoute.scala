package geo.route

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{Directives, Route}
import com.typesafe.scalalogging.LazyLogging
import geo.entity.Entity._
import geo.store.InMemoryStorage
import geo.util.Distance.calculate

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

class GeoRoute(userStorage: InMemoryStorage[Long, LocationTag], gridStorage: InMemoryStorage[GridId, GridCell])
              (implicit actorSystem: ActorSystem)
  extends Directives with JsonSupport with LazyLogging {

  implicit val dispatcher: ExecutionContextExecutor = actorSystem.dispatcher

  val route: Route =
    path("find" / "position") {
      parameters('userId.as[Long], 'lon.as[Float], 'lat.as[Float]) { (userId, lon, lat) ⇒
        val currentLocation = LocationTag(userId, lon, lat)
        val response = userStorage.get(currentLocation.userId).flatMap {
          case Some(label) ⇒
            val distance = calculate(currentLocation, label)
            println("!!" + distance)
            val gridId = GridId(label.lon.toInt, label.lat.toInt)
            gridStorage.get(gridId).map {
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
    } ~ path("label" / "insert") {
      post {
        entity(as[LocationTag]) { locationTag ⇒
          val response = userStorage
            .update(locationTag.userId, locationTag)
          onComplete(response) {
            case Success(r) ⇒ complete(r)
            case Failure(e) ⇒ complete(e.getMessage)
          }
        }
      }
    } ~ path("label" / "update") {
      put {
        entity(as[LocationTag]) { locationTag ⇒
          val response = userStorage.update(locationTag.userId, locationTag)
          onComplete(response) {
            case Success(r) ⇒ complete(r)
            case Failure(e) ⇒ complete(e.getMessage)
          }
        }
      }
    } ~ path("label" / "delete") {
      delete {
        entity(as[LocationTag]) { locationTag ⇒
          val response = userStorage.delete(locationTag.userId)
          onComplete(response) {
            case Success(r) ⇒ complete(r)
            case Failure(e) ⇒ complete(e.getMessage)
          }
        }
      }
    } ~ path("cell" / "stats") {
      parameters('lon.as[Int], 'lat.as[Int]) { (lon, lat) ⇒
        val gridId = GridId(lon, lat)
        val response = gridStorage.get(gridId).flatMap {
          case Some(_) ⇒
            userStorage.getStatistics { tag ⇒
              tag.lon.toInt == gridId.lon && tag.lat.toInt == gridId.lat
            }.map(count ⇒ StatisticsResponse(count))
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

object GeoRoute {
  def apply(userStorage: InMemoryStorage[Long, LocationTag], gridStorage: InMemoryStorage[GridId, GridCell])
           (implicit system: ActorSystem): GeoRoute =
    new GeoRoute(userStorage, gridStorage)
}
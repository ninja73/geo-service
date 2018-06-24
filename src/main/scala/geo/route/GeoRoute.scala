package geo.route

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import geo.actor.StorageActor._
import geo.entity.Entity._

import scala.concurrent.ExecutionContextExecutor
import scala.language.implicitConversions
import scala.util.{Failure, Success}
import scala.concurrent.duration._

class GeoRoute(storage: ActorRef)(implicit actorSystem: ActorSystem)
  extends Directives with JsonSupport with LazyLogging {

  implicit val dispatcher: ExecutionContextExecutor = actorSystem.dispatcher
  implicit val timeout: Timeout = Timeout(20.seconds)

  val route: Route =
    path("find" / "marker") {
      parameters('userId.as[Long], 'lon.as[Float], 'lat.as[Float]) { (userId, lon, lat) ⇒
        val currentLocation = UserMarker(userId, lon, lat)
        val response = (storage ? FindMarker(currentLocation))
          .mapTo[Option[String]]

        onComplete(response) {
          case Success(result) ⇒ complete(result)
          case Failure(e) ⇒
            complete(e.getMessage)
        }

      }
    } ~ path("marker" / "insert") {
      post {
        entity(as[UserMarker]) { marker ⇒
          val response = (storage ? AddUserMark(marker))
            .mapTo[Option[Long]]
            .map(UserMarkChange)

          onComplete(response) {
            case Success(result) ⇒ complete(result)
            case Failure(e) ⇒ complete(e.getMessage)
          }
        }
      }
    } ~ path("marker" / "update") {
      put {
        entity(as[UserMarker]) { marker ⇒
          val response = (storage ? UpdateUserMark(marker))
            .mapTo[Option[Long]]
            .map(UserMarkChange)

          onComplete(response) {
            case Success(result) ⇒ complete(result)
            case Failure(e) ⇒ complete(e.getMessage)
          }
        }
      }
    } ~ path("marker" / "delete") {
      delete {
        entity(as[UserMarker]) { marker ⇒
          val response = (storage ? DeleteUserMark(marker.userId))
            .mapTo[Option[Long]]
            .map(UserMarkChange)

          onComplete(response) {
            case Success(result) ⇒ complete(result)
            case Failure(e) ⇒ complete(e.getMessage)
          }
        }
      }
    } ~ path("point" / "info") {
      parameters('lon.as[Int], 'lat.as[Int]) { (lon, lat) ⇒
        val pointId = PointId(lon, lat)
        val response = (storage ? GetPointInf(pointId))
          .mapTo[Option[Long]]
          .map(StatisticsResponse)
        onComplete(response) {
          case Success(result) ⇒ complete(result)
          case Failure(e) ⇒ complete(e.getMessage)
        }
      }
    }

}

object GeoRoute {
  def apply(storage: ActorRef)(implicit system: ActorSystem): GeoRoute =
    new GeoRoute(storage)
}
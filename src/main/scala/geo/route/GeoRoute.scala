package geo.route

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import com.typesafe.scalalogging.LazyLogging
import geo.JsonSupport
import geo.Message.{GridCell, LocationTag, UserLocation}
import geo.store.StoreWorker

class GeoRoute(userLabelStore: StoreWorker[LocationTag, Int], geographicGrid: StoreWorker[GridCell, (Int, Int)])
              (implicit actorSystem: ActorSystem)
  extends Directives with JsonSupport with LazyLogging  {

  val route: Route = pathPrefix("find/distance") {
    entity(as[UserLocation]) { _ ⇒
      complete(StatusCodes.OK)
    }
  } ~ pathPrefix("label/update") {
    complete("")
  } ~ pathPrefix("label/delete") {
    complete("")
  } ~ pathPrefix("cell/stats") {
    complete("")
  }
}

object GeoRoute {
  def apply(userLabelStore: StoreWorker[LocationTag, Int], geographicGrid: StoreWorker[GridCell, (Int, Int)])
           (implicit system: ActorSystem): GeoRoute =
    new GeoRoute(userLabelStore, geographicGrid)
}
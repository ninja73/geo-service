package geo.util

import geo.entity.Entity.{GridPoint, PointId, UserMarker}
import geo.store.InMemoryStorage

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

object GridStoreSupport {

  implicit def convertFloatToInt(f: Float): Int = f.toInt

  implicit class GridStoreOps(gridStorage: InMemoryStorage[PointId, GridPoint]) {
    def decrementPoint(oldUserMarker: Option[UserMarker])
                      (implicit dispatcher: ExecutionContext): Future[Option[GridPoint]] = {
      oldUserMarker match {
        case Some(user) ⇒
          val oldPointId = PointId(user.lon, user.lat)
          gridStorage.updateFiled(oldPointId)(p ⇒ p.copy(markerCount = p.markerCount - 1))
        case None ⇒ Future.successful(None)
      }
    }

    def incrementPoint(newUserMarker: UserMarker)
                      (implicit dispatcher: ExecutionContext): Future[Option[GridPoint]] = {
      val newPointId = PointId(newUserMarker.lon, newUserMarker.lat)
      gridStorage.updateFiled(newPointId)(p ⇒ p.copy(markerCount = p.markerCount + 1))
    }
  }

}

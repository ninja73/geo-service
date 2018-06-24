package geo.util

import geo.entity.Entity.{GridPoint, PointId, UserMarker}
import geo.store.InMemoryStorage

import scala.language.implicitConversions

object GridStoreSupport {

  implicit def convertFloatToInt(f: Float): Int = f.toInt

  implicit class GridStoreOps(gridStorage: InMemoryStorage[PointId, GridPoint]) {
    def decrementPoint(oldUserMarker: UserMarker): Option[GridPoint] = {
      val oldPointId = PointId(oldUserMarker.lon, oldUserMarker.lat)
      gridStorage.updateFiled(oldPointId)(p ⇒ p.copy(markerCount = p.markerCount - 1))
    }

    def incrementPoint(newUserMarker: UserMarker): Option[GridPoint] = {
      val newPointId = PointId(newUserMarker.lon, newUserMarker.lat)
      gridStorage.updateFiled(newPointId)(p ⇒ p.copy(markerCount = p.markerCount + 1))
    }
  }

}

package geo.entity

sealed trait Entity

object Entity {

  case class UserMarkChange(userId: Option[Long])

  case class StatisticsResponse(countUser: Option[Long])

  case class PointId(lon: Int, lat: Int)

  case class GridPoint(id: PointId, distanceError: Float, markerCount: Int = 0) extends Entity

  case class UserMarker(userId: Long, lon: Float, lat: Float) extends Entity

  case class CmdConfig(userMarkersPath: String = "", gridPath: String = "")

}

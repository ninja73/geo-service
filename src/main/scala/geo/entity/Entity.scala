package geo.entity

sealed trait Entity

object Entity {

  case class LabelResponse(message: String)

  case class UpdateDeleteStatus(isSuccess: Boolean)

  case class GetStatistics(lon: Float, lat: Float)

  case class StatisticsResponse(countUser: Long)

  case class PointId(lon: Int, lat: Int)

  case class GridPoint(id: PointId, distanceError: Float, markerCount: Int = 0) extends Entity

  case class UserMarker(userId: Long, lon: Float, lat: Float) extends Entity

  case class CmdConfig(userMarkersPath: String = "", gridPath: String = "")

  case class LabelSearchException(message: String = "Label not found",
                                  cause: Throwable = None.orNull) extends Exception(message, cause)

  case class CellSearchException(message: String = "Cell not found",
                                  cause: Throwable = None.orNull) extends Exception(message, cause)

}

package geo.entity

sealed trait Entity

object Entity {

  case class LabelResponse(message: String)

  case class UpdateDeleteStatus(isSuccess: Boolean)

  case class GetStatistics(lon: Float, lat: Float)

  case class StatisticsResponse(countUser: Long)

  case class GridId(lon: Int, lat: Int)

  case class Grid(id: GridId, distanceError: Float) extends Entity

  case class LocationTag(userId: Long, lon: Float, lat: Float) extends Entity

  case class CmdConfig(userLabelsPath: String = "", gridPath: String = "")

  case class LabelSearchException(message: String = "Label not found",
                                  cause: Throwable = None.orNull) extends Exception(message, cause)

  case class CellSearchException(message: String = "Cell not found",
                                  cause: Throwable = None.orNull) extends Exception(message, cause)

}

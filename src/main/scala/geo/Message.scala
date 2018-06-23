package geo

sealed trait Message

object Message {

  case class LabelResponse(message: String)

  case class UpdateDeleteStatus(isSuccess: Boolean)

  case class UserLocation(userId: Int, lon: Float, lat: Float)

  case class GetStatistics(lon: Float, lat: Float)

  case class StatisticsResponse(countUser: Int)

  case class GridCell(id: Int, tileX: Int, tileY: Int, distanceError: Float) extends Message

  case class LocationTag(userId: Option[Int], lon: Float, lat: Float) extends Message

  case class Config(userLabelsPath: String = "",
                    gridPath: String = "",
                    userLabelCount: Int = 0,
                    gridMinTileX: Int = 0,
                    gridMinTileY: Int = 0,
                    gridMaxTileX: Int = 0,
                    gridMaxTileY: Int = 0)

  case class LabelSearchException(message: String = "Label not found",
                                  cause: Throwable = None.orNull) extends Exception(message, cause)

  case class CellSearchException(message: String = "Cell not found",
                                  cause: Throwable = None.orNull) extends Exception(message, cause)

}

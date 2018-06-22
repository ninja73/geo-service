package geo

sealed trait Message

object Message {
  case class UserLocation(userId: Int, lon: Float, lat: Float) extends Message
  case class GridCell(id: Int, tileX: Int, tileY: Int, distanceError: Float) extends Message
  case class LocationTag(userId: Int, lon: Float, lat: Float) extends Message
}

package geo.route

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import geo.entity.Entity._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val locationTagFormat: RootJsonFormat[UserMarker] = jsonFormat3(UserMarker)
  implicit val userMarkChangeFormat: RootJsonFormat[UserMarkChange] = jsonFormat1(UserMarkChange)
  implicit val statisticsResponseFormat: RootJsonFormat[StatisticsResponse] = jsonFormat1(StatisticsResponse)
}

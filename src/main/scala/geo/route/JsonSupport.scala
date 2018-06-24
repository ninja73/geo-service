package geo.route

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import geo.entity.Entity._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val locationTagFormat: RootJsonFormat[UserMarker] = jsonFormat3(UserMarker)
  implicit val labelResponseFormat: RootJsonFormat[LabelResponse] = jsonFormat1(LabelResponse)
  implicit val updateDeleteStatusFormat: RootJsonFormat[UpdateDeleteStatus] = jsonFormat1(UpdateDeleteStatus)
  implicit val statisticsResponseFormat: RootJsonFormat[StatisticsResponse] = jsonFormat1(StatisticsResponse)
  implicit val getStatisticsFormat: RootJsonFormat[GetStatistics] = jsonFormat2(GetStatistics)
}

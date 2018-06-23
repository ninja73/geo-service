package geo

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import geo.Message._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val userLocationFormat: RootJsonFormat[UserLocation] = jsonFormat3(UserLocation)
  implicit val locationTagFormat: RootJsonFormat[LocationTag] = jsonFormat3(LocationTag)
  implicit val labelResponseFormat: RootJsonFormat[LabelResponse] = jsonFormat1(LabelResponse)
  implicit val updateDeleteStatusFormat: RootJsonFormat[UpdateDeleteStatus] = jsonFormat1(UpdateDeleteStatus)
  implicit val statisticsResponseFormat: RootJsonFormat[StatisticsResponse] = jsonFormat1(StatisticsResponse)
  implicit val getStatisticsFormat: RootJsonFormat[GetStatistics] = jsonFormat2(GetStatistics)
}

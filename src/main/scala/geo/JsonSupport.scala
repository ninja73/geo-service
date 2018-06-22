package geo

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import geo.Message.UserLocation
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val userLocationFormat: RootJsonFormat[UserLocation] = jsonFormat3(UserLocation)
}

package geo.util

import geo.Message.{LocationTag, UserLocation}

object Distance {

  val EarthRadius = 6372795

  def calculate(userLocation: UserLocation, locationTags: LocationTag): Double = {
    val lat1 = userLocation.lat * math.Pi / 180
    val long1 = userLocation.lon * math.Pi / 180

    val lat2 = locationTags.lat * math.Pi / 180
    val long2 = locationTags.lon * math.Pi / 180

    val cl1 = math.cos(lat1)
    val cl2 = math.cos(lat2)
    val sl1 = math.sin(lat1)
    val sl2 = math.sin(lat2)
    val delta = long2 - long1

    val cDelta = math.cos(delta)
    val sDelta = math.sin(delta)

    val y = math.sqrt(math.pow(cl2 * sDelta, 2) + math.pow(cl1 * sl2 - sl1 * cl2 * cDelta, 2))
    val x = sl1 * sl2 + cl1 * cl2 * cDelta

    val ad = math.atan2(y, x)
    ad * EarthRadius
  }
}

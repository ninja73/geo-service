package geo

import akka.http.scaladsl.testkit.ScalatestRouteTest
import geo.entity.Entity._
import geo.route.{GeoRoute, JsonSupport}
import geo.store.InMemoryStorage
import org.scalatest.{Matchers, WordSpec}

class RestSpec extends WordSpec with Matchers with ScalatestRouteTest with JsonSupport{

  val userStorage: InMemoryStorage[Long, LocationTag] = InMemoryStorage[Long, LocationTag]()
  val gridStorage: InMemoryStorage[GridId, GridCell] = InMemoryStorage[GridId, GridCell]()

  val userLabels = List(
    LocationTag(1, 2.4352f, -3.2899f),
    LocationTag(2, 3.6399f, 18.0287f),
    LocationTag(3, 5.2652f, -26.4085f),
    LocationTag(4, -1.1875f, 5.2455f),
    LocationTag(5, -3.6784f, -16.9377f),
    LocationTag(6, 0.6854f, -17.9795f),
    LocationTag(7, 0.6323f, -17.4593f),
    LocationTag(8, -4.8349f, -7.7284f))

  val gridCells = List(
    GridCell(GridId(2, -3), 44),
    GridCell(GridId(3, 18), 32),
    GridCell(GridId(5, -26), 11),
    GridCell(GridId(-1, -5), 100),
    GridCell(GridId(0, -17), 2323),
    GridCell(GridId(2, -3), 444))

  userLabels.foreach(l ⇒ userStorage.update(l.userId, l))
  gridCells.foreach(l ⇒ gridStorage.update(l.id, l))

  "The service" should {

    val geoRoute = GeoRoute(userStorage, gridStorage).route

    "Get statistics" in {
      Get("/cell/stats?lon=0&lat=-17") ~> geoRoute ~> check {
        responseAs[StatisticsResponse] shouldEqual StatisticsResponse(2)
      }
    }
    "Get position status" in {
      Get("/find/position?userId=3&lon=2.698&lat=-46.0755") ~> geoRoute ~> check {
        responseAs[LabelResponse] shouldEqual LabelResponse("вдали от метки")
      }
    }
    "Get position status2" in {
      Get("/find/position?userId=3&lon=5.26522&lat=-26.40852") ~> geoRoute ~> check {
        responseAs[LabelResponse] shouldEqual LabelResponse("рядом с меткой")
      }
    }
  }
}

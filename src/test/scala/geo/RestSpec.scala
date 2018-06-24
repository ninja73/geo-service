package geo

import akka.actor.ActorRef
import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, MediaTypes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestKit
import akka.util.ByteString
import geo.actor.StorageActor
import geo.entity.Entity._
import geo.route.{GeoRoute, JsonSupport}
import geo.store.InMemoryStorage
import org.scalatest.{Matchers, WordSpec}

class RestSpec extends WordSpec with Matchers with ScalatestRouteTest with JsonSupport {

  val userStorage: InMemoryStorage[Long, UserMarker] = InMemoryStorage.create[Long, UserMarker]
  val gridStorage: InMemoryStorage[PointId, GridPoint] = InMemoryStorage.create[PointId, GridPoint]

  val userLabels = List(
    UserMarker(1, 2.4352f, -3.2899f),
    UserMarker(2, 3.6399f, 18.0287f),
    UserMarker(3, 5.2652f, -26.4085f),
    UserMarker(4, -1.1875f, 5.2455f),
    UserMarker(5, -3.6784f, -16.9377f),
    UserMarker(6, 0.6854f, -17.9795f),
    UserMarker(7, 0.6323f, -17.4593f),
    UserMarker(8, -4.8349f, -7.7284f))

  val gridCells = List(
    GridPoint(PointId(2, -3), 44, 1),
    GridPoint(PointId(3, 18), 32, 1),
    GridPoint(PointId(5, -26), 11, 1),
    GridPoint(PointId(-1, -5), 100, 1),
    GridPoint(PointId(0, -17), 2323, 2))

  userLabels.foreach(l ⇒ userStorage.update(l.userId, l))
  gridCells.foreach(l ⇒ gridStorage.update(l.id, l))

  val jsonRequest = ByteString(
    s"""
       |{
       |    "userId": 1, "lon": 2.5, "lat": -3.9
       |}
        """.stripMargin)

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "The service" should {
    val storage: ActorRef = system.actorOf(StorageActor.props(userStorage, gridStorage))
    val geoRoute = GeoRoute(storage).route

    "Get statistics" in {
      Get("/point/info?lon=0&lat=-17") ~> geoRoute ~> check {
        responseAs[StatisticsResponse] shouldEqual StatisticsResponse(Some(2))
      }
    }
    "Get position status" in {
      Get("/find/marker?userId=3&lon=2.698&lat=-46.0755") ~> geoRoute ~> check {
        responseAs[Option[String]] shouldEqual Some("вдали от метки")
      }
    }
    "Get position status2" in {
      Get("/find/marker?userId=3&lon=5.26522&lat=-26.40852") ~> geoRoute ~> check {
        responseAs[Option[String]] shouldEqual Some("рядом с меткой")
      }
    }
    "Add user marker" in {
      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/marker/insert",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest))

      postRequest ~> geoRoute ~> check {
        responseAs[UserMarkChange] shouldEqual UserMarkChange(Some(1))
        Get("/point/info?lon=2&lat=-3") ~> geoRoute ~> check {
          responseAs[StatisticsResponse] shouldEqual StatisticsResponse(Some(2))
        }
      }
    }
    "Update user marker" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |    "userId": 7, "lon": 2.5, "lat": -3.9
           |}
        """.stripMargin)
      val postRequest = HttpRequest(
        HttpMethods.PUT,
        uri = "/marker/update",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest))

      postRequest ~> geoRoute ~> check {
        responseAs[UserMarkChange] shouldEqual UserMarkChange(Some(7))
        Get("/point/info?lon=2&lat=-3") ~> geoRoute ~> check {
          responseAs[StatisticsResponse] shouldEqual StatisticsResponse(Some(3))
        }
      }
    }
    "Remove user marker" in {
      val postRequest = HttpRequest(
        HttpMethods.DELETE,
        uri = "/marker/delete",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest))

      postRequest ~> geoRoute ~> check {
        responseAs[UserMarkChange] shouldEqual UserMarkChange(Some(1))
        Get("/point/info?lon=2&lat=-3") ~> geoRoute ~> check {
          responseAs[StatisticsResponse] shouldEqual StatisticsResponse(Some(2))
        }
      }
    }
  }
}

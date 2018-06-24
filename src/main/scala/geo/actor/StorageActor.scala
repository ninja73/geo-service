package geo.actor

import akka.actor.Props
import akka.persistence.{PersistentActor, SnapshotOffer}
import geo.actor.StorageActor._
import geo.entity.Entity.{GridPoint, PointId, UserMarker}
import geo.store.InMemoryStorage
import geo.util.GridStoreSupport._
import akka.pattern.pipe
import geo.util.Distance.calculate

import scala.concurrent.{ExecutionContextExecutor, Future}


case class StorageState(userMarkStorage: InMemoryStorage[Long, UserMarker],
                        gridStorage: InMemoryStorage[PointId, GridPoint]) {

  def search(currentMarker: UserMarker): Option[String] = {
    userMarkStorage.get(currentMarker.userId).flatMap { marker ⇒
      val distance = calculate(currentMarker, marker)
      val pointId = PointId(marker.lon, marker.lat)
      gridStorage.get(pointId).map { point ⇒
        if (point.distanceError < distance)
          "вдали от метки"
        else "рядом с меткой"
      }
    }
  }

  def addMarker(marker: UserMarker): Option[Long] = {
    for {
      _ ← gridStorage.incrementPoint(marker)
      newUserMarker ← userMarkStorage.update(marker.userId, marker)
    } yield newUserMarker.userId
  }

  def updateMarker(mark: UserMarker): Option[Long] = {
    for {
      oldUserMarker ← userMarkStorage.get(mark.userId)
      _ ← gridStorage.decrementPoint(oldUserMarker)
      _ ← gridStorage.incrementPoint(mark)
      newUserMarker ← userMarkStorage.update(mark.userId, mark)
    } yield newUserMarker.userId
  }

  def deleteMarker(id: Long): Option[Long] = {
    for {
      oldUserMarker ← userMarkStorage.get(id)
      _ ← gridStorage.decrementPoint(oldUserMarker)
      deletedMark ← userMarkStorage.delete(id)
    } yield deletedMark.userId
  }

  def pointInfo(id: PointId): Option[Long] =
    gridStorage.get(id).map(_.markerCount)

}

class StorageActor(userMarkStorage: InMemoryStorage[Long, UserMarker],
                   gridStorage: InMemoryStorage[PointId, GridPoint]) extends PersistentActor {

  def persistenceId: String = "get-service"

  implicit val dispatcher: ExecutionContextExecutor = context.system.dispatcher

  val snapShotInterval = 1000

  var state = StorageState(userMarkStorage, gridStorage)

  def updateState(msg: StorageMessage): Option[Long] =
    msg match {
      case AddUserMark(marker) ⇒ state.addMarker(marker)
      case UpdateUserMark(marker) ⇒ state.updateMarker(marker)
      case DeleteUserMark(marker) ⇒ state.deleteMarker(marker)
    }

  def receiveRecover: Receive = {
    case msg: StorageMessage ⇒ updateState(msg)
    case SnapshotOffer(_, snapshot: StorageState) ⇒ state = snapshot
  }

  def receiveCommand: Receive = {
    case msg: StorageMessage ⇒
      val requester = sender()
      persistAsync(msg) { m ⇒
        if (lastSequenceNr % snapShotInterval == 0 && lastSequenceNr != 0)
          saveSnapshot(state)
        requester ! updateState(m)
      }
    case GetPointInf(pointId) ⇒
      val requester = sender()
      Future(state.pointInfo(pointId)) pipeTo requester
    case FindMarker(currentMarker) ⇒
      val requester = sender()
      Future(state.search(currentMarker)) pipeTo requester
  }
}

object StorageActor {

  sealed trait StorageMessage

  case class AddUserMark(mark: UserMarker) extends StorageMessage

  case class UpdateUserMark(mark: UserMarker) extends StorageMessage

  case class DeleteUserMark(userId: Long) extends StorageMessage

  case class GetPointInf(pointId: PointId)

  case class FindMarker(currentMarker: UserMarker)

  def props(userMarkStorage: InMemoryStorage[Long, UserMarker],
            gridStorage: InMemoryStorage[PointId, GridPoint]): Props =
    Props(new StorageActor(userMarkStorage, gridStorage))
}

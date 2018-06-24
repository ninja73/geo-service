package geo.store

import akka.actor.{Actor, Props}
import com.typesafe.scalalogging.LazyLogging
import geo.entity.Entity
import geo.entity.Entity.{GridPoint, PointId, UserMarker}
import geo.util.LoadData

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

class SaveSnapshot(userStorage: InMemoryStorage[Long, UserMarker],
                   gridStorage: InMemoryStorage[PointId, GridPoint],
                   userLoader: LoadData[UserMarker],
                   gridLoader: LoadData[GridPoint]) extends Actor with LazyLogging {

  implicit val executionContext: ExecutionContextExecutor = context.system.dispatchers.lookup("blocking-io-dispatcher")

  override def preStart(): Unit = {
    context.system.scheduler.schedule(5.second, 1.hour)(saveSnapshot(userLoader, userStorage))
    context.system.scheduler.schedule(20.second, 1.hour)(saveSnapshot(gridLoader, gridStorage))
  }

  def saveSnapshot[I, T <: Entity](loader: LoadData[T], store: InMemoryStorage[I, T]): Unit = {
    loader.save(store.getStore.take(2).to[scala.collection.immutable.Iterable])
  }

  def receive: Receive = {
    case _ ⇒ logger.error("SaveSnapshot not support receive message")
  }
}

object SaveSnapshot {

  def props(userStorage: InMemoryStorage[Long, UserMarker],
            gridStorage: InMemoryStorage[PointId, GridPoint],
            userLoader: LoadData[UserMarker],
            gridLoader: LoadData[GridPoint]): Props = Props(new SaveSnapshot(userStorage, gridStorage, userLoader, gridLoader))
}

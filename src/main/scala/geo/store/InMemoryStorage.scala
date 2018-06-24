package geo.store

import geo.entity.Entity

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, Future}

class InMemoryStorage[ID, T <: Entity] {

  private val cache: TrieMap[ID, T] = TrieMap.empty[ID, T]

  def update(id: ID, obj: T)
            (implicit dispatcher: ExecutionContext): Future[Option[T]] =
    Future(cache.put(id, obj))

  def delete(id: ID)
            (implicit dispatcher: ExecutionContext): Future[Option[T]] =
    Future(cache.remove(id))

  def get(id: ID)
         (implicit dispatcher: ExecutionContext): Future[Option[T]] =
    Future(cache.get(id))

  def getStatistics(p: T ⇒ Boolean)
                   (implicit dispatcher: ExecutionContext): Future[Long] =
    Future {
      cache.values.foldLeft(0) { case (acc, elm) ⇒
        if (p(elm))
          acc + 1
        else acc
      }
    }
}

object InMemoryStorage {
  def apply[ID, T <: Entity](): InMemoryStorage[ID, T] = new InMemoryStorage[ID, T]()
}
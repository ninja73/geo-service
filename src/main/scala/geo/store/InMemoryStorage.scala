package geo.store

import geo.entity.Entity

import scala.annotation.tailrec
import scala.collection.Iterable
import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, Future}

class InMemoryStorage[I, T <: Entity] {

  private val cache: TrieMap[I, T] = TrieMap.empty[I, T]

  def update(id: I, obj: T)
            (implicit dispatcher: ExecutionContext): Future[Option[T]] =
    Future(cache.put(id, obj))

  def delete(id: I)
            (implicit dispatcher: ExecutionContext): Future[Option[T]] =
    Future(cache.remove(id))

  def get(id: I)
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

  def getStore: Iterable[T] = cache.values

  def updateFiled(id: I)(f: T ⇒ T)
                 (implicit dispatcher: ExecutionContext): Future[Option[T]] = {
    Future {
      @tailrec
      def loop(id: I, obj: T, f: T ⇒ T): T = {
        val newObj = f(obj)

        if (cache.replace(id, obj, newObj)) newObj
        else loop(id, cache(id), f)
      }

      cache.get(id).map(old ⇒ loop(id, old, f))
    }
  }
}

object InMemoryStorage {
  def create[ID, T <: Entity]: InMemoryStorage[ID, T] = new InMemoryStorage[ID, T]()
}
package geo.store

import geo.entity.Entity

import scala.annotation.tailrec
import scala.collection.concurrent.TrieMap

class InMemoryStorage[I, T <: Entity] {

  private val cache: TrieMap[I, T] = TrieMap.empty[I, T]

  def update(id: I, obj: T): Option[T] =
    cache.put(id, obj)

  def delete(id: I): Option[T] =
    cache.remove(id)

  def get(id: I): Option[T] =
    cache.get(id)

  def updateFiled(id: I)(f: T ⇒ T): Option[T] = {
    @tailrec
    def loop(id: I, obj: T, f: T ⇒ T): T = {
      val newObj = f(obj)

      if (cache.replace(id, obj, newObj)) newObj
      else loop(id, cache(id), f)
    }

    cache.get(id).map(old ⇒ loop(id, old, f))
  }
}

object InMemoryStorage {
  def create[ID, T <: Entity]: InMemoryStorage[ID, T] = new InMemoryStorage[ID, T]()
}
package geo.util

import geo.entity.Entity.{GridPoint, UserMarker}
import geo.entity.Entity

import scala.util.parsing.combinator.RegexParsers

sealed trait Transformer[T <: Entity] {
  def toStorage(obj: T): String

  def readObj(str: String): Option[T]
}

trait MarkerTransform extends Transformer[UserMarker] {
  self: BaseParsers[UserMarker] ⇒

  def toStorage(obj: UserMarker): String =
    s"${obj.userId},${obj.lon},${obj.lat}"

  def readObj(str: String): Option[UserMarker] = parseRoot(str)
}

trait GridTransform extends Transformer[GridPoint] {
  self: BaseParsers[GridPoint] ⇒

  def toStorage(obj: GridPoint): String =
    s"${obj.id.lon},${obj.id.lat},${obj.distanceError}"

  def readObj(str: String): Option[GridPoint] = parseRoot(str)
}

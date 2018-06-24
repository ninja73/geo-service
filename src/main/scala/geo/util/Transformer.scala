package geo.util

import geo.entity.Entity.{Grid, LocationTag}
import geo.entity.Entity

import scala.util.parsing.combinator.RegexParsers

sealed trait Transformer[T <: Entity] {
  def toStorage(obj: T): String

  def readObj(str: String): Option[T]
}

trait LocationTagTransform extends Transformer[LocationTag] {
  self: BaseParsers[LocationTag] ⇒

  def toStorage(obj: LocationTag): String =
    s"${obj.userId},${obj.lon},${obj.lat}"

  def readObj(str: String): Option[LocationTag] = parseRoot(str)
}

trait GridTransform extends Transformer[Grid] {
  self: BaseParsers[Grid] ⇒

  def toStorage(obj: Grid): String =
    s"${obj.id.lon},${obj.id.lat},${obj.distanceError}"

  def readObj(str: String): Option[Grid] = parseRoot(str)
}

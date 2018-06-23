package geo.util

import geo.Message
import geo.Message.{GridCell, LocationTag}

import scala.util.parsing.combinator.RegexParsers

sealed trait Transformer[T <: Message] {
  val empty: String

  def toStorage(obj: T): String

  def readObj(str: String): Option[T]
}

trait LocationTagTransform extends Transformer[LocationTag] {
  self: BaseParsers[LocationTag] ⇒
  val empty: String = f"${""}%98s"

  def toStorage(obj: LocationTag): String =
    f"${obj.userId.getOrElse(0)}%32d,${obj.lon}%32f,${obj.lat}%32f"

  def readObj(str: String): Option[LocationTag] = parseRoot(str)
}

trait GridCellTransform extends Transformer[GridCell] {
  self: BaseParsers[GridCell] ⇒

  val empty: String = f"${""}%131s"

  def toStorage(obj: GridCell): String =
    f"${obj.id}%32d,${obj.tileX}%32d,${obj.tileY}%32d,${obj.distanceError}%32f"

  def readObj(str: String): Option[GridCell] = parseRoot(str)
}

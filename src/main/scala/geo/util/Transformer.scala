package geo.util

import geo.Message
import geo.Message.{GridCell, LocationTag}

import scala.util.parsing.combinator.RegexParsers

sealed trait Transformer[T <: Message] {
  val parser: BaseParsers[T]
  val empty: String

  def storeFormat(obj: T): String

  def readToObj(str: String): Option[T]
}

object Transformer {

  implicit val locationTagTransform: Transformer[LocationTag] = new Transformer[LocationTag] {

    val parser = new LocationTagParser()
    val empty: String = f"${""}%98s"

    def storeFormat(obj: LocationTag): String =
      f"${obj.userId}%32d,${obj.lon}%32f,${obj.lat}%32f"

    def readToObj(str: String): Option[LocationTag] =
      parser.parseRoot(str)
  }

  implicit val GridCellTransform: Transformer[GridCell] = new Transformer[GridCell] {

    val parser = new GridCellParser()
    val empty: String = f"${""}%131s"

    def storeFormat(obj: GridCell): String =
      f"${obj.id}%32d,${obj.tileX}%32d,${obj.tileY}%32d,${obj.distanceError}%32f"

    def readToObj(str: String): Option[GridCell] = parser.parseRoot(str)
  }

}

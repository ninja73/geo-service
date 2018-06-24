package geo.util

import com.typesafe.scalalogging.LazyLogging
import geo.entity.Entity.{GridPoint, PointId, UserMarker}

import scala.util.matching.Regex
import scala.util.parsing.combinator.{PackratParsers, RegexParsers}

trait BaseParsers[T] extends RegexParsers with PackratParsers with LazyLogging {

  val Eol = "\n"
  val number: Regex = """([+-]?\d+)""".r
  val float: Regex = """([+-]?\d+\.\d+)""".r
  val sepField = ","

  def root: Parser[T]

  def parseRoot(string: String): Option[T] = {
    parseAll(root, string) match {
      case Success(result, _) ⇒
        Some(result)
      case NoSuccess(info, _) ⇒
        logger.error(string)
        logger.error(info)
        None
    }
  }

}

trait MarkerParser extends BaseParsers[UserMarker] {

  val resultParse: Parser[UserMarker] = number ~ sepField ~ float ~ sepField ~ float <~ opt(Eol) ^^ {
    case userId ~ _ ~ lon ~ _ ~ lat ⇒ UserMarker(userId.toLong, lon.toFloat, lat.toFloat)
  }

  def root: Parser[UserMarker] = resultParse
}

trait GridParser extends BaseParsers[GridPoint] {

  val resultParse: Parser[GridPoint] =
    number ~ sepField ~ number ~ sepField ~ float <~ opt(Eol) ^^ {
      case tileX ~ _ ~ tileY ~ _ ~ error ⇒
        GridPoint(PointId(tileX.toInt, tileY.toInt), error.toFloat)
    }

  def root: Parser[GridPoint] = resultParse
}

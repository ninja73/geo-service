package geo.util

import com.typesafe.scalalogging.LazyLogging
import geo.entity.Entity.{Grid, GridId, LocationTag}

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

trait LocationTagParser extends BaseParsers[LocationTag] {

  val resultParse: Parser[LocationTag] = number ~ sepField ~ float ~ sepField ~ float <~ opt(Eol) ^^ {
    case userId ~ _ ~ lon ~ _ ~ lat ⇒ LocationTag(userId.toLong, lon.toFloat, lat.toFloat)
  }

  def root: Parser[LocationTag] = resultParse
}

trait GridParser extends BaseParsers[Grid] {

  val resultParse: Parser[Grid] =
    number ~ sepField ~ number ~ sepField ~ float <~ opt(Eol) ^^ {
      case tileX ~ _ ~ tileY ~ _ ~ error ⇒
        Grid(GridId(tileX.toInt, tileY.toInt), error.toFloat)
    }

  def root: Parser[Grid] = resultParse
}

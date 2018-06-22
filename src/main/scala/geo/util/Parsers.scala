package geo.util

import com.typesafe.scalalogging.StrictLogging
import geo.Message.{GridCell, LocationTag}

import scala.util.matching.Regex
import scala.util.parsing.combinator.{PackratParsers, RegexParsers}

trait BaseParsers[T] extends RegexParsers with PackratParsers with StrictLogging {

  val Eol = "\n"
  val int: Regex = """[\d]+""".r
  val float: Regex = """([+-]?\d+\.\d+)""".r
  val sepField = ","

  def root: Parser[T]

  def parseRoot(string: String): Option[T] = {
    parseAll(root, string) match {
      case Success(result, _) ⇒
        Some(result)
      case NoSuccess(info, _) ⇒
        logger.info(s"Parsed filed: $info")
        None
    }
  }
}

class LocationTagParser extends BaseParsers[LocationTag] {
  val resultParse: Parser[LocationTag] = int ~ sepField ~ float ~ sepField ~ float <~ opt(Eol) ^^ {
    case userId ~ _ ~ lon ~ _ ~ lat ⇒ LocationTag(userId.toInt, lon.toFloat, lat.toFloat)
  }

  def root: Parser[LocationTag] = resultParse
}

class GridCellParser extends BaseParsers[GridCell]  {
  val resultParse: Parser[GridCell] = int ~ sepField ~ int ~ sepField ~ int ~ sepField ~ float <~ opt(Eol) ^^ {
    case id ~ _ ~ tileX ~ _ ~ tileY ~ _ ~ error ⇒
      GridCell(id.toInt, tileX.toInt, tileY.toInt, error.toFloat)
  }

  def root: Parser[GridCell] = resultParse
}

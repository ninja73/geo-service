package geo

import geo.entity.Entity.{GridCell, GridId, LocationTag}
import geo.util.{GridParser, LocationTagParser}
import org.scalatest.{Matchers, WordSpec}

class ParserSpec extends WordSpec with Matchers {

  object locationParser extends LocationTagParser

  object gridParser extends GridParser

  "Parser" should {
    "Valid location parse" in {
      locationParser.parseRoot("14,20.6607,31.2019") shouldBe Some(LocationTag(14, 20.6607f, 31.2019f))
    }
    "Not valid location parse" in {
      locationParser.parseRoot("14,31.2019") shouldBe None
    }
    "Valid Grid parse" in {
      gridParser.parseRoot("-30,-90,7.34680145611") shouldBe Some(GridCell(GridId(-30, -90), 7.34680145611f))
    }
    "Not valid Grid parse" in {
      gridParser.parseRoot("-30,7.34680145611") shouldBe None
    }

  }
}

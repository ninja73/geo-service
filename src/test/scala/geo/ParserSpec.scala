package geo

import geo.entity.Entity.{GridPoint, PointId, UserMarker}
import geo.util.{GridParser, MarkerParser}
import org.scalatest.{Matchers, WordSpec}

class ParserSpec extends WordSpec with Matchers {

  object locationParser extends MarkerParser

  object gridParser extends GridParser

  "Parser" should {
    "Valid location parse" in {
      locationParser.parseRoot("14,20.6607,31.2019") shouldBe Some(UserMarker(14, 20.6607f, 31.2019f))
    }
    "Not valid location parse" in {
      locationParser.parseRoot("14,31.2019") shouldBe None
    }
    "Valid Grid parse" in {
      gridParser.parseRoot("-30,-90,7.34680145611") shouldBe Some(GridPoint(PointId(-30, -90), 7.34680145611f))
    }
    "Not valid Grid parse" in {
      gridParser.parseRoot("-30,7.34680145611") shouldBe None
    }

  }
}

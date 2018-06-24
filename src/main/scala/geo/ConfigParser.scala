package geo

import geo.entity.Entity.CmdConfig
import scopt.OptionParser

object ConfigParser {
  val parser: OptionParser[CmdConfig] = new scopt.OptionParser[CmdConfig]("geo-service") {
    opt[String]('u', "user-storage").required().valueName("<file>")
      .action( (u, c) ⇒ c.copy(userMarkersPath = u) )
      .text("user-storage is a required")

    opt[String]('g', "grid-storage").required().valueName("<file>")
      .action( (g, c) ⇒ c.copy(gridPath = g) )
      .text("grid-storage is a required")
  }
}

package geo

import geo.Message.Config
import scopt.OptionParser

object ConfigParser {
  val parser: OptionParser[Config] = new scopt.OptionParser[Config]("geo-service") {
    opt[String]('u', "user-storage").required().valueName("<file>")
      .action( (u, c) ⇒ c.copy(userLabelsPath = u) )
      .text("user-storage is a required")

    opt[String]('g', "grid-storage").required().valueName("<file>")
      .action( (g, c) ⇒ c.copy(gridPath = g) )
      .text("grid-storage is a required")

    opt[Int]('c', "user-storage-count").required().valueName("<Int>")
      .action( (uc, c) ⇒ c.copy(userLabelCount = uc) )
      .text("user-storage-count is a required")

    opt[Int]("min-tile-x").required().valueName("<Int>")
      .action( (x, c) ⇒ c.copy(gridMinTileX = x) )
      .text("min-tile-x is a required")

    opt[Int]("min-tile-y").required().valueName("<Int>")
      .action( (y, c) ⇒ c.copy(gridMinTileY = y) )
      .text("min-tile-y is a required")

    opt[Int]("max-tile-x").required().valueName("<Int>")
      .action( (x, c) ⇒ c.copy(gridMaxTileX = x) )
      .text("min-tile-x is a required")

    opt[Int]("max-tile-y").required().valueName("<Int>")
      .action( (y, c) ⇒ c.copy(gridMaxTileY = y) )
      .text("min-tile-y is a required")
  }
}

import sbt._

object Dependency {

  val akkaHttpVersion = "10.1.3"
  val akkaVersion = "2.5.12"
  val scalaTestVersion = "3.0.5"

  val scalactic =         "org.scalactic"               %% "scalactic" % scalaTestVersion % Test
  val scalatest =         "org.scalatest"               %% "scalatest" % scalaTestVersion % Test
  val akkaHttpTestkit =   "com.typesafe.akka"           %% "akka-http-testkit" % akkaHttpVersion % Test

  val akkaHttp =          "com.typesafe.akka"           %% "akka-http" % akkaHttpVersion
  val akkaStream =        "com.typesafe.akka"           %% "akka-stream" % akkaVersion
  val akkaHttpJson =      "com.typesafe.akka"           %% "akka-http-spray-json" % akkaHttpVersion
  val akkaPersistence =   "com.typesafe.akka"           %% "akka-persistence" % akkaVersion

  val logback =           "ch.qos.logback"              % "logback-classic" % "1.2.3"
  val scalaLogging =      "com.typesafe.scala-logging"  %% "scala-logging" % "3.7.2"

  val scopt =             "com.github.scopt"            %% "scopt" % "3.7.0"

  val leveldb =           "org.iq80.leveldb"            % "leveldb"          % "0.7"
  val leveldbjniAll =     "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8"

  val Akka = Seq(akkaHttp, akkaStream, akkaHttpJson, akkaPersistence)
  val Logging = Seq(logback, scalaLogging)
  val Testing = Seq(scalactic, scalatest, akkaHttpTestkit)
  val LevelDb = Seq(leveldb, leveldbjniAll)
}

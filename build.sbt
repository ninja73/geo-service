name := "geo-service"
version := "0.1"
scalaVersion := "2.12.6"

assemblyJarName in assembly := s"${name.value}-${version.value}.jar"

val akkaHttpVersion = "10.1.3"
val akkaVersion = "2.5.12"
val scalatestVersion = "3.0.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "com.github.scopt" %% "scopt" % "3.7.0",
  "org.scalactic" %% "scalactic" % scalatestVersion % Test,
  "org.scalatest" %% "scalatest" % scalatestVersion % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
)
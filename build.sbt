name := "geo-service"
version := "0.1"
scalaVersion := "2.12.6"

assemblyJarName in assembly := s"${name.value}-${version.value}.jar"

val akkaHttpVersion = "10.1.3"
val akkaVersion = "2.5.12"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "com.github.scopt" %% "scopt" % "3.7.0"
)
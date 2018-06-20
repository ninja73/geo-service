name := "geo-service"
version := "0.1"
scalaVersion := "2.12.6"

assemblyJarName in assembly := s"${name.value}-${version.value}.jar"

val akkaVersion = "10.1.3"
val akkaHttpVersion = "2.5.12"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaHttpVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
)
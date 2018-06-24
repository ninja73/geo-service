name := "geo-service"
version := "0.1"
scalaVersion := "2.12.6"

assemblyOutputPath in assembly := file(s"assembly/${name.value}-${version.value}.jar")

libraryDependencies ++=
  Dependency.AkkaHttp ++
    Dependency.Logging ++
    Dependency.Testing ++ Seq(Dependency.scopt)

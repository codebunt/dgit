name := """dgit"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "commons-lang" % "commons-lang" % "2.6"
)

libraryDependencies += filters

libraryDependencies += "commons-beanutils" % "commons-beanutils" % "1.9.2"

libraryDependencies += "postgresql" % "postgresql" % "9.1-901-1.jdbc4"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

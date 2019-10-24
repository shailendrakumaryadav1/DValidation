name := "dvalidation"

organization := "net.atinu"

version := "7.2.7.0.2"

scalaVersion := "2.12.9"

scalacOptions  ++= Seq("-unchecked", "-deprecation", "-feature")

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)

val scalazVersion = "7.2.7"

libraryDependencies += "org.scalaz" %% "scalaz-core" % scalazVersion % "provided"

libraryDependencies += "org.scalaz" %% "scalaz-scalacheck-binding" % scalazVersion % "test"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"

//scoverage.ScoverageSbtPlugin.instrumentSettings
//
//org.scoverage.coveralls.CoverallsPlugin.coverallsSettings
//
//scalariformSettings

publishMavenStyle := true



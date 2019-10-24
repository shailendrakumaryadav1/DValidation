logLevel := Level.Warn

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.3")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.8")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.0-M2")

resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "1.6.0")

addSbtPlugin("org.scoverage" %% "sbt-coveralls" % "1.2.7")
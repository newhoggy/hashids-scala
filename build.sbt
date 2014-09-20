libraryDependencies += "org.specs2" %% "specs2" % "2.4.2" % "test"

scalacOptions in Test ++= Seq("-Yrangepos")

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)


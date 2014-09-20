name := "hashids-scala"

organization := "org.hashids"

version := "1.0-SNAPSHOT"

crossScalaVersions := Seq("2.10.4", "2.11.2")

libraryDependencies += "org.specs2" %% "specs2" % "2.4.2" % "test"

scalacOptions in Test ++= Seq("-Yrangepos")

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

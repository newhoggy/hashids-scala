name := "hashids-scala"

organization := "org.hashids"

version := "1.0-SNAPSHOT"

crossScalaVersions := Seq("2.10.4", "2.11.2")

libraryDependencies += "org.specs2" %% "specs2" % "2.4.2" % "test"

scalacOptions in Test ++= Seq("-Yrangepos")

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

pomExtra :=
  <licenses>
    <license>
      <name>MIT</name>
      <url>http://opensource.org/licenses/MIT</url>
      <distribution>repo</distribution>
    </license>
  </licenses>

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { x => false }

pomExtra := (
  <url>http://john-ky.io</url>
  <licenses>
    <license>
      <name>MIT</name>
      <url>http://opensource.org/licenses/MIT</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:newhoggy/hashids-scala.git</url>
    <connection>scm:git:git@github.com:newhoggy/hashids-scala.git</connection>
  </scm>
  <developers>
    <developer>
      <id>newhoggy</id>
      <name>John Ky</name>
      <url>http://john-ky.io</url>
    </developer>
  </developers>
  )

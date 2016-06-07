import sbt._
import Keys._

object Build extends Build with Version {
  val specs2_core       = "org.specs2"      %%  "specs2-core"         % "3.6.2"
  val specs2_scalacheck = "org.specs2"      %%  "specs2-scalacheck"   % "3.6.2"
  val scalacheck        = "org.scalacheck"  %%  "scalacheck"          % "1.12.4"

  implicit class ProjectOps(self: Project) {
    def standard: Project = {
      self
          .settings(organization := "io.john-ky")
          .settings(resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases")
          .settings(scalacOptions := Seq("-feature", "-deprecation", "-unchecked", "-Xlint", "-Yrangepos", "-encoding", "utf8"))
          .settings(scalacOptions in Test ++= Seq("-Yrangepos"))
    }

    def notPublished: Project = {
      self
          .settings(publish := {})
          .settings(publishArtifact := false)
    }

    def published: Project = {
      self
          .settings(publishTo := Some("Scalap Releases" at "s3://dl.john-ky.io/maven/releases"))
          .settings(isSnapshot := true)
    }

    def libs(modules: ModuleID*) = self.settings(libraryDependencies ++= modules)

    def testLibs(modules: ModuleID*) = self.libs(modules.map(_ % "test"): _*)
  }

  lazy val `hashids-scala` = Project(id = "hashids-scala", base = file("hashids-scala"))
      .standard
      .published
      .testLibs(scalacheck, specs2_core, specs2_scalacheck)

  lazy val root = Project(id = "all", base = file("."))
      .notPublished
      .aggregate(`hashids-scala`)
}

import sbt._
import Keys._

object Multibuild extends Build with Version {
  val scalacheckV = "1.12.4"
  val specs2V = "3.6.2"

  implicit class ProjectOps(self: Project) {
    def standard: Project = {
      self
          .settings(organization := "io.john-ky")
          .settings(resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases")
          .settings(scalacOptions := Seq(
              "-feature",
              "-deprecation",
              "-unchecked",
              "-Xlint",
              "-Yrangepos",
              "-encoding",
              "utf8"))
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

    def dependsOnAndAggregates(projects: Project*): Project = {
      val dependencies = projects.map(pr => pr: sbt.ClasspathDep[sbt.ProjectReference])
      val aggregates = projects.map(pr => pr: sbt.ProjectReference)
      self.dependsOn(dependencies: _*).aggregate(aggregates: _*)
    }
  }

  lazy val `hashids-scala` = Project(id = "hashids-scala", base = file("hashids-scala"))
      .standard
      .published
      .settings(libraryDependencies ++= Seq(
          "org.specs2"      %% "specs2-core"        % specs2V     % "test",
          "org.specs2"      %% "specs2-scalacheck"  % specs2V     % "test",
          "org.scalacheck"  %% "scalacheck"         % scalacheckV % "test"))

  lazy val root = Project(id = "all", base = file("."))
      .notPublished
      .aggregate(`hashids-scala`)
}

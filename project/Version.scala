import sbt._
import Keys._

trait Version {
  object Tmp {
    val specs2V = "3.6.2"

    val semVersion = settingKey[String]("The SEM version prefix.")

    val shaVersion = settingKey[Option[String]]("The SHA version suffix.")

    val hasLocalChanges = settingKey[Boolean]("Flag indicating whether there are local changes.")

    val remote = settingKey[String]("")
  }

  lazy val releaseVersion = List("1.1.2")

  lazy val gitSha = Process("git rev-parse --short HEAD").lines.take(1).toList

  lazy val username = Option(System.getProperty("user.name")).toList

  lazy val localChanges: List[String] = {
    val stagedChanges = ("git diff-index --quiet HEAD" !) != 0
    val untrackedChanges = "git ls-files --other --exclude-standard".!! != ""
    if (stagedChanges || untrackedChanges) {
      username :+ "-SNAPSHOT"
    } else {
      List.empty[String]
    }
  }

  lazy val buildVersion = {
    implicit class ListStringOps(self: List[String]) {
      def ?:(delim: String): List[String] = if (self.nonEmpty) delim :: self else self
    }
    (releaseVersion ++ ("-" ?: gitSha) ++ ("-" ?: localChanges)).mkString
  }
}

import sbt._
import sbt.Keys._

/**
 * Created by Zac on 12/05/16.
 */
 object ARGEBuildSettings {
  lazy val root = Project(id = "root", base = file("."))

  /**
    * Use this method to create a new project.
    *
    * @param projectName Project name.
    * @return A new [[Project]] with default settings.
    */
  def project(projectName: String) = {
    Project(id = projectName, base = file(projectName))
      .settings(name := projectName)
      .settings(
        organization := ARGEBuildConstants.Organization,
        version := ARGEBuildConstants.Version,
        scalaVersion := ARGEBuildConstants.ScalaVersion,
        scalacOptions := ARGEBuildConstants.ScalacOptions)
  }

  /**
    * Implicit decorator around sbt's [[Project]]. It adds additional useful features and methods.
    *
    * @param p Project to decorate.
    */
  implicit class RichSettingsProject(p: Project) {
    /**
      * Adds external dependencies to a project.
      *
      * @param mms Dependencies to add.
      * @return Project with the dependencies added.
      */
    def libraryDependencies(mms: ModuleID*) = {
      p.settings(mms.map(sbt.Keys.libraryDependencies += _): _*)
    }
  }
}

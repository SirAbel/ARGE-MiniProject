import sbt._
import sbt.Keys._
import ARGEBuildSettings.RichSettingsProject
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging

object ARGEBuild extends Build {

  override def settings = super.settings ++ Seq(resolvers ++= ARGEDependenciesResolvers.resolvers)

  lazy val root = ARGEBuildSettings.root.aggregate(common, loadGenerator)

  lazy val common = ARGEBuildSettings.project("common").libraryDependencies(
    ARGEDependencies.OpenStack.core,
    ARGEDependencies.OpenStack.httpClient,

    ARGEDependencies.Logging.slf4j,
    ARGEDependencies.Logging.logbackClassic,

    ARGEDependencies.Config.typeSafeConfig,
    ARGEDependencies.Config.ficus,

    ARGEDependencies.Commons.util,
    ARGEDependencies.Commons.logging,
    ARGEDependencies.Commons.httpClient,

    ARGEDependencies.XmlRpc.server,
    ARGEDependencies.XmlRpc.common,
    ARGEDependencies.XmlRpc.client,

    ARGEDependencies.Commons.codec,
    ARGEDependencies.Monitoring.oshi

  )

  lazy val loadGenerator = ARGEBuildSettings.project("loadGenerator").dependsOn(common).enablePlugins(JavaAppPackaging)

  lazy val computeNode = ARGEBuildSettings.project("computeNode").dependsOn(common).enablePlugins(JavaAppPackaging)

  lazy val loadBalancer = ARGEBuildSettings.project("loadBalancer").dependsOn(common).libraryDependencies(
    ARGEDependencies.Akka.actors
  ).enablePlugins(JavaAppPackaging)

}
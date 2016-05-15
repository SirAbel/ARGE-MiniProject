import sbt._

object ARGEDependenciesResolvers {
  val resolvers = Seq(Resolver.jcenterRepo, Resolver.mavenLocal)
}

object ARGEDependencies {

  object OpenStack {
    val core = "org.pacesys" % "openstack4j-core" % "2.11"
    val httpClient = "org.pacesys.openstack4j.connectors" % "openstack4j-httpclient" % "2.11"
  }

  object Logging {
    val slf4j = "org.slf4j" % "slf4j-log4j12" % "1.2"
    val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.1.3"
  }

  object Config {
    val typeSafeConfig = "com.typesafe" % "config" % "1.2.1"
    val ficus = "com.iheart" %% "ficus" % "1.1.3"
  }

  object XmlRpc {
    val client = "org.apache.xmlrpc" % "xmlrpc-client" % "3.1.3"
    val common = "org.apache.xmlrpc" % "xmlrpc-common" % "3.1.3"
    val server = "org.apache.xmlrpc" % "xmlrpc-server" % "3.1.3"
  }

  object Commons {
    val httpClient = "commons-httpclient" % "commons-httpclient" % "3.1-rc1"
    val util = "org.apache.ws.commons.util" % "ws-commons-util" % "1.0.2"
    val logging = "commons-logging" % "commons-logging" % "1.0.1"
    val codec = "commons-codec" % "commons-codec" % "1.10"
  }

  object Akka {
    val actors = "com.typesafe.akka" % "akka-actor_2.11" % "2.4.4"
  }

  object Monitoring {
    val oshi = "com.github.dblock" % "oshi-core" % "2.2"
  }
}
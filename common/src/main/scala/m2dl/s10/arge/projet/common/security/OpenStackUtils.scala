package m2dl.s10.arge.projet.common.security

import java.util.UUID

import m2dl.s10.arge.projet.common.config.ConfigHandler
import m2dl.s10.arge.projet.common.config.readers._
import org.openstack4j.api.OSClient
import org.openstack4j.core.transport.{Config, ProxyHost}
import org.openstack4j.model.compute.{ActionResponse, Server, ServerCreate}
import org.openstack4j.openstack.OSFactory
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._


/**
  * Created by Zac on 12/05/16.
  */
object OpenStackUtils {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def authenticate(): OSClient = {

    val authenticationInfo = ConfigHandler.getAuthenticationInfo
    val proxyInfo = ConfigHandler.getProxyInfo

    //  PROXY MODE //
    proxyInfo match {
      case Some(readProxyInfo) =>
        logger.info("Current logging configuration: " + authenticationInfo.forLogMultiString + "Using: " + readProxyInfo.forLogMultiString)

        val client: OSClient = OSFactory.builder()
          .endpoint(authenticationInfo.authurl)
          .credentials(authenticationInfo.username, authenticationInfo.password)
          .tenantName(authenticationInfo.tenantname)
          .withConfig(Config.newConfig().withProxy(ProxyHost.of(readProxyInfo.hostname, readProxyInfo.port)))
          .authenticate()

        logger.info("Authentication successful on authurl=[{}]", authenticationInfo.authurl)
        client


      // DIRECT MODE //

      case None =>
        logger.info("Current logging configuration: " + authenticationInfo.forLogMultiString)


        val client: OSClient = OSFactory.builder()
          .endpoint(authenticationInfo.authurl)
          .credentials(authenticationInfo.username, authenticationInfo.password)
          .tenantName(authenticationInfo.tenantname)
          .authenticate()

        logger.info("Authentication successful on authurl=[{}]", authenticationInfo.authurl)

        client

    }
  }

  def createServer (client: OSClient, namePrefix: String, userData: String): Server = {
    val name: String = (namePrefix + UUID.randomUUID.toString).substring(0, 28)
    logger.info("Creating new server with name=[{}]...", name)

    val serverInfo = ConfigHandler.getServerCreationInfo

    // Image "flm-bs-java-ubuntu"

    val newServer: ServerCreate = client.compute.servers.serverBuilder.flavor("2").
      image(serverInfo.imageId)
      .keypairName("mykey").name(name)
      .networks(serverInfo.networksId).userData(userData).build

    // Boot the server
    var server: Server = client.compute.servers.boot(newServer)

    // Check errors
    Option(server) match {
      case Some(startedServer) if startedServer.getStatus == Server.Status.ERROR =>
        val message = String.format("Error when creating server [%s] because [%s]", name, startedServer.getFault)
        signalError(message)

      case None =>
        val message = String.format("Error when creating server [%s] because server is null", name)
        signalError(message)

      case Some(startedServer) =>
        // Wait for the server creation
        while ({server = client.compute.servers.get(startedServer.getId); server}.getStatus != Server.Status.ACTIVE) {
          logger.info("Waiting for server=[{}] to start...", name)
          try {
            Thread.sleep(5 * 1000)
          } catch {
            case e: InterruptedException =>
          }
        }
        server
    }
  }

  def delete(client: OSClient, serverId: String) = {
    logger.info("Deleting server [{}]...", serverId)
    val delete: ActionResponse = client.compute().servers().delete(serverId)
    delete.isSuccess
  }

  def signalError(msg: String) = {
    logger.error(msg)
    throw new RuntimeException(msg)
  }
}

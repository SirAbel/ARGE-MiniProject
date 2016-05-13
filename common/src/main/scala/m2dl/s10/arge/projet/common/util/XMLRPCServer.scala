package m2dl.s10.arge.projet.common.util

import java.io.IOException

import org.apache.xmlrpc.XmlRpcException
import org.apache.xmlrpc.server.{PropertyHandlerMapping, XmlRpcServer, XmlRpcServerConfigImpl}
import org.apache.xmlrpc.webserver.WebServer
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by Zac on 13/05/16.
  */
object XMLRPCServer {
  private final val logger: Logger = LoggerFactory.getLogger(this.getClass)
  private final val DEFAULT_LISTENING_PORT: Int = 9876
}

@throws(classOf[IOException])
@throws(classOf[XmlRpcException])
class XMLRPCServer(listeningPort: Int = XMLRPCServer.DEFAULT_LISTENING_PORT, classLoader: ClassLoader) {

  private val webServer = new WebServer(listeningPort)
  val xmlRpcServer: XmlRpcServer = webServer.getXmlRpcServer
  val phm: PropertyHandlerMapping = new PropertyHandlerMapping
  phm.load(classLoader, "XmlRpcServlet.properties")
  xmlRpcServer.setHandlerMapping(phm)
  val serverConfig: XmlRpcServerConfigImpl = xmlRpcServer.getConfig.asInstanceOf[XmlRpcServerConfigImpl]
  serverConfig.setEnabledForExtensions(true)
  serverConfig.setContentLengthOptional(false)

  @throws[IOException]
  def start() = {
    XMLRPCServer.logger.info("Starting server on port=[{}]...", listeningPort)
    this.webServer.start
    XMLRPCServer.logger.info("Server has been started on port=[{}]", listeningPort)
  }

  def stop() = {
    this.webServer.shutdown
  }
}

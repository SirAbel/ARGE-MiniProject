import m2dl.s10.arge.projet.common.config.ConfigHandler
import m2dl.s10.arge.projet.common.util.XMLRPCServer
import org.slf4j.LoggerFactory
import net.ceedubs.ficus.Ficus._

/**
  * Created by Zac on 13/05/16.
  */
object CPNEntryPoint {

  private val logger = LoggerFactory.getLogger(this.getClass)
  private var xmlrpcServer: Option[XMLRPCServer] = None

  def main(args: Array[String]) {

    try {
      val rpcPort = ConfigHandler.getConfig.as[Int]("app.openStack.xmlRpc.computenodePort")
      xmlrpcServer = Option(new XMLRPCServer(rpcPort, Thread.currentThread.getContextClassLoader))
      xmlrpcServer.get.start()
    }
    catch {
      case e: Exception =>
        logger.error("Could not start the server", e)
        System.exit(-1)
    }

    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      def run = {
        if (xmlrpcServer.isDefined) {
          logger.info("Stopping server...")
          xmlrpcServer.get.stop()
        }}}, "Shutdown"))

  }
}
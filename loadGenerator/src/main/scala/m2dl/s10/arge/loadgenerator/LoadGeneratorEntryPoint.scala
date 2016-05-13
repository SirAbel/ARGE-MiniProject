package m2dl.s10.arge.loadgenerator

import java.net.URL

import m2dl.s10.arge.loadgenerator.util.LoadGeneratorException
import m2dl.s10.arge.projet.common.util.XMLRPCClient
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by Zac on 13/05/16.
  */
object LoadGeneratorEntryPoint {

  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]) {

    try {
      val loadBalancerUrl = args.lift(1).getOrElse(throw LoadGeneratorException.MissingCallParameter("Missing load balancer url"))
      val client = new XMLRPCClient(new URL(loadBalancerUrl))

      generateRandomCharge(client)

    } catch {
      case e: LoadGeneratorException.MissingCallParameter =>
        logAndExit("Usage: ./generator <loadBalancer host>",e)
      case e: Exception =>
        logAndExit("Failed to create client - shutting down",e)
    }
  }

  private def generateRandomCharge(client: XMLRPCClient) = ???

  private def logAndExit(msg: String, e: Throwable) = {
    logger.error(msg,e)
    System.exit(-1)
  }

}

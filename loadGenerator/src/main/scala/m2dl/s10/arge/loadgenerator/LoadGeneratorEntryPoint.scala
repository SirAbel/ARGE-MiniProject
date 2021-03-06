package m2dl.s10.arge.loadgenerator

import java.math.BigDecimal
import java.net.URL
import java.util.concurrent.TimeUnit

import m2dl.s10.arge.loadgenerator.objs.LoadType
import m2dl.s10.arge.loadgenerator.util.LoadGeneratorException
import m2dl.s10.arge.projet.common.config.ConfigHandler
import m2dl.s10.arge.projet.common.util.XMLRPCClient
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration.FiniteDuration
import scala.util.Random

/**
  * Created by Zac on 13/05/16.
  */
object LoadGeneratorEntryPoint {

  private val logger: Logger = LoggerFactory.getLogger(this.getClass)
  private val DEFAULT_NB_DECIMALS = ConfigHandler.getConfig.getInt("app.openStack.defaults.nbDecimals")
  private val DEFAULT_BURST_INTERVAL = ConfigHandler.getConfig.getInt("app.openStack.defaults.burstInterval")

  def main(args: Array[String]) {

    try {
      val loadBalancerUrl = args.headOption.getOrElse(throw LoadGeneratorException.MissingCallParameter("Missing load balancer url"))
      val client = new XMLRPCClient(new URL(loadBalancerUrl))

      generateRandomCharge(client)

    } catch {
      case e: LoadGeneratorException.MissingCallParameter =>
        logAndExit("Usage: ./generator <loadBalancer host>",e)
      case e: Exception =>
        logAndExit("Failed to create client - shutting down",e)
    }
  }

  private def generateRandomCharge(client: XMLRPCClient) = {
    val rndom = Random

    def sendBatch(upperLimit: Int): Unit = {
      for(k <- 0 until upperLimit) {
        val params = Array.fill[Object](1)(Int.box(DEFAULT_NB_DECIMALS))
        client.send("compute.getPIWithDecimals",params,classOf[Option[BigDecimal]]).foreach(rslt => println(s"computation[$k] - result: $rslt"))
      }
    }

    while(true) {
      sendBatch(LoadType.randomValue.id)
      Thread.sleep(FiniteDuration(DEFAULT_BURST_INTERVAL,TimeUnit.MICROSECONDS).toMillis)
    }
  }

  private def logAndExit(msg: String, e: Throwable) = {
    logger.error(msg,e)
    System.exit(-1)
  }

}

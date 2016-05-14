package m2dl.s10.arge.projet.loadbalancer.server.handlers

import java.math.BigDecimal

import m2dl.s10.arge.projet.common.util.{IComputationWork, PICalculator}

/**
  * Created by Zac on 13/05/16.
  */
class RequestHandler extends IComputationWork{

  override def getPIWithDecimals(nbDecimals: Int): BigDecimal = {
    PICalculator.computePIValueForDecimals(nbDecimals)
  }
}

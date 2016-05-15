package m2dl.s10.arge.computeNode.handlers

import java.math.BigDecimal

import m2dl.s10.arge.projet.common.IComputationWork
import m2dl.s10.arge.projet.common.util.PICalculator

/**
  * Created by Zac on 13/05/16.
  */
class Worker extends IComputationWork  {

  override def getPIWithDecimals(nbDecimals: Int): Option[BigDecimal] = {
    Option(PICalculator.computePIValueForDecimals(nbDecimals))
  }
}

import java.math.BigDecimal

import m2dl.s10.arge.projet.common.util.{IComputationWork, PICalculator}

/**
  * Created by Zac on 13/05/16.
  */
class Worker extends IComputationWork  {

  override def getPIWithDecimals(nbDecimals: Int): Option[BigDecimal] = {
    Option(PICalculator.computePIValueForDecimals(nbDecimals))
  }
}

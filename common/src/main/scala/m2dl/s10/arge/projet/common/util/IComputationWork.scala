package m2dl.s10.arge.projet.common.util

import java.math.BigDecimal

/**
  * Created by Zac on 13/05/16.
  */
trait IComputationWork {

  def getPIWithDecimals(nbDecimals: Int): BigDecimal
}

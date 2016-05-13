package m2dl.s10.arge.projet.common.util

import java.math.BigDecimal

/**
  * Created by Zac on 12/05/16.
  */
object PICalculator {

  // Adapted from the answer http://stackoverflow.com/a/5590575/2630191
  def computePIValueForDecimals(nbDecimals: Int, nbTerms: Int = 10000): BigDecimal = {

    var sum: BigDecimal = new BigDecimal(0)
    var term: BigDecimal = new BigDecimal(0)
    var sign: BigDecimal = new BigDecimal(1.0)

    val one: BigDecimal = new BigDecimal(1.0)
    val two: BigDecimal = new BigDecimal(2.0)

    for (k <- 0 until nbTerms) {
      val count: BigDecimal = new BigDecimal(k)

      val temp1: BigDecimal = two.multiply(count)
      val temp2: BigDecimal = temp1.add(one)
      term = one.divide(temp2,nbDecimals,BigDecimal.ROUND_FLOOR)

      //sum = sum + sign*term;
      val temp3: BigDecimal = sign.multiply(term)
      sum = sum.add(temp3)

      sign = sign.negate()
    }
    var pi: BigDecimal = new BigDecimal(0)
    val four: BigDecimal = new BigDecimal(4)
    pi = sum.multiply(four)

    pi
  }

}

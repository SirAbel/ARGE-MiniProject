package m2dl.s10.arge.loadgenerator.objs

import scala.util.Random

/**
  * Created by Zac on 13/05/16.
  */
object LoadType extends Enumeration {

  type LoadType = Value
  val HIGH = Value(50000)
  val MEDIUM = Value(25000)
  val LOW = Value(10000)

  def valuesAsSeq = this.values.toSeq

  def randomValue = valuesAsSeq(Random.nextInt(valuesAsSeq.size))
}

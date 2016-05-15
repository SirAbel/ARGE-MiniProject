package m2dl.s10.arge.projet.loadbalancer.core.util

/**
  * Created by Zac on 15/05/16.
  */
object LoadBalancerException {

  class MissingServerAddress(msg: String, t: Throwable = null) extends LoadBalancerException(msg, t)
}

abstract class LoadBalancerException(msg: String, t: Throwable = null) extends Exception(msg, t)

package m2dl.s10.arge.loadgenerator.util

/**
  * Created by Zac on 13/05/16.
  */
object LoadGeneratorException {

  case class MissingCallParameter(msg: String, t: Throwable = null) extends LoadGeneratorException(msg, t)
}

abstract class LoadGeneratorException(msg: String, t: Throwable = null) extends Exception(msg,t)

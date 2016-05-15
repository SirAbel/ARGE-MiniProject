package m2dl.s10.arge.projet.loadbalancer.core.model

import java.math.BigDecimal

/**
  * Created by Zac on 14/05/16.
  */
case class JobDescription(handlerName: String, params: Array[Object], responseType: Class[Option[BigDecimal]])

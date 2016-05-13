package m2dl.s10.arge.projet.common.config

import net.ceedubs.ficus.readers.ValueReader
import net.ceedubs.ficus.Ficus._

/**
  * Created by Zac on 12/05/16.
  */
package object readers {

  private[common] implicit val openStackAuthConfReader = ValueReader.relative[OpenStackConfig.Authentication] { conf =>
    OpenStackConfig.Authentication(
      username = conf.as[String]("username"),
      password = conf.as[String]("password"),
      authurl = conf.as[String]("authurl"),
      tenantname = conf.as[String]("tenantname")
    )
  }

  private[common] implicit val openStackProxyConfReader = ValueReader.relative[OpenStackConfig.ProxyInfo] { conf =>
    OpenStackConfig.ProxyInfo(
      hostname = conf.as[String]("hostname"),
      port = conf.as[Int]("port")
    )
  }

  private[common] implicit val openStackServerConfReader = ValueReader.relative[OpenStackConfig.ServerCreationInfo] { conf =>
    OpenStackConfig.ServerCreationInfo(
      imageId = conf.as[String]("imageId"),
      networksId = conf.as[Seq[String]]("networksId")
    )
  }
}

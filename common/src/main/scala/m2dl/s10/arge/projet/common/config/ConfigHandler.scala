package m2dl.s10.arge.projet.common.config

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ValueReader

/**
  * Created by Zac on 12/05/16.
  */
object ConfigHandler {

  private val config = ConfigFactory.load()


  def getConfig = config

  def getAuthenticationInfo(implicit reader: ValueReader[OpenStackConfig.Authentication]) = {
    config.as[OpenStackConfig.Authentication]("app.openStack.auth")
  }

  def getProxyInfo(implicit reader: ValueReader[OpenStackConfig.ProxyInfo]) = {
    config.as[Option[OpenStackConfig.ProxyInfo]]("app.openStack.proxy")
  }

  def getServerCreationInfo(implicit reader: ValueReader[OpenStackConfig.ServerCreationInfo]) = {
    config.as[OpenStackConfig.ServerCreationInfo]("app.openStack.cloudMip")
  }

}

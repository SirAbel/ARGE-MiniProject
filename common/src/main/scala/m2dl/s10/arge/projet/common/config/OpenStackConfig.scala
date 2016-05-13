package m2dl.s10.arge.projet.common.config

/**
  * Created by Zac on 12/05/16.
  */
object OpenStackConfig {

  case class Authentication(username: String, password: String, authurl: String, tenantname: String) extends OpenStackConfig {
    override def forLogMultiString =
      s"""Authentication {
         |keystone=[$authurl]
         |username=[$username]
         |tenant=[$tenantname]
         |}
       """.stripMargin
  }

  case class ProxyInfo(hostname: String, port: Int) extends OpenStackConfig {
    override def forLogMultiString: String =
      s"""Proxy {
         |hostname=[$hostname]
         |port=[$port]
         |}
       """.stripMargin
  }

  case class ServerCreationInfo(imageId:String, networksId: Seq[String]) extends OpenStackConfig {
    override def forLogMultiString: String =
      s"""CreationInfo {
         |imageId=[$imageId]
         |networksId=[${networksId.mkString("-")}]
       """.stripMargin
  }
}

abstract class OpenStackConfig {
  def forLogMultiString: String
}

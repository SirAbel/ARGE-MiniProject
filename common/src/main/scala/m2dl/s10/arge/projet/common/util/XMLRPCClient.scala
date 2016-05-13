package m2dl.s10.arge.projet.common.util

import java.net.URL

import org.apache.xmlrpc.XmlRpcException
import org.apache.xmlrpc.client.{XmlRpcClient, XmlRpcClientConfigImpl, XmlRpcCommonsTransportFactory}

/**
  * Created by Zac on 12/05/16.
  */

object XMLRPCClient {

  @throws(classOf[XmlRpcException])
  def send[T](serverURL: URL, endpoint: String, params: Array[Object], _type: Class[T]): T =  {
    // create configuration
    val config: XmlRpcClientConfigImpl = new XmlRpcClientConfigImpl
    config.setServerURL(serverURL)
    config.setEnabledForExtensions(true)
    config.setConnectionTimeout(60 * 1000)
    config.setReplyTimeout(60 * 1000)

    val client: XmlRpcClient = new XmlRpcClient

    // use Commons HttpClient as transport
    client.setTransportFactory(new XmlRpcCommonsTransportFactory(client))
    // set configuration
    client.setConfig(config)

    // make the a regular call
    _type.cast(client.execute(endpoint, params))
  }
}

class XMLRPCClient(serverURL: URL) {

  @throws(classOf[XmlRpcException])
  def send[T](endpoint: String, params: Array[Object], _type: Class[T]): T =  {
    XMLRPCClient.send(serverURL, endpoint, params, _type)
  }

}
package m2dl.s10.arge.projet.loadbalancer.core.model

/**
  * Created by Zac on 15/05/16.
  */
case class WorkerNode(nodeId: String,serverUrl: String, pendingDelete: Boolean = false, runningWorks: Int = 0) {
  def toMultilineString =
    s"""Node {
       |Id: $nodeId
       |url: $serverUrl
       |runningWorks: $runningWorks
       |}
     """.stripMargin
}

package m2dl.s10.arge.projet.loadbalancer.core.model

/**
  * Created by Zac on 15/05/16.
  */
case class WorkerNode(nodeId,hostname: String, pendingDelete: Boolean, runningWorks: Int = 0)

package m2dl.s10.arge.projet.common

import m2dl.s10.arge.projet.common.util.WorkerNodeMonitoringReport

/**
  * Created by Zac on 15/05/16.
  */
trait IMonitorWork {

  def getMonitoringReport(nodeId: String) : Option[WorkerNodeMonitoringReport]
}

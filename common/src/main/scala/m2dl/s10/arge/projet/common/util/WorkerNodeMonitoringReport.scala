package m2dl.s10.arge.projet.common.util

import java.time.LocalDateTime

/**
  * Created by Zac on 15/05/16.
  */
case class WorkerNodeMonitoringReport(nodeId: String, created: LocalDateTime = LocalDateTime.now(), cpuLoad: Double, memoryUsage: Double) {
  def toMultiLineString =
    s"""monitoring node {
       |nodeId: $nodeId
       |date: $created
       |cpuLoad: $cpuLoad
       |memoryUsage: $memoryUsage
     """.stripMargin
}

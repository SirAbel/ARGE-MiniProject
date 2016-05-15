package m2dl.s10.arge.projet.loadbalancer.core.actors

import java.net.URL
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.typesafe.config.Config
import m2dl.s10.arge.projet.common.util.{WorkerNodeMonitoringReport, XMLRPCClient}
import m2dl.s10.arge.projet.loadbalancer.core.protocol.LoadBalancerProtocol._

import scala.collection.immutable.HashMap
import scala.concurrent.duration.FiniteDuration

/**
  * Created by Zac on 14/05/16.
  */
object MonitoringActor {
   case class MonitoringBlock(totalStats: HashMap[String, (Double,Double)], currentCycle: Int, conf: Config) {

     val cycle = 5.0
     val minimumMemoryUsage = conf.getInt("app.openstack.defaults.minimumMemoryUsage")
     val maximumMemoryUsage = conf.getInt("app.openstack.defaults.maximumMemoryUsage")

     val minimumCpuLoad = conf.getInt("app.openstack.defaults.minimumCpuLoad")
     val maximumCpuLoad = conf.getInt("app.openstack.defaults.maximumCpuLoad")

     def getOperation: (Option[Operation],Seq[String]) = {
       var averageStats = HashMap.empty[String, (Double,Double)]

       totalStats.foreach {
         case (id, values) =>
           val averageCpu = values._1 / cycle
           val averageMemory = values._2 / cycle
           val averageValue = averageCpu -> averageMemory

           averageStats = averageStats + ((id, averageValue))
       }

       val overloadedNodes = averageStats.count {
         case (id, values) => values._1 >= maximumMemoryUsage || values._2 >= maximumMemoryUsage
       }

       val idleNodes = averageStats.filter {
         case (id, values) => values._1 < minimumCpuLoad || values._2 < minimumMemoryUsage
       }

       if (totalStats.nonEmpty && (overloadedNodes / totalStats.size) > 0.5) {
         Some(AddWorkerInstance) -> Seq.empty
       } else if (totalStats.nonEmpty && (idleNodes.size / totalStats.size) > 0.5) {
         Some(DeleteWorkerInstance("")) -> idleNodes.keys.take(2).toSeq
       } else None -> Seq.empty
     }
   }
}

class MonitoringActor(manager: ActorRef) extends Actor with ActorLogging{

  var registeredWorkerNodes: HashMap[String, String] = HashMap.empty
  val schedulerInterval = context.system.settings.config.getInt("app.openstack.defaults.schedulerInterval")
  var monitoringBlock = MonitoringActor.MonitoringBlock(HashMap.empty,0,context.system.settings.config)

  // -------------------------------------------------------------------------------------------------------------------
  // -------------------------------------- Hooks ----------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------

  override def preStart = {
    log.debug("Starting watcher actor.")
    super.preStart
  }

  override def postStop = {
    super.postStop
    log.debug("watcher actor shutdown")
  }

  // -------------------------------------------------------------------------------------------------------------------
  // ------------------------------------- Handlers --------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------

  override def receive: Receive = {
    case StartMonitoring =>
      log.debug("Starting worker nodes monitoring session")
      val reports = registeredWorkerNodes.map {
        case (id, workerNodeUrl) =>
        fetchWorkerNodeStats(id, workerNodeUrl)
      }
      analyzeMonitoringReports(reports.flatten.toSeq)

      import context.dispatcher
      context.system.scheduler.scheduleOnce(FiniteDuration(schedulerInterval,TimeUnit.MINUTES),self, StartMonitoring)

    case RegisterNode(workerNode) if !registeredWorkerNodes.contains(workerNode.nodeId) =>
      registeredWorkerNodes = registeredWorkerNodes + ((workerNode.nodeId, workerNode.serverUrl))

    case UnregisterNode(workerNodeId) if registeredWorkerNodes.contains(workerNodeId) =>
      registeredWorkerNodes = registeredWorkerNodes - workerNodeId
  }

  // -------------------------------------------------------------------------------------------------------------------
  // ---------------------------------- Helper methods -----------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------

  def fetchWorkerNodeStats(workerNodeId: String, workerNodeUrl: String) : Option[WorkerNodeMonitoringReport] = {
    val url = new URL(workerNodeUrl)
    val params = Array.fill[String](1)(workerNodeId)

    XMLRPCClient.send(url,"monitor", params.asInstanceOf[Array[Object]],classOf[Option[WorkerNodeMonitoringReport]])
  }

  def analyzeMonitoringReports(reports: Seq[WorkerNodeMonitoringReport]): Unit = {

    var stats = monitoringBlock.totalStats

    reports.foreach { report =>
      stats = stats.get(report.nodeId) match {
        case Some(value) =>
          stats.updated(report.nodeId, (value._1 + report.cpuLoad) -> (value._2 + report.memoryUsage))
        case None =>
          val value = report.cpuLoad -> report.memoryUsage
          stats + ((report.nodeId, value))
      }
    }

    monitoringBlock = monitoringBlock.copy(
      totalStats = stats,
      currentCycle = (monitoringBlock.currentCycle + 1) % 5
    )

    if(monitoringBlock.currentCycle == 0) {
      val (operation, nodes) = monitoringBlock.getOperation

      operation match {
        case Some(DeleteWorkerInstance(_)) =>
          nodes.foreach(manager ! DeleteWorkerInstance(_))

        case Some(AddWorkerInstance) =>
          manager ! AddWorkerInstance

        case _ =>
      }
    }
  }
}

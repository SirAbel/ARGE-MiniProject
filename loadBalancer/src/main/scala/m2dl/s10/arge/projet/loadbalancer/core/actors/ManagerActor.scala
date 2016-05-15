package m2dl.s10.arge.projet.loadbalancer.core.actors

import akka.actor.{Actor, ActorLogging, Props}
import m2dl.s10.arge.projet.loadbalancer.core.model.WorkerNode
import m2dl.s10.arge.projet.loadbalancer.core.protocol.LoadBalancerProtocol._

import scala.collection.immutable.HashMap

/**
  * Created by Zac on 14/05/16.
  */
class ManagerActor extends Actor with ActorLogging {

  val monitoringActor = context.actorOf(Props[MonitoringActor], "StatsMonitor")
  val workDispatcher = context.actorOf(Props[WorkDispatcher], "WorkDispatcher")

  val minRunningNodes = context.system.settings.config.getInt("")

  var runningJobs: Set[String] = Set.empty
  var runningWorkerNodeInstances: HashMap[String, WorkerNode] = HashMap.empty
  var roundRobinWorkerNodesSelector: Iterator[String] = Iterator.continually(runningWorkerNodeInstances.keySet).flatten

  // -------------------------------------------------------------------------------------------------------------------
  // -------------------------------------- Hooks ----------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------

  override def preStart = {
    log.debug("Starting manager actor.")
    super.preStart
  }

  override def postStop = {
    super.postStop
    log.debug("manager actor shutdown")
  }

  // -------------------------------------------------------------------------------------------------------------------
  // ------------------------------------- Handlers --------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------

  override def receive: Receive = waitingJobCompletion orElse {

    case NewComputationJob(computationJob) =>
      log.debug("received computation request forwarding to worker nodes")
      runningJobs += computationJob.jobId
      val workerNodeId = roundRobinWorkerNodesSelector.next()
      runningWorkerNodeInstances.get(workerNodeId).collect {
        case workerNode =>
          val updatedNode = workerNode.copy(runningWorks = workerNode.runningWorks + 1)
        runningWorkerNodeInstances = runningWorkerNodeInstances.updated(workerNodeId,updatedNode)
        workDispatcher ! RunJobOnWorkerNode(sender(),updatedNode, computationJob)
      }

    case DeleteWorkerInstance(workerNodeId) if runningWorkerNodeInstances.contains(workerNodeId) =>
      log.debug("received node delete order - checking if operation can be performed...")

      if (runningWorkerNodeInstances.size > minRunningNodes) {
        runningWorkerNodeInstances.get(workerNodeId).collect {
          case workerNode if workerNode.runningWorks == 0 =>
            log.debug("deleting worker node...")
            deleteWorkerNodeInstance(workerNode)
          case workerNode =>
            log.debug("This worker node has running tasks - marking as pending")
            val updatedNode = workerNode.copy(pendingDelete = true)
            runningWorkerNodeInstances = runningWorkerNodeInstances.updated(workerNodeId, updatedNode)
        }
      }

    case AddWorkerInstance =>
      log.debug("received watching report - increasing worker nodes...")
      val workerNode = createNewWorkerNodeInstance()
      runningWorkerNodeInstances = runningWorkerNodeInstances + ((workerNode.nodeId, workerNode))
      roundRobinWorkerNodesSelector = Iterator.continually(runningWorkerNodeInstances.keySet).flatten

  }

  def waitingJobCompletion: Receive = {

    case JobPerformed(workerNodeId: String, jobId: String) if runningJobs.contains(jobId) && runningWorkerNodeInstances.contains(workerNodeId) =>
      log.debug(s"Job [id=$jobId] - completed")
      runningJobs -= jobId
      runningWorkerNodeInstances.get(workerNodeId).collect {
        case workerNode =>
          val updatedWorkerNode = workerNode.copy(runningWorks = workerNode.runningWorks - 1)
          runningWorkerNodeInstances = runningWorkerNodeInstances.updated(workerNodeId,updatedWorkerNode)
          if(updatedWorkerNode.runningWorks == 0 && updatedWorkerNode.pendingDelete){
            deleteWorkerNodeInstance(updatedWorkerNode)
          }
      }
  }

  // -------------------------------------------------------------------------------------------------------------------
  // ---------------------------------- Helper methods -----------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------

  def createNewWorkerNodeInstance(): WorkerNode = ???

  def deleteWorkerNodeInstance(workerNode: WorkerNode): Unit = {
    //TODO OpenStack api request

    runningWorkerNodeInstances = runningWorkerNodeInstances - workerNode.nodeId
    roundRobinWorkerNodesSelector = Iterator.continually(runningWorkerNodeInstances.keySet).flatten

  }
}

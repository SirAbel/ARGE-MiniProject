package m2dl.s10.arge.projet.loadbalancer.core.actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool
import m2dl.s10.arge.projet.common.security.OpenStackUtils
import m2dl.s10.arge.projet.loadbalancer.core.model.WorkerNode
import m2dl.s10.arge.projet.loadbalancer.core.protocol.LoadBalancerProtocol._
import m2dl.s10.arge.projet.loadbalancer.core.util.LoadBalancerException
import org.openstack4j.model.compute.Addresses

import scala.collection.immutable.HashMap

/**
  * Created by Zac on 14/05/16.
  */
class ManagerActor extends Actor with ActorLogging {

  val (minRunningNodes, routerPoolSize) = {
    val conf = context.system.settings.config
    conf.getInt("app.openStack.defaults.minRunningNodes") -> conf.getInt("app.openStack.defaults.routerPoolSize")
  }

  val monitoringActor = context.actorOf(Props(classOf[MonitoringActor],self), "StatsMonitor")
  val workDispatcher = context.actorOf(RoundRobinPool(routerPoolSize).props(Props[WorkDispatcher]), "WorkDispatcher")

  var runningJobs: Set[String] = Set.empty
  var runningWorkerNodeInstances: HashMap[String, WorkerNode] = initRunningNodes(minRunningNodes)
  var runningDeletableWorkerInstances: HashMap[String, WorkerNode] = HashMap.empty
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
        case workerNode: WorkerNode =>
          val updatedNode = workerNode.copy(runningWorks = workerNode.runningWorks + 1)
        runningWorkerNodeInstances = runningWorkerNodeInstances.updated(workerNodeId,updatedNode)
        workDispatcher ! RunJobOnWorkerNode(sender(),updatedNode, computationJob)
      }


    case DeleteWorkerInstance(workerNodeId) if runningWorkerNodeInstances.contains(workerNodeId) =>
      log.debug("received node delete order - checking if operation can be performed...")

      if (runningWorkerNodeInstances.size > minRunningNodes) {
        runningWorkerNodeInstances.get(workerNodeId).collect {
          case workerNode: WorkerNode if workerNode.runningWorks == 0 =>
            log.debug("deleting worker node...")
            deleteWorkerNodeInstance(workerNode)
            runningWorkerNodeInstances = runningWorkerNodeInstances - workerNode.nodeId
            roundRobinWorkerNodesSelector = Iterator.continually(runningWorkerNodeInstances.keySet).flatten
          case workerNode: WorkerNode =>
            log.debug("This worker node has running tasks - marking as pending")
            val updatedNode = workerNode.copy(pendingDelete = true)
            runningWorkerNodeInstances = runningWorkerNodeInstances - updatedNode.nodeId
            roundRobinWorkerNodesSelector = Iterator.continually(runningWorkerNodeInstances.keySet).flatten
            runningDeletableWorkerInstances = runningDeletableWorkerInstances + ((updatedNode.nodeId, updatedNode))
        }
      }

    case AddWorkerInstance =>
      log.debug("received watching report - increasing worker nodes...")
      val workerNode = createNewWorkerNodeInstance()
      runningWorkerNodeInstances = runningWorkerNodeInstances + ((workerNode.nodeId, workerNode))
      roundRobinWorkerNodesSelector = Iterator.continually(runningWorkerNodeInstances.keySet).flatten
      monitoringActor ! RegisterNode(workerNode)

  }

  def waitingJobCompletion: Receive = {

    case JobPerformed(workerNodeId: String, jobId: String) if runningJobs.contains(jobId) && runningWorkerNodeInstances.contains(workerNodeId) =>
      log.debug(s"Job [id=$jobId] - completed")
      runningJobs -= jobId
      runningWorkerNodeInstances.get(workerNodeId).collect {
        case workerNode: WorkerNode =>
          val updatedWorkerNode = workerNode.copy(runningWorks = workerNode.runningWorks - 1)
          runningWorkerNodeInstances = runningWorkerNodeInstances.updated(workerNodeId,updatedWorkerNode)
      }

    case JobPerformed(workerNodeId: String, jobId: String) if runningJobs.contains(jobId) && runningDeletableWorkerInstances.contains(workerNodeId) =>
      log.debug(s"Job [id=$jobId] - completed")
      runningJobs -= jobId
      runningDeletableWorkerInstances.get(workerNodeId).collect {
        case workerNode: WorkerNode =>
          val updatedWorkerNode = workerNode.copy(runningWorks = workerNode.runningWorks - 1)
          runningDeletableWorkerInstances = runningDeletableWorkerInstances.updated(workerNodeId,updatedWorkerNode)
          if(updatedWorkerNode.runningWorks == 0){
            deleteWorkerNodeInstance(updatedWorkerNode)
            runningDeletableWorkerInstances = runningDeletableWorkerInstances - workerNode.nodeId
          }
      }
  }

  // -------------------------------------------------------------------------------------------------------------------
  // ---------------------------------- Helper methods -----------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------

  private def createNewWorkerNodeInstance(): WorkerNode = {
    val prefix: String = "zzya-tbla-WorkerNodeInstance-"
    val userData = ""

    val client = OpenStackUtils.authenticate()

    val server = OpenStackUtils.createServer(client, prefix, userData)
    val serverUrlOption = Option(server.getAddresses).collect {
      case addresses: Addresses if !addresses.getAddresses().isEmpty =>
        addresses.getAddresses().values().iterator().next().get(0).getAddr
    }

    val serverUrl = serverUrlOption.getOrElse{
      OpenStackUtils.delete(client, server.getId)
      throw new LoadBalancerException.MissingServerAddress(s"The server=[${server.getName}] does not have any address")
    }

    val node = WorkerNode(server.getId, serverUrl)
    log.info(s"successfully created node: ${node.toMultilineString}")
    node
  }

  private def deleteWorkerNodeInstance(workerNode: WorkerNode): Unit = {
    val client = OpenStackUtils.authenticate()

    OpenStackUtils.delete(client,workerNode.nodeId)

    log.info(s"successfully deleted node: ${workerNode.toMultilineString}")
    monitoringActor ! UnregisterNode(workerNode.nodeId)

  }

  private def initRunningNodes(minRunningNodes: Int) = {
    var map = HashMap.empty[String, WorkerNode]

    for (k <- 0 until minRunningNodes) {
      val temp = createNewWorkerNodeInstance()
      map = map + ((temp.nodeId, temp))
      monitoringActor ! RegisterNode(temp)
    }

    monitoringActor ! StartMonitoring
    map
  }

  ////////////////////////////////////////methods for local execution///////////////////////////////////////////////////
  // these methods do not use openStack for node creation/delete since the authentication fails when trying to        //
  // connect with the credentials in the configuration                                                                //
  // Usage:                                                                                                           //
  // uncomment these methods and comment the ones above with the same names                                           //
  // for a node delete simulation use the loadBalancerEntryPoint main method to send a DeleteNode message to the      //
  // managerActor                                                                                                     //
  // This solution is here only to help check the behaviour of the works framework without the need to use the        //
  // OpenStack layer required in the deleteWorkerNodeInstance and deleteWorkerNodeInstance methods                    //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /*private def createNewWorkerNodeInstance(): WorkerNode = {

    val temp = WorkerNode("local"+Random.nextInt(100),"http://localhost:9876")
    log.debug(s"successfully created node: ${temp.toMultilineString}")
    temp
  }

  private def deleteWorkerNodeInstance(workerNode: WorkerNode): Unit = {

    log.debug(s"successfully deleted node: ${workerNode.toMultilineString}")
    monitoringActor ! UnregisterNode(workerNode.nodeId)

  }

  private def initRunningNodes(minRunningNodes: Int) = {
    var map = HashMap.empty[String, WorkerNode]

    for (k <- 0 until minRunningNodes) {
      val temp = createNewWorkerNodeInstance()
      map = map + ((temp.nodeId, temp))
      monitoringActor ! RegisterNode(temp)
    }

    monitoringActor ! StartMonitoring
    //the node with the id "local" can be used for a node delete simulation
    monitoringActor ! RegisterNode(WorkerNode("local", "http://localhost:9876"))
    map + ("local" -> WorkerNode("local", "http://localhost:9876"))
  }*/

}

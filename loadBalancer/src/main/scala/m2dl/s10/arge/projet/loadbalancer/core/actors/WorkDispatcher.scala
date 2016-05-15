package m2dl.s10.arge.projet.loadbalancer.core.actors

import java.net.URL

import akka.actor.{Actor, ActorLogging}
import m2dl.s10.arge.projet.common.util.XMLRPCClient
import m2dl.s10.arge.projet.loadbalancer.core.model.{JobDescription, JobOutcomeType}
import m2dl.s10.arge.projet.loadbalancer.core.protocol.LoadBalancerProtocol.{ComputationJobOutcome, JobPerformed, RunJobOnWorkerNode}

/**
  * Created by Zac on 14/05/16.
  */
class WorkDispatcher extends Actor with ActorLogging {


  // -------------------------------------------------------------------------------------------------------------------
  // -------------------------------------- Hooks ----------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------

  override def preStart = {
    log.debug("Starting work dispatcher actor.")
    super.preStart
  }

  override def postStop = {
    super.postStop
    log.debug("Work dispatcher shutdown")
  }

  // -------------------------------------------------------------------------------------------------------------------
  // ------------------------------------- Handlers --------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------

  override def receive: Receive = {
    case RunJobOnWorkerNode(client, workerNode,computationJob) =>
      log.debug("received work request...")
      val (result, outcome) = runTask(workerNode.hostname,computationJob.jobDescription)
      sender() ! JobPerformed(workerNode.nodeId, computationJob.jobId)
      client ! ComputationJobOutcome(computationJob.jobId, outcome,result)
  }

  // -------------------------------------------------------------------------------------------------------------------
  // ---------------------------------- Helper methods -----------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------

  def runTask(endPointUrlAsString: String ,jobDescription: JobDescription) = {

    try {
      val endPointUrl = new URL(endPointUrlAsString)
      val result = XMLRPCClient.send(endPointUrl,jobDescription.handlerName, jobDescription.params, jobDescription.responseType)
      result -> JobOutcomeType.Success
    } catch {
      case e: Exception => None -> JobOutcomeType.Failure
    }
  }
}

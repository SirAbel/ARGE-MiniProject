package m2dl.s10.arge.projet.loadbalancer.core.protocol

import java.math.BigDecimal

import akka.actor.ActorRef
import m2dl.s10.arge.projet.loadbalancer.core.model.{ComputationJob, WorkerNode}
import m2dl.s10.arge.projet.loadbalancer.core.model.JobOutcomeType.JobOutcomeType

/**
  * Created by Zac on 14/05/16.
  */
object LoadBalancerProtocol {

  //Job request messages
  case class NewComputationJob(computationJob: ComputationJob)
  case class RunJobOnWorkerNode(client:ActorRef, workerNode: WorkerNode, computationJob: ComputationJob)
  case class JobPerformed(workerNodeId: String, jobId: String)
  case class ComputationJobOutcome(jobId: String, status: JobOutcomeType, result: Option[BigDecimal])

  //Monitoring messages
  case class RegisterNode(workerNode: WorkerNode)
  case class UnregisterNode(workerNodeId: String)
  case object StartMonitoring

  sealed trait Operation
  case class DeleteWorkerInstance(workerNodeId: String) extends Operation
  case object AddWorkerInstance extends Operation

}

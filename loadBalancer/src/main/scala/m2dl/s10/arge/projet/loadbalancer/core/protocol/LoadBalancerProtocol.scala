package m2dl.s10.arge.projet.loadbalancer.core.protocol

import java.math.BigDecimal

import m2dl.s10.arge.projet.loadbalancer.core.model.ComputationJob
import m2dl.s10.arge.projet.loadbalancer.core.model.JobOutcomeType.JobOutcomeType

/**
  * Created by Zac on 14/05/16.
  */
object LoadBalancerProtocol {

  //Job request messages
  case class NewComputationJob(computationJob: ComputationJob)
  case class ComputationJobOutcome(jobId: String, status: JobOutcomeType, result: Option[BigDecimal])

  //Monitoring messages
  case object GetNodesStats
  case object DeleteWorkerInstance
  case object AddWorkerInstance

}

package m2dl.s10.arge.projet.loadbalancer.core.model

import java.util.UUID

/**
  * Created by Zac on 14/05/16.
  */

object ComputationJob {

  def idGenerator = UUID.randomUUID().toString

}

case class ComputationJob(jobId: String = ComputationJob.idGenerator, jobDescription: JobDescription)

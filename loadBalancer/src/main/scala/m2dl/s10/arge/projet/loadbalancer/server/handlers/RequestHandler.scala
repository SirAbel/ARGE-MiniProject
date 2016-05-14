package m2dl.s10.arge.projet.loadbalancer.server.handlers

import java.math.BigDecimal
import java.util.concurrent.{TimeUnit, TimeoutException}

import akka.pattern.ask
import akka.util.Timeout
import m2dl.s10.arge.projet.common.util.IComputationWork
import m2dl.s10.arge.projet.loadbalancer.core.model.{ComputationJob, JobDescription, JobOutcomeType}
import m2dl.s10.arge.projet.loadbalancer.core.protocol.LoadBalancerProtocol.ComputationJobOutcome
import m2dl.s10.arge.projet.loadbalancer.server.LoadBalancerEntryPoint

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Zac on 13/05/16.
  */
class RequestHandler extends IComputationWork{

  override def getPIWithDecimals(nbDecimals: Int): Option[BigDecimal] = {
    val jobDescription = JobDescription("compute", new Array(nbDecimals))
    val computationJob = ComputationJob(jobDescription = jobDescription)

    implicit val timeout = Timeout(FiniteDuration(30, TimeUnit.SECONDS))
    val future = LoadBalancerEntryPoint.manager.ask(computationJob).mapTo[ComputationJobOutcome].flatMap {
      case ComputationJobOutcome(jobId,status,outcome) if status == JobOutcomeType.Success =>
        Future.successful(outcome)

      case ComputationJobOutcome(jobId,status,outcome) =>
        Future.successful(Option.empty[BigDecimal])
    }.recover {
      case e: TimeoutException =>
        Option.empty[BigDecimal]
    }

    Await.result(future, timeout.duration)
  }
}
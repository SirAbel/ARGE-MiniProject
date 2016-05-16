package m2dl.s10.arge.projet.loadbalancer.server.handlers

import java.math.BigDecimal
import java.util.concurrent.{TimeUnit, TimeoutException}

import akka.pattern.ask
import akka.util.Timeout
import m2dl.s10.arge.projet.common.IComputationWork
import m2dl.s10.arge.projet.loadbalancer.core.model.{ComputationJob, JobDescription, JobOutcomeType}
import m2dl.s10.arge.projet.loadbalancer.core.protocol.LoadBalancerProtocol.{ComputationJobOutcome, NewComputationJob}
import m2dl.s10.arge.projet.loadbalancer.server.LoadBalancerEntryPoint

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Zac on 13/05/16.
  */
class RequestHandler extends IComputationWork{

  override def getPIWithDecimals(nbDecimals: Int): Option[BigDecimal] = {
    val params = Array.fill[Object](1)(Int.box(nbDecimals))
    val jobDescription = JobDescription("compute.getPIWithDecimals", params, classOf[Option[BigDecimal]])
    val computationJob = ComputationJob(jobDescription = jobDescription)

    implicit val timeout = Timeout(FiniteDuration(30, TimeUnit.SECONDS))

    val future = LoadBalancerEntryPoint.manager.ask(NewComputationJob(computationJob)).mapTo[ComputationJobOutcome].flatMap {
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

package m2dl.s10.arge.projet.loadbalancer.core.actors

import akka.actor.{Actor, ActorLogging, Props}
import m2dl.s10.arge.projet.loadbalancer.core.protocol.LoadBalancerProtocol._

/**
  * Created by Zac on 14/05/16.
  */
class ManagerActor extends Actor with ActorLogging {

  val watcherActor = context.actorOf(Props[MonitoringActor], "StatsMonitor")
  //Iterator.continually(List(1, 2, 3, 4)).flatten TODO roundRobing load division

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

    case DeleteWorkerInstance =>
      log.debug("received watching report - removing free worker nodes...")

    case AddWorkerInstance =>
      log.debug("received watching report - increasing worker nodes...")

  }

  def waitingJobCompletion: Receive = ???

  // -------------------------------------------------------------------------------------------------------------------
  // ---------------------------------- Helper methods -----------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------

}

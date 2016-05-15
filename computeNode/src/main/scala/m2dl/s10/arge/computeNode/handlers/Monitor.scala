package m2dl.s10.arge.computeNode.handlers

import m2dl.s10.arge.projet.common.IMonitorWork
import m2dl.s10.arge.projet.common.util.WorkerNodeMonitoringReport
import oshi.SystemInfo
import oshi.hardware.{CentralProcessor, GlobalMemory}

/**
  * Created by Zac on 15/05/16.
  */
class Monitor extends IMonitorWork {

  override def getMonitoringReport(nodeId: String): Option[WorkerNodeMonitoringReport] = {
    for {
      cpuLoad <- Option(cpuLoad())
      memoryUsage <- Option(memoryUsagePercentage)
    } yield WorkerNodeMonitoringReport(nodeId,cpuLoad = cpuLoad,memoryUsage = memoryUsagePercentage)
  }

  private def cpuLoad(): Double = {
    val systemInfo: SystemInfo = new SystemInfo
    val processors: CentralProcessor = systemInfo.getHardware.getProcessor
    val cpuLoad: Double = processors.getSystemCpuLoad
    if (cpuLoad < 0) {
      throw new IllegalStateException("Could not get the CPU load because of an unexpected error")
    }
    cpuLoad
  }

  private def memoryUsagePercentage: Double = {
    val systemInfo: SystemInfo = new SystemInfo
    val memory: GlobalMemory = systemInfo.getHardware.getMemory

    val memoryAvailable: Double = memory.getAvailable
    val totalMemory: Double = memory.getTotal

    val availableMemoryPercentage = memoryAvailable / totalMemory
    val result = 1.toDouble - availableMemoryPercentage

    BigDecimal(result).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
  }
}

package de.firegate.unitycloudbuild.actors

import akka.actor.Actor
import de.firegate.tools.LogTrait
import de.firegate.unitycloudbuild.entities.HookRequest

class PrintProjectDetailsActor extends Actor with LogTrait {

  def receive = {
    case data: HookRequest ⇒ print(data)
    case _ ⇒ logger.warn("received unknown message")
  }

  def print(data: HookRequest): Unit = {
    logger.info("Project: " + data.projectName)
    logger.info("Target: " + data.buildTargetName)
    logger.info("Started by: " + data.startedBy)
    logger.info("Build status: " + data.buildStatus)
  }
}

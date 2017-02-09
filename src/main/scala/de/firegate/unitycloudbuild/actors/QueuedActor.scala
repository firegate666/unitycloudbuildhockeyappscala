package de.firegate.unitycloudbuild.actors

import akka.actor.Actor
import de.firegate.tools.LogTrait
import de.firegate.unitycloudbuild.entities.ProjectBuildQueuedRequest

class QueuedActor extends Actor with LogTrait {

  def receive = {
    case data: ProjectBuildQueuedRequest ⇒ logger.info(s"handle queued ${data.projectName}:${data.buildStatus}")
    case _ ⇒ logger.warn("received unknown message")
  }
}
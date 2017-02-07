package de.firegate.unitycloudbuild.actors

import akka.actor.Actor
import de.firegate.unitycloudbuild.entities.ProjectBuildQueuedRequest

class QueuedActor extends Actor {

  def receive = {
    case data: ProjectBuildQueuedRequest ⇒ println(s"handle queued ${data.projectName}:${data.buildStatus}")
    case _ ⇒ println("received unknown message")
  }
}
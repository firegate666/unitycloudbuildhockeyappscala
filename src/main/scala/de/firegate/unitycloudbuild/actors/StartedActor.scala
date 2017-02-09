package de.firegate.unitycloudbuild.actors

import akka.actor.Actor
import de.firegate.tools.LogTrait
import de.firegate.unitycloudbuild.entities.ProjectBuildStartedRequest

class StartedActor extends Actor with LogTrait {

   def receive = {
     case data: ProjectBuildStartedRequest ⇒ logger.info(s"handle started ${data.projectName}:${data.buildStatus}")
     case _ ⇒ logger.warn("received unknown message")
   }
 }
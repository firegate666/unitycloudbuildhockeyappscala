package de.firegate.unitycloudbuild.actors

import akka.actor.Actor
import de.firegate.tools.LogTrait
import de.firegate.unitycloudbuild.entities.ProjectBuildCanceledRequest

class CanceledActor extends Actor with LogTrait {

   def receive = {
     case data: ProjectBuildCanceledRequest ⇒ logger.info(s"handle canceled ${data.projectName}:${data.buildStatus}")
     case _ ⇒ logger.warn("received unknown message")
   }
 }
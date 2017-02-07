package de.firegate.unitycloudbuild.actors

import akka.actor.Actor
import de.firegate.unitycloudbuild.entities.{ProjectBuildStartedRequest, ProjectBuildCanceledRequest}

class StartedActor extends Actor {

   def receive = {
     case data: ProjectBuildStartedRequest ⇒ println(s"handle started ${data.projectName}:${data.buildStatus}")
     case _ ⇒ println("received unknown message")
   }
 }
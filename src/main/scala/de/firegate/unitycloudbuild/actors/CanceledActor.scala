package de.firegate.unitycloudbuild.actors

import akka.actor.Actor
import de.firegate.unitycloudbuild.entities.{ProjectBuildCanceledRequest, ProjectBuildSuccessRequest}

class CanceledActor extends Actor {

   def receive = {
     case data: ProjectBuildCanceledRequest ⇒ println(s"handle canceled ${data.projectName}:${data.buildStatus}")
     case _ ⇒ println("received unknown message")
   }
 }
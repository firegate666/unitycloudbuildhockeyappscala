package de.firegate.unitycloudbuild.actors

import akka.actor.Actor
import de.firegate.unitycloudbuild.entities.{ProjectBuildQueuedRequest, HookRequest}

class PrintProjectDetailsActor extends Actor {

  def receive = {
    case data: HookRequest ⇒ print(data)
    case _ ⇒ println("received unknown message")
  }

  def print(data: HookRequest): Unit = {
    println("Project: " + data.projectName)
    println("Target: " + data.buildTargetName)
    println("Started by: " + data.startedBy)
    println("Build status: " + data.buildStatus)
  }
}

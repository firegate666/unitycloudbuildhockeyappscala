package de.firegate.unitycloudbuild.actors

import akka.actor.{Props, Actor}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.ActorMaterializer
import de.firegate.tools.{JsonUtil, FutureResponseHandler}
import de.firegate.unitycloudbuild.entities.{ProjectBuildStartedRequest, ProjectBuildCanceledRequest, ProjectBuildQueuedRequest, ProjectBuildSuccessRequest}
import de.firegate.unitycloudbuild.Options
import scala.concurrent.Future

case class HandleBuildActorMessage(buildUrl: String, buildStatus: String)

class HandleBuildActor extends Actor {

  implicit val system = this.context.system
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val queuedActor = system.actorOf(Props[QueuedActor], name = "queuedActor")
  val canceledActor = system.actorOf(Props[CanceledActor], name = "canceledActor")
  val successActor = system.actorOf(Props[SuccessActor], name = "successActor")
  val startedActor = system.actorOf(Props[StartedActor], name = "startedActor")

  def receive = {
    case data: HandleBuildActorMessage ⇒ handleBuild(data.buildUrl, data.buildStatus)
    case _ ⇒ println("received unknown message")
  }

  def handleBuild(buildUrl: String, buildStatus: String): Unit = {
    println("Connect to " + buildUrl)
    println("Auth " + Options.unityCloudAPIKey)

    val request = HttpRequest(uri = buildUrl)
      .withHeaders(RawHeader("Authorization", "Basic " + Options.unityCloudAPIKey))

    val futureResponse: Future[HttpResponse] = Http().singleRequest(request)
    val body = FutureResponseHandler.getBody(futureResponse)

    buildStatus match {
      case "queued" => queuedActor ! JsonUtil.fromJson[ProjectBuildQueuedRequest](body)
      case "canceled" => canceledActor ! JsonUtil.fromJson[ProjectBuildCanceledRequest](body)
      case "started" => startedActor ! JsonUtil.fromJson[ProjectBuildStartedRequest](body)
      case "success" => successActor ! JsonUtil.fromJson[ProjectBuildSuccessRequest](body)
      case "sentToBuilder" => println("sentToBuilder not implemented yet")
      case _ => println("unknown build status " + buildStatus)
    }
  }
}

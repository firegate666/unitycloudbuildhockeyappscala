package de.firegate.unitycloudbuild.actors

import akka.actor.{Props, Actor}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.ActorMaterializer
import de.firegate.tools.{LogTrait, JsonUtil, FutureResponseHandler}
import de.firegate.unitycloudbuild.entities.{ProjectBuildStartedRequest, ProjectBuildCanceledRequest, ProjectBuildQueuedRequest, ProjectBuildSuccessRequest}
import de.firegate.unitycloudbuild.UnityCloudBuildOptions
import scala.concurrent.Future

case class HandleBuildActorMessage(buildUrl: String, buildStatus: String)

class HandleBuildActor extends Actor with LogTrait {

  implicit val system = this.context.system
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val queuedActorRef = system.actorOf(Props[QueuedActor], name = "queuedActor")
  val canceledActorRef = system.actorOf(Props[CanceledActor], name = "canceledActor")
  val successActorRef = system.actorOf(Props[SuccessActor], name = "successActor")
  val startedActorRef = system.actorOf(Props[StartedActor], name = "startedActor")

  def receive = {
    case data: HandleBuildActorMessage ⇒ handleBuild(data.buildUrl, data.buildStatus)
    case _ ⇒ logger.warn("received unknown message")
  }

  def handleBuild(buildUrl: String, buildStatus: String): Unit = {
    logger.info("Connect to " + buildUrl)

    val request = HttpRequest(uri = buildUrl)
      .withHeaders(RawHeader("Authorization", "Basic " + UnityCloudBuildOptions.unityCloudAPIKey))

    val futureResponse: Future[HttpResponse] = Http().singleRequest(request)
    val body = FutureResponseHandler.getBody(futureResponse)

    buildStatus match {
      case "queued" => queuedActorRef ! JsonUtil.fromJson[ProjectBuildQueuedRequest](body)
      case "canceled" => canceledActorRef ! JsonUtil.fromJson[ProjectBuildCanceledRequest](body)
      case "started" => startedActorRef ! JsonUtil.fromJson[ProjectBuildStartedRequest](body)
      case "success" => successActorRef ! JsonUtil.fromJson[ProjectBuildSuccessRequest](body)
      case "sentToBuilder" => logger.warn("sentToBuilder not implemented yet")
      case _ => logger.warn("unknown build status " + buildStatus)
    }
  }
}

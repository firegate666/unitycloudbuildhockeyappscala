package de.firegate.unitycloudbuild.actors

import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpEntity, HttpRequest}
import akka.stream.ActorMaterializer
import akka.util.ByteString
import de.firegate.tools.{JsonUtil, LogTrait}
import de.firegate.unitycloudbuild.UnityCloudBuildOptions
import de.firegate.unitycloudbuild.entities.{ProjectBuildRequestProjectVersion, ProjectBuildSuccessRequest}

case class PermalinkActorMessage(data: ProjectBuildSuccessRequest, shareLink: String)

class PermalinkActor extends Actor with LogTrait {

  implicit val system = this.context.system
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  def receive = {
    case data: PermalinkActorMessage ⇒ publishShareLink(data.data, data.shareLink)
    case _ ⇒ logger.warn("received unknown message")
  }

  case class Payload(
    build: Int,
    buildtargetid: String,
    buildTargetName: String,
    platform: String,
    finished: String,
    projectName: String,
    projectId: String,
    projectVersion: ProjectBuildRequestProjectVersion,
    shareLink: String
  )

  def publishShareLink(data: ProjectBuildSuccessRequest, shareLink: String): Unit = {
    val payload = new Payload(
      data.build,
      data.buildtargetid,
      data.buildTargetName,
      data.platform,
      data.finished,
      data.projectName,
      data.projectId,
      data.projectVersion,
      shareLink
    )

    if (UnityCloudBuildOptions.permalinkApiUrl.nonEmpty) {
      val userData = ByteString(JsonUtil.toJson(payload))
      val entity = HttpEntity(`application/json`, userData)
      val request = HttpRequest(POST, uri = UnityCloudBuildOptions.permalinkApiUrl, entity = entity)
        .withHeaders(
          RawHeader("Authorization", "Basic " + UnityCloudBuildOptions.unityCloudAPIKey)
        )

      Http().singleRequest(request)
      logger.info("Publish share link finished")
    } else {
      logger.warn("Publish share disabled")
    }
  }
}

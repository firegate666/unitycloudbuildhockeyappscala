package de.firegate.unitycloudbuild.actors

import akka.actor.{ActorSystem, Actor}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.ActorMaterializer
import akka.util.ByteString
import de.firegate.tools.{LogTrait, JsonUtil, FutureResponseHandler}
import de.firegate.unitycloudbuild.entities.{ProjectBuildRequestProjectVersion, ProjectBuildSuccessRequest, ProjectBuildQueuedRequest}
import com.netaporter.uri.Uri.parse
import de.firegate.unitycloudbuild.UnityCloudBuildOptions
import HttpMethods._
import MediaTypes._

import scala.concurrent.Future

class SuccessActor extends Actor with LogTrait {

  implicit val system = this.context.system
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

   def receive = {
     case data: ProjectBuildSuccessRequest ⇒ handle(data)
     case _ ⇒ logger.warn("received unknown message")
   }

  def handle(data: ProjectBuildSuccessRequest): Unit = {
    logger.info(s"handle success ${data.projectName}:${data.buildStatus}")

    val href = data.links.download_primary.get.href
    val parsed = parse(href)
    val filename = parsed.pathParts.last.part.replaceAll("\\s", "_")

    logger.info(s"Found filename $filename")

    downloadBinary(href, filename)
    requestShareLink(data)
  }

  def downloadBinary(href: String, filename: String): Unit = {
    logger.info(s"Download binary $href to $filename")

  }

  case class ShareData(shareid: String)

  def requestShareLink(data: ProjectBuildSuccessRequest): Unit = {
    val shareAPIURL = data.links.create_share.get.href
    val method = data.links.create_share.get.method

    val url = UnityCloudBuildOptions.unityAPIBase + shareAPIURL

    val userData = ByteString(JsonUtil.toJson(data))
    val entity = HttpEntity(`application/json`, userData)

    val request = HttpRequest(uri = url, entity = entity)
      .withHeaders(
        RawHeader("Authorization", "Basic " + UnityCloudBuildOptions.unityCloudAPIKey)
      )
      .withMethod(HttpMethods.getForKey(method.toUpperCase).get)

    val futureResponse: Future[HttpResponse] = Http().singleRequest(request)
    val body = FutureResponseHandler.getBody(futureResponse)
    /*val shareData = JsonUtil.fromJson[ShareData](body)

    publishShareLink(data, Options.unityShareLinkBase + shareData.shareid)*/
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
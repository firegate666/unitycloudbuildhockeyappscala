package de.firegate.unitycloudbuild.actors

import java.io.{FileWriter, BufferedWriter, File}
import java.net.URI

import akka.actor.{Actor, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.Multipart.FormData.BodyPart
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.netaporter.uri.Uri.parse
import de.firegate.tools._
import de.firegate.unitycloudbuild.UnityCloudBuildOptions
import de.firegate.unitycloudbuild.entities.{ProjectBuildSuccessRequest, ShareData}
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class SuccessActor extends Actor with LogTrait {

  implicit val system = this.context.system
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  var permalinkActorRef = system.actorOf(Props[PermalinkActor], name = "permalinkActor")

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

    requestShareLink(data)
    downloadBinary(href, filename)
  }

  def uploadToHockeyApp(file: File): Unit = {
    logger.info("Upload to hockeyapp")

    val uri = new URI(UnityCloudBuildOptions.hockeyappAPIUpload)
    val properties = Map(
      "status" -> "2", // to make the version available for download
      "notes" -> "Automated release triggered from Unity Cloud Build.",
      "notes_type" -> "0",
      "notify" -> "0"
    )

    val header = Map(
      "X-HockeyAppToken" -> UnityCloudBuildOptions.hockeyappAPIKey,
      "Accept" -> "application/json"
    )

    val futureResponse = Uploader.run(uri, file, properties, header)

    logger.info("Upload started")

    futureResponse onComplete {
      case Success(response) => logger.info(s"Upload finished ${response.get}")
      case Failure(t) => logger.error(s"Error while uploading to hockey app ${t.getMessage}")
    }
  }

  def uploadToPlayStore(file: File): Unit = {
    logger.warn("Upload to GPS not implemented yet")
  }

  def downloadBinary(href: String, filename: String): Unit = {
    val tmpFileName = Tools.tmpFileName() + filename
    logger.info(s"Download binary $href to $tmpFileName")

    val request = HttpRequest(HttpMethods.GET, uri = href)
    val futureResponse: Future[HttpResponse] = Http().singleRequest(request)
    val body = FutureResponseHandler.getBody(futureResponse, 1.minutes)

    val file = new File(tmpFileName)
    val bw = new BufferedWriter(new FileWriter(file, false)) // explicit overwrite
    bw.write(body)
    bw.close()

    uploadToHockeyApp(file)
    uploadToPlayStore(file)
  }

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
    val shareData = JsonUtil.fromJson[ShareData](body)

    permalinkActorRef ! new PermalinkActorMessage(data, UnityCloudBuildOptions.unityShareLinkBase + shareData.shareid)
  }
}
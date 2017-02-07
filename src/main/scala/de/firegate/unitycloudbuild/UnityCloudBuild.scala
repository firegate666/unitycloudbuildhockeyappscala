package de.firegate.unitycloudbuild

import akka.actor.{Props, Actor, ActorSystem}
import akka.actor.Status.Success
import akka.actor.Status.Failure
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpRequest, StatusCodes, HttpResponse}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.util.ByteString
import de.firegate.unitycloudbuild.actors._
import de.firegate.{FutureResponseHandler, JsonUtil}
import de.firegate.unitycloudbuild.entities._
import scala.concurrent.{Await, Future}
import scala.io.StdIn
import scala.concurrent.duration._
import scala.language.postfixOps

object Options {
  val host: String = sys.env.getOrElse[String]("HOST", "0.0.0.0")
  val port: Int = sys.env.getOrElse[String]("PORT", "80") toInt
  val unityAPIBase = "https://build-api.cloud.unity3d.com/"
  val unityCloudAPIKey = sys.env.getOrElse[String]("UNITYCLOUD_KEY", "")
  val unityShareLinkBase = "https://developer.cloud.unity3d.com/share/"
  val hockeyappAPIUpload = "https://rink.hockeyapp.net/api/2/apps/upload"
  val hockeyappAPIKey = sys.env.get("HOCKEYAPP_KEY").toString
  val permalinkApiUrl = sys.env.get("PERMALINK_API_URL").toString
}

object UnityCloudBuild {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val queuedActor = system.actorOf(Props[QueuedActor], name = "queuedActor")
  val canceledActor = system.actorOf(Props[CanceledActor], name = "canceledActor")
  val successActor = system.actorOf(Props[SuccessActor], name = "successActor")
  val startedActor = system.actorOf(Props[StartedActor], name = "startedActor")
  val projectDetailsActor = system.actorOf(Props[PrintProjectDetailsActor], name = "projectDetailsActor")

  def main(args: Array[String]): Unit = {
    val route =
      post {
        path("build") {
          entity(as[String]) { hookRequest =>
            val data = JsonUtil.fromJson[HookRequest](hookRequest)
            handleRequest(data)
            try {
              complete(StatusCodes.OK -> s"request handled for ${data.projectName}")
            } catch {
              case x: RuntimeException => complete(StatusCodes.InternalServerError -> x.getMessage)
            }
          }
        }
      }

    val bindingFuture = Http().bindAndHandle(route, Options.host, Options.port)

    println(s"Server online at http://${Options.host}:${Options.port}/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.terminate()) // and shutdown when done
  }

  def handleRequest(data: HookRequest): Unit = {
    projectDetailsActor ! data

    val buildAPIURL = data.links.api_self.href
    if (buildAPIURL == "") {
      throw new RuntimeException("No build link from Unity Cloud Build webhook")
    }

    handleBuild(Options.unityAPIBase + buildAPIURL, data.buildStatus)
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

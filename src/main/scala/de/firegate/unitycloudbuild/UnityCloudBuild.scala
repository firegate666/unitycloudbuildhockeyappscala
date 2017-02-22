package de.firegate.unitycloudbuild

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import de.firegate.tools.{JsonUtil, LogTrait}
import de.firegate.unitycloudbuild.actors._
import de.firegate.unitycloudbuild.entities._
import rapture.uri./

import scala.io.StdIn
import scala.language.postfixOps

object UnityCloudBuildOptions {
  val host: String = sys.env.getOrElse[String]("HOST", "0.0.0.0")
  val port: Int = sys.env.getOrElse[String]("PORT", "80") toInt
  val unityAPIBase = "https://build-api.cloud.unity3d.com/"
  val unityCloudAPIKey = sys.env.getOrElse[String]("UNITYCLOUD_KEY", "")
  val unityShareLinkBase = "https://developer.cloud.unity3d.com/share/"
  val hockeyappAPIUpload = "https://rink.hockeyapp.net/api/2/apps/upload"
  val hockeyappAPIKey = sys.env.getOrElse[String]("HOCKEYAPP_KEY", "")
  val permalinkApiUrl = sys.env.getOrElse[String]("PERMALINK_API_URL", "")
}

object UnityCloudBuild extends LogTrait {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val projectDetailsActorRef = system.actorOf(Props[PrintProjectDetailsActor], name = "projectDetailsActor")
  val handleBuildActorRef = system.actorOf(Props[HandleBuildActor], name = "handleBuildActor")

  def main(args: Array[String]): Unit = {
    val route =
      get {
        path("check") {
          complete("OK")
        }
      } ~
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

    val bindingFuture = Http().bindAndHandle(route, UnityCloudBuildOptions.host, UnityCloudBuildOptions.port)

    logger.info(s"Server online at http://${UnityCloudBuildOptions.host}:${UnityCloudBuildOptions.port}")

    /*StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.terminate()) // and shutdown when done
      */
  }

  def handleRequest(data: HookRequest): Unit = {
    projectDetailsActorRef ! data

    val buildAPIURL = data.links.api_self.href
    if (buildAPIURL == "") {
      throw new RuntimeException("No build link from Unity Cloud Build webhook")
    }

    handleBuildActorRef ! new HandleBuildActorMessage(UnityCloudBuildOptions.unityAPIBase + buildAPIURL, data.buildStatus)
  }
}

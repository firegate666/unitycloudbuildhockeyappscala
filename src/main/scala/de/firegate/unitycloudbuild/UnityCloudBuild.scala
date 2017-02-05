package de.firegate.unitycloudbuild

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{ModeledCustomHeaderCompanion, ModeledCustomHeader, RawHeader}
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, StatusCodes, HttpResponse}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import scala.concurrent.{Await, Future}
import scala.io.StdIn
import spray.json.DefaultJsonProtocol._
import spray.json._
import scala.concurrent.duration._


import scala.language.postfixOps
import scala.util.{Try, Success}

object Options {
  val host: String = sys.env.getOrElse[String]("HOST", "0.0.0.0")
  val port: Int = sys.env.getOrElse[String]("PORT", "80") toInt
  val unityAPIBase = "https://build-api.cloud.unity3d.com/"
  val unityCloudAPIKey = sys.env.get("UNITYCLOUD_KEY").toString
  val unityShareLinkBase = "https://developer.cloud.unity3d.com/share/"
  val hockeyappAPIUpload = "https://rink.hockeyapp.net/api/2/apps/upload"
  val hockeyappAPIKey = sys.env.get("HOCKEYAPP_KEY").toString
  val permalinkApiUrl = sys.env.get("PERMALINK_API_URL").toString
}

object UnityCloudBuild {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  // json formats
  implicit val apiEndpointFormat = jsonFormat2(ApiEndpoint)
  implicit val apiEndpointsFormat = jsonFormat5(ApiEndpoints)
  implicit val hookRequestFormat = jsonFormat10(HookRequest)

  def main(args: Array[String]): Unit = {

    val route =
      post {
        path("build") {
          entity(as[String]) { hookRequest =>
            val data = hookRequest.parseJson.convertTo[HookRequest]
            handleRequest(data)
            try {
              complete(StatusCodes.OK -> "request handled for " + data.projectName)
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
    printProjectDetails(data)

    val buildAPIURL = data.links.api_self.href
    if (buildAPIURL == "") {
      throw new RuntimeException("No build link from Unity Cloud Build webhook")
    }

    handleBuild(Options.unityAPIBase + buildAPIURL)
  }

  def handleBuild(buildUrl: String): Unit = {
    println("Connect to " + buildUrl)
    println("Auth " + Options.unityCloudAPIKey)

    val request = HttpRequest(uri = buildUrl)
    request.addHeader(BasicAuthHeader("Basic " + Options.unityCloudAPIKey))

    println(request.headers.toString())

    val futureResponse: Future[HttpResponse] = Http().singleRequest(request)

    val response = Await.result(futureResponse, 5.seconds)

    println(response.entity.toString)

  }


  def printProjectDetails(data: HookRequest): Unit = {
    println("Project: " + data.projectName)
    println("Target: " + data.buildTargetName)
    println("Started by: " + data.startedBy)
    println("Build status: " + data.buildStatus)
  }
}

final class BasicAuthHeader(token: String) extends ModeledCustomHeader[BasicAuthHeader] {
  override def renderInRequests = false
  override def renderInResponses = false
  override val companion = BasicAuthHeader
  override def value: String = token
}
object BasicAuthHeader extends ModeledCustomHeaderCompanion[BasicAuthHeader] {
  override val name = "Authorization"
  override def parse(value: String) = Try(new BasicAuthHeader(value))
}
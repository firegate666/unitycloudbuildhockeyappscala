package de.firegate.http

import akka.actor.{Props, Actor, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{StatusCodes, ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import spray.json.DefaultJsonProtocol._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.StdIn
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask

object SimpleServer {

  case class Bid(user: String, bid: Int)
  case object GetBids
  case class Bids(bids: List[Bid])

  class Auction extends Actor {
    def receive = {
      case Bid(userId, bid) => println(s"Bid complete: $userId, $bid")
      case _ => println("Invalid message")
    }
  }

  implicit val bidFormat = jsonFormat2(Bid)
  implicit val bidsFormat = jsonFormat1(Bids)

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val auction = system.actorOf(Props[Auction], "auction")

    val route =
      get {
        pathSingleSlash {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<html><body>Hello world!</body></html>"))
        } ~
          path("ping") {
            complete("PONG!")
          } ~
          path("pong") {
            complete("PING!")
          } ~
          path("hello") {
            parameter("name") { (name) =>
              complete("Hello " + name)
            }
          } ~
          path("auction") {
            parameters("bid", "user") { (bid, user) =>
              auction ! Bid(user, bid.toInt)
              complete((StatusCodes.Accepted, user + " is bidding " + bid))
            }
          }  ~
          path("auctions") {
            parameter("user") { (user) =>
              implicit val timeout: Timeout = 5.seconds

              // query the actor for the current auction state
              val bids: Future[Bids] = (auction ? GetBids).mapTo[Bids]
              complete(bids)
            }
          }
      }

    // `route` will be implicitly converted to `Flow` using `RouteResult.route2HandlerFlow`
    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.terminate()) // and shutdown when done
  }
}
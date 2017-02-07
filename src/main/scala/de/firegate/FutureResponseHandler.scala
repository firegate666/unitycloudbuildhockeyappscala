package de.firegate

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.stream.ActorMaterializer
import akka.util.ByteString
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

object FutureResponseHandler {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  def getBody(futureResponse: Future[HttpResponse]): String = {
    val response = Await.result(futureResponse, 5.seconds)

    val timeout = 300.millis
    val bs: Future[ByteString] = response.entity.toStrict(timeout).map { _.data }
    val futureBody: Future[String] = bs.map(_.utf8String) // if you indeed need a `String`
    Await.result(futureBody, 5.seconds)
  }
}

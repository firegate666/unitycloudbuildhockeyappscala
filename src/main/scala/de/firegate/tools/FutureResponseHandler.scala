package de.firegate.tools

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.stream.ActorMaterializer
import akka.util.ByteString

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object FutureResponseHandler {
   implicit val system = ActorSystem()
   implicit val materializer = ActorMaterializer()
   implicit val executionContext = system.dispatcher

   def getBody(futureResponse: Future[HttpResponse], timeout: FiniteDuration = 5.seconds): String = {
     val response = Await.result(futureResponse, timeout)

     val bs: Future[ByteString] = response.entity.toStrict(timeout).map { _.data }
     val futureBody: Future[String] = bs.map(_.utf8String) // if you indeed need a `String`
     Await.result(futureBody, timeout)
   }
 }

package de.firegate.tools

import java.io.File
import java.net.URI
import org.apache.http.client.methods.{HttpPost, CloseableHttpResponse}
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.{StringBody, FileBody}
import org.apache.http.entity.ContentType
import org.apache.http.util.EntityUtils
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.impl.client.HttpClients
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object Uploader extends LogTrait {

  lazy val httpClient = {
    val connManager = new PoolingHttpClientConnectionManager()
    HttpClients.custom().setConnectionManager(connManager).build()
  }

  def run(uri: URI, file: File, properties: Map[String, String], header: Map[String, String]): Future[Try[String]] = {
    import ExecutionContext.Implicits.global

    Future {
      Try({
        // Create the entity
        val reqEntity = MultipartEntityBuilder.create()

        // Attach the file
        reqEntity.addPart("ipa", new FileBody(file))

        for ((key, value) <- properties) {
          reqEntity.addPart(key, new StringBody(value, ContentType.TEXT_PLAIN))
        }

        // Create POST request
        val httpPost = new HttpPost(uri)
        httpPost.setEntity(reqEntity.build())

        for ((name, value) <- header) {
          httpPost.setHeader(name, value)
        }

        // Execute the request in a new HttpContext
        val ctx = HttpClientContext.create()
        val response: CloseableHttpResponse = httpClient.execute(httpPost, ctx)

        // Read the response
        val entity = response.getEntity
        val result = EntityUtils.toString(entity)

        // Close the response
        if (response != null) response.close()

        result
      })
    }
  }
}
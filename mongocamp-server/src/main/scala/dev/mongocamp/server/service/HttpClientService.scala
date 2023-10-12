package dev.mongocamp.server.service

import better.files.File
import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.exception.MongoCampException
import io.circe.parser.decode
import sttp.capabilities
import sttp.capabilities.pekko.PekkoStreams
import sttp.client3._
import sttp.client3.pekkohttp.PekkoHttpBackend
import sttp.model.{ Method, StatusCode }

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ Await, Future }
import scala.util.Random

object HttpClientService {
  private lazy val backend: SttpBackend[Future, PekkoStreams with capabilities.WebSockets] = PekkoHttpBackend()

  def additionalHeaderByHost(host: String): Map[String, String] = {
    val hostHeadersString                                = ConfigurationService.getConfigValue[String](DefaultConfigurations.ConfigKeyHttpClientHeaders)
    val hostHeadersMap: Map[String, Map[String, String]] = decode[Map[String, Map[String, String]]](hostHeadersString).getOrElse(Map())
    hostHeadersMap.getOrElse(host, Map())
  }

  def downloadToFile(url: String, downloadDirectory: File): File = {
    val uri      = uri"$url"
    val tempFile = File.temporaryFile().get()
    val request = basicRequest
      .method(Method.GET, uri)
      .followRedirects(true)
      .headers(additionalHeaderByHost(uri.host.getOrElse("")))
      .response(asFile(tempFile.toJava))
    val resultFuture   = backend.send(request)
    val responseResult = Await.result(resultFuture, 60.seconds)
    val response: File = File(responseResult.body.getOrElse(throw new MongoCampException("could not download file", StatusCode.PreconditionFailed)).getPath)
    val nameByPath     = uri.pathSegments.segments.lastOption.map(_.v)
    val nameByHeader   = responseResult.header("content-disposition").map(_.split("filename=").last)
    val fileName       = nameByHeader.getOrElse(nameByPath.getOrElse(Random.alphanumeric.take(10).mkString))
    val childFile      = downloadDirectory.createChild(s"${Random.alphanumeric.take(4).mkString}_$fileName")
    childFile.delete()
    response.moveTo(childFile)
  }

}

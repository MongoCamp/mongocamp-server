package dev.mongocamp.server.routes.file
import dev.mongocamp.server.config.Config
import sttp.tapir.{ fileBody, header }

object FileFunctions extends Config {
  val fileResult = fileBody
    .and(header[Long]("Content-Length"))
    .and(header[String]("Content-Disposition"))
    .and(header[String]("Content-Type"))
    .mapTo[FileResult]
    .and(header("cache-control", "max-age=" + globalConfigString("file.cache.age")))
}

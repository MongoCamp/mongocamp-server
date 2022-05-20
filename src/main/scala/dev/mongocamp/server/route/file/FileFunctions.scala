package dev.mongocamp.server.route.file
import dev.mongocamp.server.config.ConfigHolder
import sttp.tapir.{ fileBody, header }

object FileFunctions {
  val fileResult = fileBody
    .and(header[Long]("Content-Length"))
    .and(header[String]("Content-Disposition"))
    .and(header[String]("Content-Type"))
    .mapTo[FileResult]
    .and(header("cache-control", "max-age=" + ConfigHolder.fileCacheAge))
}

package dev.mongocamp.server.route.file

import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.service.ConfigurationService
import sttp.tapir.{ fileBody, header }
object FileFunctions {
  def fileResult = fileBody
    .and(header[Long]("Content-Length"))
    .and(header[String]("Content-Disposition"))
    .and(header[String]("Content-Type"))
    .mapTo[FileResult]
    .and(header("cache-control", "max-age=" + ConfigurationService.getConfigValue[String](DefaultConfigurations.ConfigKeyFileCache)))
}

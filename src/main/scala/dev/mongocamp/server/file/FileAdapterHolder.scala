package dev.mongocamp.server.file

import dev.mongocamp.server.auth.AuthHolder.globalConfigString
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.service.ReflectionService
import sttp.model.StatusCode

object FileAdapterHolder {

  lazy val fileHandlerType: String = globalConfigString("file.handler")

  def isGridfsHolder: Boolean = fileHandlerType.equalsIgnoreCase("gridfs")

  lazy val listOfFilePlugins: List[FilePlugin] = ReflectionService.instancesForType(classOf[FilePlugin])

  lazy val handler: FilePlugin = {
    listOfFilePlugins
      .find(_.name.equalsIgnoreCase(fileHandlerType))
      .getOrElse(throw MongoCampException("Unknown File Handler defined", StatusCode.InternalServerError))
  }

}

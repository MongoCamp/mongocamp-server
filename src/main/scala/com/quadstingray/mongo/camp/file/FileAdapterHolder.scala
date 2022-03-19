package com.quadstingray.mongo.camp.file

import com.quadstingray.mongo.camp.auth.AuthHolder.globalConfigString
import com.quadstingray.mongo.camp.exception.MongoCampException
import com.quadstingray.mongo.camp.service.ReflectionService
import sttp.model.StatusCode

object FileAdapterHolder {

  lazy val fileHandlerType: String = globalConfigString("file.handler")

  def isGridfsHolder: Boolean = fileHandlerType.equalsIgnoreCase("gridfs")

  lazy val listOfRoutePlugins: List[FilePlugin] = ReflectionService.instancesForType(classOf[FilePlugin])

  lazy val handler: FilePlugin = {
    listOfRoutePlugins
      .find(_.name.equalsIgnoreCase(fileHandlerType))
      .getOrElse(throw MongoCampException("Unknown File Handler defined", StatusCode.InternalServerError))
  }

}

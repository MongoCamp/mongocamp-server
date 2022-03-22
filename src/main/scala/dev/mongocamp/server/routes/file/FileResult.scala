package dev.mongocamp.server.routes.file

case class FileResult(file: java.io.File, contentLength: Long, contentDisposition: String, contentType: String)

object FileResult {
  def apply(file: better.files.File, fileName: Option[String] = None): FileResult = {
    val contentType = file.contentType.getOrElse("application/octet-stream")
    val contentDisposition = if (contentType.startsWith("image")) {
      "inline; "
    }
    else {
      "attachment; "
    }
    FileResult(
      file.toJava,
      file.byteArray.length,
      contentDisposition + "filename=\"%s\"".format(fileName.getOrElse(file.name)),
      contentType
    )
  }

}

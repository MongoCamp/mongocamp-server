package dev.mongocamp.server.model

import java.io.File
import sttp.model.Part

case class FileUploadForm(file: Part[File], metaData: String)

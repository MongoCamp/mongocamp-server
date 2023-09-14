package dev.mongocamp.server.model

import sttp.model.Part

import java.io.File

case class FileUploadForm(file: Part[File], metaData: String)

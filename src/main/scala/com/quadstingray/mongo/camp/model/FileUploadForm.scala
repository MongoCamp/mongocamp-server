package com.quadstingray.mongo.camp.model

import sttp.model.Part

import java.io.File

case class FileUploadForm(file: Part[File], metaData: String)

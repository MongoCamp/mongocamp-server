package dev.mongocamp.server.file

import better.files.File

trait FilePlugin {

  val name: String

  def getFile(bucket: String, fileId: String): File

  def deleteFile(bucket: String, fileId: String): Boolean

  def putFile(bucket: String, fileId: String, file: File): Boolean

  def size(bucket: String): Double

  def delete(bucket: String): Unit

  def clear(bucket: String): Boolean

}

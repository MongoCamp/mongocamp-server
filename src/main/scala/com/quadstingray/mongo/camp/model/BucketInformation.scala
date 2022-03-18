package com.quadstingray.mongo.camp.model

case class BucketInformation(name: String, files: Long, size: Double, avgObjectSize: Double)

object BucketInformation {
  val BucketCollectionSuffix   = ".files"
  val GridFsBucketChunksSuffix = ".chunks"
}
package dev.mongocamp.server.cli.exception

case class NativeBuildException(buildMessage: String) extends Exception("Image Generation failed")


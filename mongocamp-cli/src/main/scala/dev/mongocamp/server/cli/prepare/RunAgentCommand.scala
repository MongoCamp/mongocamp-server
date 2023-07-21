package dev.mongocamp.server.cli.prepare

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.cli.service.JvmStartService
import picocli.CommandLine.{Command, Parameters}

@Command(
  name = "runAgent",
  description = Array("Run Server with Agent and save generated Files to specified Folder")
)
class RunAgentCommand extends Runnable with LazyLogging {

  @Parameters(description = Array("Folder for generated Files"))
  var path: String = ""

  def run(): Unit = {
    JvmStartService.startServerWithAgent(path)
  }

}

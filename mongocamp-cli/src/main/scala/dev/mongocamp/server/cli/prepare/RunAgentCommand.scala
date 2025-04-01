package dev.mongocamp.server.cli.prepare

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.cli.service.JvmStartService
import dev.mongocamp.server.converter.CirceSchema
import io.circe.parser.decode
import picocli.CommandLine.{ Command, Parameters }

import java.util.concurrent.Callable
@Command(
  name = "runAgent",
  description = Array("Run Server with Agent and save generated Files to specified Folder")
)
class RunAgentCommand extends Callable[Integer] with LazyLogging with CirceSchema {

  @Parameters(description = Array("Folder for generated Files"))
  var path: String = ""

  def call(): Integer = {
    sys.addShutdownHook(
      () => {
        logger.warn("Cleanup Reflect Config")
        val file       = better.files.File("./mongocamp-server/src/main/resources/META-INF/native-image/dev.mongocamp/mongocamp-server/reflect-config.json")
        val jsonString = file.contentAsString
        val jsonMapParseResult = decode[List[Map[String, Any]]](jsonString)
        val jsonMap            = jsonMapParseResult.getOrElse(throw new Exception(s"Could not parse file ${file.toString()}"))
        val ignoreClasses = List(
          "jdk.internal.loader.BuiltinClassLoader",
          "jdk.internal.loader.ClassLoaders$AppClassLoader",
          "jdk.internal.loader.ClassLoaders$PlatformClassLoader"
        )
        val filtered = jsonMap.filter(map => {
          val name = map.get("name")
          ignoreClasses.contains(name)
        })
        file.write(encodeAnyToJson(filtered).toString())
      }
    )
    JvmStartService.startServerWithAgent(path)
  }

}

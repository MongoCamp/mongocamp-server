package dev.mongocamp.server.service

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.model.Capabilities
import dev.mongocamp.server.model.CapabilityLimits
import dev.mongocamp.server.BuildInfo

/** What this server can do. A new feature of the API gets its name in `Features` in the same change. */
object CapabilitiesService extends LazyLogging {

  val Features: List[String] = List(
    "permissions",
    "count",
    "searchInBody",
    "manyFilter",
    "aggregateFilter",
    "insertManyUnordered",
    "collectionsInfo",
    "collectionsInfoDatabase",
    "fieldsDetail",
    "indexDetails",
    "indexOrderedCreate",
    "collectionRename",
    "collectionCopy",
    "extendedJsonWrite",
    "extendedJsonRead",
    "export",
    "import",
    "systemInfo",
    "systemStats",
    "capabilities"
  )

  def capabilities(): Capabilities =
    Capabilities(BuildInfo.version, mongoDbMajor(), Features, limits(), AggregationPipelineGuard.deniedOperators.toList.sorted)

  def limits(): CapabilityLimits =
    CapabilityLimits(
      ConfigurationService.getConfigValue[Long](DefaultConfigurations.ConfigKeyImportMaxBytes),
      // the same decision as GrantDenied: 403 when configured, 401 for everything else
      if (ConfigurationService.getConfigValue[Long](DefaultConfigurations.ConfigKeyAuthGrantDeniedStatus) == 403L) 403 else 401
    )

  /** Only the major version, the whole version is for administrators (`GET /system/info`). A database that does not answer is no reason to fail. */
  private def mongoDbMajor(): Option[Int] =
    try MongoDbInfoService.systemInfo().mongoDbVersionArray.headOption
    catch {
      case e: Exception =>
        logger.debug("the version of the MongoDB server is not available: %s".format(e.getMessage))
        None
    }
}

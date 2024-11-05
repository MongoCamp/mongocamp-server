package dev.mongocamp.server.route
import dev.mongocamp.server.exception.ErrorDescription
import dev.mongocamp.server.model.auth.UserInformation
import dev.mongocamp.server.model.{JobConfig, JobInformation, JsonValue, ModelConstants}
import dev.mongocamp.server.plugin.{JobPlugin, RoutesPlugin}
import dev.mongocamp.server.service.ReflectionService
import io.circe.generic.auto._
import org.quartz.Job
import sttp.capabilities
import sttp.capabilities.pekko.PekkoStreams
import sttp.model.{Method, StatusCode}
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

object JobRoutes extends BaseRoute with RoutesPlugin {
  private val jobApiBaseEndpoint = adminEndpoint.tag("Jobs").in("system" / "jobs")

  val jobsListRoutes = jobApiBaseEndpoint
    .out(jsonBody[List[JobInformation]])
    .summary("Registered Jobs")
    .description("Returns the List of all registered Jobs with full information")
    .method(Method.GET)
    .name("jobsList")
    .serverLogic(
      _ => _ => jobsList()
    )

  def jobsList(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[JobInformation]]] = {
    Future.successful(Right({
      JobPlugin.jobsList()
    }))
  }

  val registerJobRoutes = jobApiBaseEndpoint
    .in(jsonBody[JobConfig])
    .out(jsonBody[Option[JobInformation]])
    .summary("Register Job")
    .description("Register an Job and return the JobInformation with next schedule information")
    .method(Method.PUT)
    .name("registerJob")
    .serverLogic(
      auth => config => registerJob(auth, config)
    )

  def registerJob(auth: UserInformation, jobConfig: JobConfig): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), Option[JobInformation]]] = {
    Future.successful(Right({
      val added = JobPlugin.addJob(jobConfig, Some(auth))
      if (added) {
        Some(JobPlugin.convertToJobInformation(jobConfig))
      }
      else {
        None
      }
    }))
  }

  lazy val jobGroupParameter =
    path[String]("jobGroup")
      .description("Group Name of the Job")
      .default(ModelConstants.jobDefaultGroup)
      .and(path[String]("jobName").description("Name of the Job"))

  // https://www.freeformatter.com/cron-expression-generator-quartz.html
  val updateJobRoutes = jobApiBaseEndpoint
    .in(jobGroupParameter)
    .in(jsonBody[JobConfig])
    .out(jsonBody[Option[JobInformation]])
    .summary("Update Job")
    .description("Add Job and get JobInformation back")
    .method(Method.PATCH)
    .name("updateJob")
    .serverLogic(
      auth => parameter => updateJob(auth, parameter)
    )

  def updateJob(
      auth: UserInformation,
      parameter: (String, String, JobConfig)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), Option[JobInformation]]] = {
    Future.successful(Right({
      val jobConfig = parameter._3
      val updated   = JobPlugin.updateJob(auth, parameter._1, parameter._2, jobConfig)
      if (updated) {
        Some(JobPlugin.convertToJobInformation(jobConfig))
      }
      else {
        None
      }
    }))
  }

  val deleteJobRoutes = jobApiBaseEndpoint
    .in(jobGroupParameter)
    .out(jsonBody[JsonValue[Boolean]])
    .summary("Delete Job")
    .description("Delete Job and reload all Job Information")
    .method(Method.DELETE)
    .name("deleteJob")
    .serverLogic(
      auth => parameter => deleteJob(auth, parameter)
    )

  def deleteJob(auth: UserInformation, parameter: (String, String)): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonValue[Boolean]]] = {
    Future.successful(Right({
      JsonValue(JobPlugin.deleteJob(auth, parameter._1, parameter._2))
    }))
  }

  val jobClassesRoutes = jobApiBaseEndpoint
    .in("classes")
    .out(jsonBody[List[String]])
    .summary("Possible Jobs")
    .description("Returns the List of possible job classes")
    .method(Method.GET)
    .name("possibleJobsList")
    .serverLogic(
      _ => _ => jobClassesList()
    )

  def jobClassesList(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[String]]] = {
    Future.successful(Right({
      ReflectionService.getSubClassesList(classOf[Job]).map(_.getName).filterNot(_.startsWith("org.quartz")).sorted
    }))
  }

  val executeJobRoutes = jobApiBaseEndpoint
    .in(jobGroupParameter)
    .out(jsonBody[JsonValue[Boolean]])
    .summary("Execute Job")
    .description("Execute scheduled Job manually")
    .method(Method.POST)
    .name("executeJob")
    .serverLogic(
      _ => parameter => executeJob(parameter)
    )

  def executeJob(parameter: (String, String)): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonValue[Boolean]]] = {
    Future.successful(Right({
      JsonValue(JobPlugin.executeJob(parameter._1, parameter._2))
    }))
  }

  override def endpoints: List[ServerEndpoint[PekkoStreams with capabilities.WebSockets, Future]] =
    List(jobsListRoutes, registerJobRoutes, updateJobRoutes, deleteJobRoutes, executeJobRoutes, jobClassesRoutes)
}

package dev.mongocamp.server.plugin

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.event.EventSystem
import dev.mongocamp.server.event.job.{ CreateJobEvent, DeleteJobEvent, UpdateJobEvent }
import dev.mongocamp.server.exception.ErrorCodes.{ jobAlreadyAdded, jobClassNotFound, jobCouldNotFound, jobCouldNotUpdated }
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.model.auth.UserInformation
import dev.mongocamp.server.model.{ JobConfig, JobInformation }
import dev.mongocamp.server.service.ReflectionService
import org.mongodb.scala.model.IndexOptions
import org.quartz.JobBuilder._
import org.quartz.TriggerBuilder._
import org.quartz.impl.StdSchedulerFactory
import org.quartz.{ CronScheduleBuilder, Job, JobKey, Trigger }
import sttp.model.StatusCode

import java.util.Date
import scala.jdk.CollectionConverters.ListHasAsScala

object JobPlugin extends ServerPlugin with LazyLogging {

  private lazy val scheduler = StdSchedulerFactory.getDefaultScheduler

  override def activate(): Unit = {
    scheduler.start()
    MongoDatabase.jobDao.createIndex(Map("group" -> 1, "name" -> 1), IndexOptions().unique(true)).toFuture()
    loadJobs()
    sys.addShutdownHook({
      logger.info("Shutdown for Job Scheduler triggered. Wait fore Jobs to be completed")
      scheduler.shutdown(true)
    })
  }

  def reloadJobs(): Unit = {
    scheduler.clear()
    loadJobs()
  }

  def convertToJobInformation(jobConfig: JobConfig) = {
    val schedulerTriggerList         = JobPlugin.getTriggerList(jobConfig.name, jobConfig.group)
    var nextFireTime: Option[Date]   = None
    var lastFireTime: Option[Date]   = None
    var scheduleInfo: Option[String] = None
    if (schedulerTriggerList.nonEmpty) {
      nextFireTime = Option(schedulerTriggerList.map(_.getNextFireTime).min)
      lastFireTime = Option(schedulerTriggerList.map(_.getPreviousFireTime).max)
    }
    else {
      scheduleInfo = Some(s"Job `${jobConfig.name}` in group `${jobConfig.group}` is not scheduled.")
    }
    JobInformation(
      jobConfig.name,
      jobConfig.group,
      jobConfig.className,
      jobConfig.description,
      jobConfig.cronExpression,
      jobConfig.priority,
      lastFireTime,
      nextFireTime,
      scheduleInfo
    )
  }

  def addJob(jobConfig: JobConfig, userInformationOption: Option[UserInformation] = None): Boolean = {
    val jobClass = getJobClass(jobConfig)
    val internalJobName = if (jobConfig.name.trim.equalsIgnoreCase("")) {
      jobClass.getSimpleName
    }
    else {
      jobConfig.name
    }
    val couldAddJob = MongoDatabase.jobDao.find(Map("name" -> internalJobName, "group" -> jobConfig.group)).resultOption().isEmpty

    if (couldAddJob) {
      val jobConfigDetail = jobConfig.copy(name = internalJobName)
      val inserted        = MongoDatabase.jobDao.insertOne(jobConfigDetail).result()
      reloadJobs()
      if (inserted.wasAcknowledged()) {
        userInformationOption.foreach(userInformation => EventSystem.eventStream.publish(CreateJobEvent(userInformation, jobConfig)))
      }
      inserted.wasAcknowledged()
    }
    else {
      throw MongoCampException(s"${jobConfig.name} with group ${jobConfig.group} is already added.", StatusCode.PreconditionFailed, jobAlreadyAdded)
    }
  }

  def getTriggerList(jobName: String, groupName: String): List[Trigger] = {
    try
      scheduler.getTriggersOfJob(new JobKey(jobName, groupName)).asScala.toList
    catch {
      case _: Exception =>
        List()
    }
  }

  def getJobClass(jobConfig: JobConfig) = {
    var jobClass: Class[_ <: Job] = null
    try
      jobClass = ReflectionService.getClassByName(jobConfig.className).asInstanceOf[Class[_ <: Job]]
    catch {
      case _: ClassNotFoundException =>
        throw MongoCampException(
          s"${jobConfig.className} for ${jobConfig.name} with group ${jobConfig.group} not found.",
          StatusCode.NotFound,
          jobClassNotFound
        )
    }
    jobClass
  }

  def updateJob(userInformation: UserInformation, groupName: String, jobName: String, jobConfig: JobConfig): Boolean = {
    val jobExists = MongoDatabase.jobDao.find(Map("name" -> jobName, "group" -> groupName)).resultOption().isDefined

    if (!jobExists) {
      throw MongoCampException(s"$jobName with group $groupName does not exists.", StatusCode.NotFound, jobCouldNotFound)
    }

    val couldUpdate = if (jobName == jobConfig.name && groupName == jobConfig.group) {
      true
    }
    else {
      val jobClass = getJobClass(jobConfig)
      val internalJobName = if (jobConfig.name.trim.equalsIgnoreCase("")) {
        jobClass.getSimpleName
      }
      else {
        jobConfig.name
      }
      MongoDatabase.jobDao.find(Map("name" -> internalJobName, "group" -> jobConfig.group)).resultOption().isEmpty
    }
    if (couldUpdate) {
      val updateResponse = MongoDatabase.jobDao.replaceOne(Map("name" -> jobName, "group" -> groupName), jobConfig).result()
      reloadJobs()
      if (updateResponse.getModifiedCount > 0) {
        EventSystem.eventStream.publish(UpdateJobEvent(userInformation, jobName, groupName, jobConfig))
      }
      updateResponse.wasAcknowledged() && updateResponse.getModifiedCount > 0
    }
    else {
      throw MongoCampException(s"$jobName with group $groupName is already exists. Could not rename.", StatusCode.PreconditionFailed, jobCouldNotUpdated)
    }
  }

  def deleteJob(userInformation: UserInformation, groupName: String, jobName: String): Boolean = {
    val deleteResponse = MongoDatabase.jobDao.deleteMany(Map("name" -> jobName, "group" -> groupName)).result()
    reloadJobs()
    if (deleteResponse.getDeletedCount > 0) {
      EventSystem.eventStream.publish(DeleteJobEvent(userInformation, jobName, groupName))
    }
    deleteResponse.wasAcknowledged() && deleteResponse.getDeletedCount > 0
  }

  def executeJob(groupName: String, jobName: String): Boolean = {
    try {
      scheduler.triggerJob(new JobKey(jobName, groupName))
      true
    }
    catch {
      case _: Exception =>
        throw MongoCampException(s"$jobName with group $groupName not found", StatusCode.NotFound, jobCouldNotFound)
    }
  }

  private def loadJobs(): Unit = {
    val foundJobs = MongoDatabase.jobDao.find().resultList()
    foundJobs.foreach(scheduleJob)
  }

  private def scheduleJob(dbJob: JobConfig): Unit = {
    try {
      val jobClass = ReflectionService.getClassByName(dbJob.className).asInstanceOf[Class[_ <: Job]]
      val job      = newJob(jobClass).withIdentity(dbJob.name, dbJob.group).build
      val trigger = newTrigger()
        .withIdentity(s"${dbJob.name}Trigger", dbJob.group)
        .withSchedule(CronScheduleBuilder.cronSchedule(dbJob.cronExpression))
        .withPriority(dbJob.priority)
        .forJob(job)
        .build()
      scheduler.scheduleJob(job, trigger)
    }
    catch {
      case _: ClassNotFoundException =>
        logger.error(s"job ${dbJob.name} for group ${dbJob.group} class (${dbJob.className}) not found ")
    }
  }
}

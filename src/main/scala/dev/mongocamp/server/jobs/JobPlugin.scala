package dev.mongocamp.server.jobs

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.exception.ErrorCodes.{jobAlreadyAdded, jobCouldNotFound, jobCouldNotUpdated}
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.model.{JobConfig, JobInformation}
import dev.mongocamp.server.plugin.ServerPlugin
import dev.mongocamp.server.service.ReflectionService
import org.mongodb.scala.model.IndexOptions
import org.quartz.JobBuilder._
import org.quartz.TriggerBuilder._
import org.quartz.impl.StdSchedulerFactory
import org.quartz.{CronScheduleBuilder, Job, JobKey, Trigger}
import sttp.model.StatusCode

import java.util.Date
import scala.jdk.CollectionConverters.ListHasAsScala

object JobPlugin extends ServerPlugin with LazyLogging {

  private lazy val scheduler = StdSchedulerFactory.getDefaultScheduler

  override def activate(): Unit = {
    scheduler.startDelayed(30)
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
    val schedulerTriggerList        = JobPlugin.getTriggerList(jobConfig.name, jobConfig.group)
    var nextFireTime: Option[Date]  = None
    var lastFireTime: Option[Date]  = None
    var scheduleIno: Option[String] = None
    if (schedulerTriggerList.nonEmpty) {
      nextFireTime = Option(schedulerTriggerList.map(_.getNextFireTime).min)
      lastFireTime = Option(schedulerTriggerList.map(_.getPreviousFireTime).max)
    }
    else {
      scheduleIno = Some(s"Job `${jobConfig.name}` in group `${jobConfig.group}` is not scheduled.")
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
      scheduleIno
    )
  }

  def addJob(jobConfig: JobConfig): Boolean = {
    addJob(jobConfig.className, jobConfig.cronExpression, jobConfig.name, jobConfig.group, jobConfig.description, jobConfig.priority)
  }

  def addJob(
      className: String,
      cronExpression: String,
      jobName: String = "",
      groupName: String = JobConfig.defaultGroup,
      description: String = "",
      priority: Int = 1
  ): Boolean = {
    val internalJobName = if (jobName.trim.equalsIgnoreCase("")) {
      val jobClass = ReflectionService.getClassByName(className).asInstanceOf[Class[_ <: Job]]
      jobClass.getSimpleName
    }
    else {
      jobName
    }
    val couldAddJob = MongoDatabase.jobDao.find(Map("name" -> internalJobName, "group" -> groupName)).resultOption().isEmpty

    if (couldAddJob) {
      val jobConfigDetail = JobConfig(internalJobName, groupName, className, description, cronExpression, priority)
      MongoDatabase.jobDao.insertOne(jobConfigDetail).result().wasAcknowledged()
    }
    else {
      throw MongoCampException(s"$jobName with group $groupName is already added.", StatusCode.PreconditionFailed, jobAlreadyAdded)
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

  def updateJob(groupName: String, jobName: String, jobConfig: JobConfig): Boolean = {
    val couldUpdate = if (jobName == jobConfig.name && groupName == jobConfig.group) {
      true
    }
    else {
      val internalJobName = if (jobName.trim.equalsIgnoreCase("")) {
        val jobClass = ReflectionService.getClassByName(jobConfig.className).asInstanceOf[Class[_ <: Job]]
        jobClass.getSimpleName
      }
      else {
        jobName
      }
      MongoDatabase.jobDao.find(Map("name" -> internalJobName, "group" -> groupName)).resultOption().isEmpty
    }
    if (couldUpdate) {
      val updateResponse = MongoDatabase.jobDao.replaceOne(Map("name" -> jobName, "group" -> groupName), jobConfig).result()
      reloadJobs()
      updateResponse.wasAcknowledged()
    }
    else {
      throw MongoCampException(s"$jobName with group $groupName is already exists. Could not rename.", StatusCode.PreconditionFailed, jobCouldNotUpdated)
    }
  }

  def deleteJob(jobName: String, groupName: String): Boolean = {
    val deleteResponse = MongoDatabase.jobDao.deleteMany(Map("name" -> jobName, "group" -> groupName)).result()
    reloadJobs()
    deleteResponse.wasAcknowledged()
  }

  def executeJob(jobName: String, groupName: String): Boolean = {
    try
      scheduler.triggerJob(new JobKey(jobName, groupName))
    catch {
      case _: Exception =>
        throw MongoCampException(s"$jobName with group $groupName not found", StatusCode.NotFound, jobCouldNotFound)
    }
  }

  private def loadJobs(): Unit = {
    MongoDatabase.jobDao.find().resultList().foreach(scheduleJob)
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

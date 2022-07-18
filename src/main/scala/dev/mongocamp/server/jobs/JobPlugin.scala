package dev.mongocamp.server.jobs

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.converter.MongoCampBsonConverter
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.exception.ErrorCodes.jobAlreadyAdded
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.model.JobConfigDetail
import dev.mongocamp.server.plugin.ServerPlugin
import dev.mongocamp.server.service.ReflectionService
import org.mongodb.scala.model.IndexOptions
import org.quartz.JobBuilder._
import org.quartz.TriggerBuilder._
import org.quartz.impl.StdSchedulerFactory
import org.quartz.{CronScheduleBuilder, Job}
import sttp.model.StatusCode
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

  def addJob(
      className: String,
      cronExpression: String,
      jobName: String = "",
      groupName: String = "Default",
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
      val jobConfigDetail = JobConfigDetail(internalJobName, groupName, className, description, cronExpression, priority)
      MongoDatabase.jobDao.insertOne(jobConfigDetail).result().wasAcknowledged()
    }
    else {
      throw MongoCampException(s"$jobName with group $groupName is already added.", StatusCode.PreconditionFailed, jobAlreadyAdded)
    }
  }

  def updateJob(jobName: String, cronExpression: String, priority: Int = 0, groupName: String = "Default"): Boolean = {
    var updateMap: Map[String, Any] = Map("cronExpression" -> cronExpression)
    if (priority > 0) {
      updateMap = updateMap ++ Map("priority" -> priority)
    }
    val updateResponse = MongoDatabase.jobDao
      .updateOne(Map("name" -> jobName, "group" -> groupName), MongoCampBsonConverter.convertToOperationMap(updateMap))
      .result()
    reloadJobs()
    updateResponse.wasAcknowledged()
  }

  def deleteJob(jobName: String, groupName: String = "Default"): Boolean = {
    val deleteResponse = MongoDatabase.jobDao.deleteMany(Map("name" -> jobName, "group" -> groupName)).result()
    reloadJobs()
    deleteResponse.wasAcknowledged()
  }

  private def loadJobs(): Unit = {
    MongoDatabase.jobDao.find().resultList().foreach(scheduleJob)
  }

  private def scheduleJob(dbJob: JobConfigDetail): Unit = {
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
      case e: ClassNotFoundException =>
        logger.error("job class not found: " + e.getMessage)
    }
  }
}

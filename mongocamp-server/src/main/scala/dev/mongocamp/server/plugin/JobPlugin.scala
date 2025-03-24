package dev.mongocamp.server.plugin

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.Server
import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.event.EventSystem
import dev.mongocamp.server.event.job._
import dev.mongocamp.server.exception.ErrorCodes.{jobAlreadyAdded, jobClassNotFound, jobCouldNotFound}
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.model.{JobConfig, JobInformation}
import dev.mongocamp.server.model.auth.UserInformation
import dev.mongocamp.server.service.{ConfigurationRead, ReflectionService}
import org.quartz.JobBuilder._
import org.quartz.TriggerBuilder._
import org.quartz._
import org.quartz.impl.StdSchedulerFactory
import org.quartz.impl.matchers.GroupMatcher
import org.quartz.utils.{DBConnectionManager, HikariCpPoolingConnectionProvider}
import sttp.model.StatusCode

import java.util.Date
import scala.jdk.CollectionConverters.{CollectionHasAsScala, ListHasAsScala}

object JobPlugin extends ServerPlugin with LazyLogging {
  private var provider: HikariCpPoolingConnectionProvider = _
  private lazy val scheduler = {
    val configRead         = ConfigurationRead.noPublishReader
    val connectionDatabase = configRead.getConfigValue[String](DefaultConfigurations.ConfigKeyConnectionDatabase)
    val connectionHost     = configRead.getConfigValue[String](DefaultConfigurations.ConfigKeyConnectionHost)
    val connectionPort     = configRead.getConfigValue[Long](DefaultConfigurations.ConfigKeyConnectionPort).toInt
    val jdbcUrl            = s"jdbc:mongodb://$connectionHost:$connectionPort/$connectionDatabase"
    val userName           = configRead.getConfigValue[Option[String]](DefaultConfigurations.ConfigKeyConnectionUsername).getOrElse("")
    val password           = configRead.getConfigValue[Option[String]](DefaultConfigurations.ConfigKeyConnectionPassword).getOrElse("")

    provider =
      new HikariCpPoolingConnectionProvider("dev.mongocamp.driver.mongodb.jdbc.MongoJdbcDriver", jdbcUrl, userName, password, 100, "select * from mc_users")
    DBConnectionManager.getInstance().addConnectionProvider("quartzJdbc", provider)
    StdSchedulerFactory.getDefaultScheduler
  }

  override def activate(): Unit = {
    scheduler.startDelayed(1)
    Server.registerServerShutdownCallBacks(
      () => {
        println("Shutdown for Job Scheduler triggered. Wait fore Jobs to be completed")
        scheduler.shutdown(true)
        if (provider != null) {
          provider.shutdown()
        }
      }
    )
  }

  def convertToJobInformation(
    jobGroup: String,
    jobName: String,
    className: String,
    description: String,
    cronExpression: Option[String] = None
  ): JobInformation = {
    val schedulerTriggerList         = JobPlugin.getTriggerList(jobGroup, jobName)
    var nextFireTime: Option[Date]   = None
    var lastFireTime: Option[Date]   = None
    var scheduleInfo: Option[String] = None
    var priority: Int                = Int.MaxValue
    var triggerCron: String          = ""
    if (schedulerTriggerList.nonEmpty) {
      nextFireTime = Option(schedulerTriggerList.map(_.getNextFireTime).min)
      lastFireTime = Option(schedulerTriggerList.map(_.getPreviousFireTime).max)
      priority = schedulerTriggerList.map(_.getPriority).min
      triggerCron = schedulerTriggerList.filter(_.isInstanceOf[CronTrigger]).map(_.asInstanceOf[CronTrigger].getCronExpression).headOption.getOrElse("")
    }
    else {
      scheduleInfo = Some(s"Job `${jobName}` in group `${jobGroup}` is not scheduled.")
    }
    JobInformation(
      jobName,
      jobGroup,
      className,
      Option(description).filterNot(_.trim.isEmpty),
      cronExpression.getOrElse(triggerCron),
      priority,
      lastFireTime,
      nextFireTime,
      scheduleInfo
    )
  }

  def convertToJobInformation(jobConfig: JobConfig): JobInformation = {
    convertToJobInformation(jobConfig.group, jobConfig.name, jobConfig.className, jobConfig.description, Option(jobConfig.cronExpression))
  }

  def addJob(jobConfig: JobConfig, userInformationOption: Option[UserInformation] = None, sendEvent: Boolean = true): Boolean = {
    val jobClass = getJobClass(jobConfig)
    val internalJobName = if (jobConfig.name.trim.equalsIgnoreCase("")) {
      jobClass.getSimpleName
    }
    else {
      jobConfig.name
    }
    try {
      scheduleJob(jobConfig.copy(name = internalJobName))
      if (sendEvent) {
        userInformationOption.foreach(
          userInformation => EventSystem.publish(CreateJobEvent(userInformation, jobConfig))
        )
      }
      true
    }
    catch {
      case _: Throwable =>
        throw MongoCampException(s"${jobConfig.name} with group ${jobConfig.group} is already added.", StatusCode.PreconditionFailed, jobAlreadyAdded)
    }
  }

  def getTriggerList(groupName: String, jobName: String): List[Trigger] = {
    try {
      val trigger = scheduler.getTriggersOfJob(new JobKey(jobName, groupName)).asScala
      trigger.toList
    }
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

  def jobsList(): List[JobInformation] = {
    val jobList = scheduler.getJobGroupNames.asScala
      .filter(
        s => Option(s).nonEmpty
      )
      .flatMap(
        jobGroup => {
          scheduler
            .getJobKeys(GroupMatcher.jobGroupEquals(jobGroup))
            .asScala
            .map(
              jobKey => {
                val jobDetail = scheduler.getJobDetail(jobKey)
                convertToJobInformation(jobDetail.getKey.getGroup, jobDetail.getKey.getName, jobDetail.getJobClass.getName, jobDetail.getDescription)
              }
            )
        }
      )
    jobList.toList
  }

  def updateJob(userInformation: UserInformation, groupName: String, jobName: String, jobConfig: JobConfig): Boolean = {
    val jobKey = new JobKey(jobName, groupName)
    scheduler.getJobDetail(jobKey) match {
      case null =>
        throw MongoCampException(s"$jobName with group $groupName does not exists.", StatusCode.NotFound, jobCouldNotFound)
      case _ =>
        deleteJob(userInformation, groupName, jobName, sendEvent = false)
        addJob(jobConfig, Some(userInformation), sendEvent = false)
    }
  }

  def deleteJob(userInformation: UserInformation, groupName: String, jobName: String, sendEvent: Boolean = true): Boolean = {
    getTriggerList(groupName, jobName).foreach(
      trigger => scheduler.unscheduleJob(trigger.getKey)
    )
    val response = scheduler.deleteJob(new JobKey(jobName, groupName))
    if (sendEvent && response) {
      EventSystem.publish(DeleteJobEvent(userInformation, jobName, groupName))
    }
    response
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

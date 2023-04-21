package dev.mongocamp.server.tests

import dev.mongocamp.server.test.{CountingTestJob, MongoCampBaseServerSuite}
import dev.mongocamp.server.test.client.api.JobsApi
import dev.mongocamp.server.test.client.model.JobConfig

class JobSuite extends MongoCampBaseServerSuite {

  val jobsApi: JobsApi = JobsApi()

  test("check pre triggered job was running") {
    // test fails sometime if only JobSuite is running
    val jobsList = executeRequestToResponse(jobsApi.possibleJobsList("", "", adminBearerToken, "")())
    assertEquals(CountingTestJob.counter > 0, true)
  }

  test("check possible jobs as admin") {
    val jobsList = executeRequestToResponse(jobsApi.possibleJobsList("", "", adminBearerToken, "")())
    assertEquals(jobsList.size, 2)
    assertEquals(jobsList.head, "dev.mongocamp.server.test.CountingTestJob")
    assertEquals(jobsList.last, "dev.mongocamp.server.jobs.CleanUpTokenJob")
  }

  test("check possible jobs as user") {
    val response = executeRequest(jobsApi.possibleJobsList("", "", testUserBearerToken, "")())
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for request")
  }

  test("list jobs as admin") {
    val jobsList = executeRequestToResponse(jobsApi.jobsList("", "", adminBearerToken, "")())
    assertEquals(jobsList.size, 2)
    val fistJob = jobsList.head
    assertEquals(fistJob.jobClassName, "dev.mongocamp.server.test.CountingTestJob")
    assertEquals(fistJob.name, "CountingTestJob")
    assertEquals(fistJob.group, "Default")
    assertEquals(fistJob.description, "")
    assertEquals(fistJob.cronExpression, "0/5 * * ? * * *")
    assertEquals(fistJob.nextScheduledFireTime.isDefined, true)
  }

  test("list jobs as user") {
    val response = executeRequest(jobsApi.jobsList("", "", testUserBearerToken, "")())
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for request")
  }

  test("execute jobs as admin") {
    val countBefore = CountingTestJob.counter
    val executedJob = executeRequestToResponse(jobsApi.executeJob("", "", adminBearerToken, "")("Default", "CountingTestJob"))
    assertEquals(executedJob.value, true)
    val countAfter = CountingTestJob.counter
    assertEquals(countAfter > countBefore, true)
  }

  test("execute jobs as user") {
    val response = executeRequest(jobsApi.executeJob("", "", testUserBearerToken, "")("Default", "CountingTestJob"))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for request")
  }

  test("register job as admin") {
    val config      = JobConfig("RandomNewJobName", "dev.mongocamp.server.test.CountingTestJob", "registeredJob", "34 34 0 1 1/7 ? 2022/7", "NewGroup", 1)
    val executedJob = executeRequestToResponse(jobsApi.registerJob("", "", adminBearerToken, "")(config))
    assertEquals(executedJob.name, config.name)
    assertEquals(executedJob.group, config.group)
    assertEquals(executedJob.jobClassName, config.className)
    assertEquals(executedJob.cronExpression, config.cronExpression)
    assertEquals(executedJob.description, config.description)
    assertEquals(executedJob.nextScheduledFireTime.isDefined, true)
  }

  test("register job as user") {
    val config   = JobConfig("RandomNewJobName", "dev.mongocamp.server.test.CountingTestJob", "registeredJob", "34 34 0 1 1/7 ? 2022/7", "NewGroup", 1)
    val response = executeRequest(jobsApi.registerJob("", "", testUserBearerToken, "")(config))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for request")
  }

  test("update job as admin") {
    val config =
      JobConfig("RandomNewJobNameChanged", "dev.mongocamp.server.test.CountingTestJob", "Changed Description", "34 34 0 1 1/7 ? 2022/7", "NewGroup", 1)
    val executedJob = executeRequestToResponse(jobsApi.updateJob("", "", adminBearerToken, "")("NewGroup", "RandomNewJobName", config))
    assertEquals(executedJob.name, config.name)
    assertEquals(executedJob.group, config.group)
    assertEquals(executedJob.jobClassName, config.className)
    assertEquals(executedJob.cronExpression, config.cronExpression)
    assertEquals(executedJob.description, config.description)
    assertEquals(executedJob.nextScheduledFireTime.isDefined, true)
  }

  test("update not existing job as admin") {
    val config   = JobConfig("RandomNewJobNameChanged", "dev.mongocamp.server.test.CountingTestJob", "registeredJob", "34 34 0 1 1/7 ? 2022/7", "NewGroup", 1)
    val response = executeRequest(jobsApi.updateJob("", "", adminBearerToken, "")("NewGroup", "FreakyName", config))
    assertEquals(response.code.code, 404)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "FreakyName with group NewGroup does not exists.")
  }

  test("update job as user") {
    val config   = JobConfig("RandomNewJobName", "dev.mongocamp.server.server.CountingTestJob", "registeredJob", "34 34 0 1 1/7 ? 2022/7", "NewGroup", 1)
    val response = executeRequest(jobsApi.updateJob("", "", testUserBearerToken, "")("NewGroup", "RandomNewJobName", config))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for request")
  }

  test("delete job as admin") {
    val jobsList1 = executeRequestToResponse(jobsApi.jobsList("", "", adminBearerToken, "")())
    assertEquals(jobsList1.size, 3)
    val deleteResponse = executeRequestToResponse(jobsApi.deleteJob("", "", adminBearerToken, "")("NewGroup", "RandomNewJobNameChanged"))
    assertEquals(deleteResponse.value, true)
    val jobsList2 = executeRequestToResponse(jobsApi.jobsList("", "", adminBearerToken, "")())
    assertEquals(jobsList2.size, 2)
  }

  test("delete job as user") {
    val response = executeRequest(jobsApi.deleteJob("", "", testUserBearerToken, "")("NewGroup", "RandomNewJobNameChanged"))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for request")
  }

}

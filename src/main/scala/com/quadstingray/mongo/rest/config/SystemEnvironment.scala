package com.quadstingray.mongo.rest.config

import com.quadstingray.mongo.rest.config.SystemStage.{Development, Production, Staging, SystemTest, UnitTest}
import com.typesafe.scalalogging.LazyLogging

object SystemEnvironmentHelper extends SystemEnvironment

trait SystemEnvironment extends LazyLogging {
  val ProjectStageKey = "ProjectStage"

  def isSystemStageDevelopment: Boolean =
    Development.toString == systemStage

  def isSystemStageStaging: Boolean =
    Staging.toString == systemStage

  def isSystemStageProduction: Boolean =
    Production.toString == systemStage

  def isSystemStageUnitTest: Boolean =
    UnitTest.toString == systemStage

  def isSystemStageSystemTest: Boolean =
    SystemTest.toString == systemStage

  def systemStage: String = System.getProperty(ProjectStageKey)

  def setSystemStage(projectStage: String): Unit = {
    System.setProperty(ProjectStageKey, projectStage)
    logger.debug("System Stage changed to %s".format(projectStage))
  }

  def setSystemStageUnitTest(): Unit = setSystemStage(UnitTest.toString)

  def setSystemStageSystemTest(): Unit = setSystemStage(SystemTest.toString)

  def setSystemStageDevelopment(): Unit = setSystemStage(Development.toString)

  def setSystemStageProduction(): Unit = setSystemStage(Production.toString)

  def setSystemStageStaging(): Unit = setSystemStage(Staging.toString)

  def getJavaVersion: String =
    System.getProperty("java.version")

  def getJavaVendor: String =
    System.getProperty("java.vendor")

  def getJavaVendorUrl: String =
    System.getProperty("java.vendor.url")

  def getJavaHome: String =
    System.getProperty("java.home")

  // os
  def getOSName: String =
    System.getProperty("os.name")

  def getOSArchitecture: String =
    System.getProperty("os.arch")

  def getOSVersion: String =
    System.getProperty("os.version")

  def isMacOS: Boolean =
    getOSName.toLowerCase.contains("mac")

  def isWindowsOS: Boolean =
    getOSName.toLowerCase.contains("windows")

  // separators
  def getFileSeparator: String =
    System.getProperty("file.separator")

  def getPathSeparator: String =
    System.getProperty("path.separator")

  def getLineSeparator: String =
    System.getProperty("line.separator")

  // user
  def getUserName: String =
    System.getProperty("user.name")

  def getUserHomeDirectory: String =
    System.getProperty("user.home")

  def getCurrentUserDirectory: String =
    System.getProperty("user.dir")

  def getTempPath: String =
    System.getProperty("java.io.tmpdir")

}

object SystemStage extends Enumeration {
  val Development, Production, SystemTest, Staging, UnitTest = Value
}

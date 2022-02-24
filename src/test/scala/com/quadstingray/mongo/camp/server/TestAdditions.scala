package com.quadstingray.mongo.camp.server
import sttp.client.akkahttp.AkkaHttpBackend

import scala.util.Random

object TestAdditions {

  lazy val backend = AkkaHttpBackend()

  def importData(): Boolean = {
    Random.nextBoolean()
  }

}

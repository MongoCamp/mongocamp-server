package com.quadstingray.mongo.camp.routes.parameter.paging

case class Paging(rowsPerPage: Option[Int], page: Option[Int])

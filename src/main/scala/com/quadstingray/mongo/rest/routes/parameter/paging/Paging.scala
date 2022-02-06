package com.quadstingray.mongo.rest.routes.parameter.paging

case class Paging(rowsPerPage: Option[Int], page: Option[Int])

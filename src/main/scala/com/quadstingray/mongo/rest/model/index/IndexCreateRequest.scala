package com.quadstingray.mongo.rest.model.index

case class IndexCreateRequest(keys: Map[String, Any], indexOptionsRequest: IndexOptionsRequest)

package com.quadstingray.mongo.camp.model.index

case class IndexCreateRequest(keys: Map[String, Any], indexOptionsRequest: IndexOptionsRequest)

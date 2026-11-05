package dev.mongocamp.server.model

/** The parameters of the list of documents in a request body: the filter is a Lucene query like the query parameter `filter`, sort and projection are lists of
  * field names like the query parameters, a leading `-` sorts descending.
  */
case class SearchRequest(filter: Option[String] = None, sort: List[String] = List(), projection: List[String] = List())

/** mongocamp No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
  *
  * The version of the OpenAPI document: 0.5.0
  *
  * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech). https://openapi-generator.tech Do not edit the class manually.
  */
package com.quadstingray.mongo.camp.client.model

case class UpdateResponse(
    wasAcknowledged: Boolean,
    upsertedIds: Option[Seq[String]] = None,
    modifiedCount: Long,
    matchedCount: Long
)

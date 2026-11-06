package dev.mongocamp.server.model

/** How often a field has a value of a type in the analysed documents. */
case class FieldTypeCount(`type`: String, count: Long)

/** A field of a collection with its path (`address.city`, an element of an array is `tags[]`) and the types of its values. */
case class FieldInfo(path: String, types: List[FieldTypeCount])

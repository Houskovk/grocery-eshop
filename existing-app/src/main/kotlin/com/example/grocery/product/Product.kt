package com.example.grocery.product

import io.quarkus.mongodb.panache.common.MongoEntity
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

@MongoEntity(collection = "products")
data class Product @BsonCreator constructor(
    @BsonId
    var id: ObjectId? = null,
    @BsonProperty("name") val name: String,
    @BsonProperty("priceInCents") val priceInCents: Long
)
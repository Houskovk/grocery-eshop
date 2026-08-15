package com.example.grocery.user

import io.quarkus.mongodb.panache.common.MongoEntity
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@MongoEntity(collection = "users")
data class User(
    @BsonId var id: ObjectId? = null,
    var username: String = "",
    var passwordHash: String = "",
    var role: String = "USER",
    var balanceInCents: Long = 0
)
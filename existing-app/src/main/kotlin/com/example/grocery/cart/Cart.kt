package com.example.grocery.cart

import io.quarkus.mongodb.panache.common.MongoEntity
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@MongoEntity(collection = "carts")
data class Cart(
    @BsonId var id: ObjectId? = null,
    var userId: String = "",
    var items: MutableList<CartItem> = mutableListOf()
)

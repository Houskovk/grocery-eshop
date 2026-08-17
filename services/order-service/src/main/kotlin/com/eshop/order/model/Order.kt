package com.eshop.order.model

import io.quarkus.mongodb.panache.common.MongoEntity
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant

@MongoEntity(collection = "orders")
class Order {
    @BsonId
    var id: ObjectId? = null

    lateinit var userId: String

    var items: List<OrderItem> = emptyList()

    var totalInCents: Long = 0

    var createdAt: Instant = Instant.now()
}


package com.eshop.user.model

import io.quarkus.mongodb.panache.common.MongoEntity
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant

@MongoEntity(collection = "walletTransactions")
data class WalletTransaction(
    @BsonId var id: ObjectId? = null,
    var userId: ObjectId? = null,
    var orderReference: String = "",
    var amountInCents: Long = 0,
    var createdAt: Instant = Instant.now()
)

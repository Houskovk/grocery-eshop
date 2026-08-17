package com.eshop.user.repository

import com.eshop.user.model.WalletTransaction
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import io.quarkus.mongodb.panache.kotlin.PanacheMongoRepository
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes

@ApplicationScoped
class WalletTransactionRepository : PanacheMongoRepository<WalletTransaction> {

    fun findByOrderReference(orderReference: String): WalletTransaction? =
        find("orderReference", orderReference).firstResult()

    fun onStart(@Observes event: StartupEvent) {
        mongoCollection().createIndex(
            Indexes.ascending("orderReference"),
            IndexOptions().unique(true)
        )
    }
}

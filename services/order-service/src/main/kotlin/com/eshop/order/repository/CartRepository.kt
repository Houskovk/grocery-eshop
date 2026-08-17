package com.eshop.order.repository

import com.eshop.order.model.Cart
import io.quarkus.mongodb.panache.kotlin.PanacheMongoRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class CartRepository : PanacheMongoRepository<Cart> {

    fun findByUserId(userId: String): Cart? {
        return find("userId", userId).firstResult()
    }
}


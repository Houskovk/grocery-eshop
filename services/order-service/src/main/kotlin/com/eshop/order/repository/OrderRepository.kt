package com.eshop.order.repository

import com.eshop.order.model.Order
import io.quarkus.mongodb.panache.kotlin.PanacheMongoRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class OrderRepository : PanacheMongoRepository<Order> {

    fun findByUserId(userId: String): List<Order> = find("userId", userId).list()
}


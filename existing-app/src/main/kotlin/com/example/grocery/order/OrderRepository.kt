package com.example.grocery.order

import io.quarkus.mongodb.panache.kotlin.PanacheMongoRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class OrderRepository : PanacheMongoRepository<Order> {

    fun findByUserId(userId: String): List<Order> = find("userId", userId).list()
}
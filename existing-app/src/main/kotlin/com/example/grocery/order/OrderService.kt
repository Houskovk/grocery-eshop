package com.example.grocery.order

import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class OrderService(private val orderRepository: OrderRepository) {

    fun createOrder(userId: String, items: List<OrderItem>, totalInCents: Long): Order {
        val order = Order().apply {
            this.userId = userId
            this.items = items
            this.totalInCents = totalInCents
        }

        orderRepository.persist(order)
        return order
    }

    fun findOrdersForUser(userId: String): List<Order> = orderRepository.findByUserId(userId).sortedByDescending { it.createdAt }

}
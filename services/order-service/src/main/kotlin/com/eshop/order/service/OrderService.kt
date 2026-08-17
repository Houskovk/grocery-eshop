package com.eshop.order.service

import com.eshop.order.model.Order
import com.eshop.order.model.OrderItem
import com.eshop.order.repository.OrderRepository
import jakarta.enterprise.context.ApplicationScoped
import org.bson.types.ObjectId

@ApplicationScoped
class OrderService(private val orderRepository: OrderRepository) {

    fun createOrder(id: ObjectId, userId: String, items: List<OrderItem>, totalInCents: Long): Order {
        val order = Order().apply {
            this.id = id
            this.userId = userId
            this.items = items
            this.totalInCents = totalInCents
        }

        orderRepository.persist(order)
        return order
    }

    fun findOrdersForUser(userId: String): List<Order> = orderRepository.findByUserId(userId).sortedByDescending { it.createdAt }

}


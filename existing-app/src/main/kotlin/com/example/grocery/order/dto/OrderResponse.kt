package com.example.grocery.order.dto

import com.example.grocery.order.OrderItem
import java.time.Instant

data class OrderResponse(
    val id: String,
    val items: List<OrderItem>,
    val totalInCents: Long,
    val createdAt: Instant
)
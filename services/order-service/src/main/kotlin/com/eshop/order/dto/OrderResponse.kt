package com.eshop.order.dto

import com.eshop.order.model.OrderItem
import java.time.Instant

data class OrderResponse(
    val id: String,
    val items: List<OrderItem>,
    val totalInCents: Long,
    val createdAt: Instant
)


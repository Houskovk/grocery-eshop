package com.example.grocery.order

import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@QuarkusTest
class OrderServiceTest {

    @Inject
    lateinit var orderService: OrderService

    @Inject
    lateinit var orderRepository: OrderRepository

    @AfterEach
    fun cleanup() {
        orderRepository.deleteAll()
    }

    private fun createTestOrderItem(
        productId: String = "test-product-id",
        productName: String = "Milk",
        unitPriceInCents: Long = 249,
        quantity: Int = 2
    ): OrderItem {
        return OrderItem(
            productId = productId,
            productName = productName,
            unitPriceInCents = unitPriceInCents,
            quantity = quantity,
            subtotalInCents = unitPriceInCents * quantity
        )
    }

    @Test
    fun createOrder_shouldPersistOrder() {
        val userId = "test-user-${System.nanoTime()}"
        val items = listOf(createTestOrderItem())

        val order = orderService.createOrder(userId, items, 498)

        assertNotNull(order.id)
        assertEquals(1, orderRepository.count())
    }

    @Test
    fun createOrder_shouldStoreProvidedItemsAndTotal() {
        val userId = "test-user-${System.nanoTime()}"
        val items = listOf(createTestOrderItem())

        val order = orderService.createOrder(userId, items, 498)

        assertEquals(userId, order.userId)
        assertEquals(1, order.items.size)
        assertEquals(498L, order.totalInCents)
    }

    @Test
    fun findOrdersForUser_shouldReturnEmptyList_whenUserHasNoOrders() {
        val userId = "test-user-${System.nanoTime()}"

        val orders = orderService.findOrdersForUser(userId)

        assertTrue(orders.isEmpty())
    }

    @Test
    fun findOrdersForUser_shouldReturnOnlyOrdersBelongingToThatUser() {
        val userId = "test-user-${System.nanoTime()}"
        val otherUserId = "other-user-${System.nanoTime()}"

        orderService.createOrder(userId, listOf(createTestOrderItem()), 498)
        orderService.createOrder(otherUserId, listOf(createTestOrderItem()), 498)

        val orders = orderService.findOrdersForUser(userId)

        assertEquals(1, orders.size)
        assertEquals(userId, orders.first().userId)
    }

    @Test
    fun findOrdersForUser_shouldReturnNewestOrderFirst() {
        val userId = "test-user-${System.nanoTime()}"

        val firstOrder = orderService.createOrder(userId, listOf(createTestOrderItem()), 100)
        Thread.sleep(5)
        val secondOrder = orderService.createOrder(userId, listOf(createTestOrderItem()), 200)

        val orders = orderService.findOrdersForUser(userId)

        assertEquals(secondOrder.id, orders.first().id)
        assertEquals(firstOrder.id, orders.last().id)
    }
}


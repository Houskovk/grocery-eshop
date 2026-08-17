package com.eshop.order

import com.eshop.order.client.CatalogClient
import com.eshop.order.client.CatalogProductResponse
import com.eshop.order.client.UserClient
import com.eshop.order.repository.CartRepository
import com.eshop.order.repository.OrderRepository
import com.eshop.order.service.CartService
import com.eshop.order.service.CheckoutService
import com.eshop.order.testsupport.MockedCatalogClient
import com.eshop.order.testsupport.MockedUserClient
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.InjectMock
import jakarta.inject.Inject
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException
import org.bson.types.ObjectId
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.kotlin.any

@QuarkusTest
class CheckoutServiceTest {

    @Inject
    lateinit var checkoutService: CheckoutService

    @Inject
    lateinit var cartService: CartService

    @Inject
    lateinit var cartRepository: CartRepository

    @Inject
    lateinit var orderRepository: OrderRepository

    @InjectMock
    @RestClient
    lateinit var catalogClient: CatalogClient

    @InjectMock
    @RestClient
    lateinit var userClient: UserClient

    private val productBackend = MockedCatalogClient()
    private val userBackend = MockedUserClient()

    @BeforeEach
    fun setup() {
        Mockito.`when`(catalogClient.getProduct(anyString())).thenAnswer { invocation ->
            productBackend.getProduct(invocation.getArgument(0))
        }
        Mockito.`when`(catalogClient.getProducts()).thenAnswer {
            productBackend.getProducts()
        }
        Mockito.`when`(userClient.chargeWallet(anyString(), any())).thenAnswer { invocation ->
            userBackend.chargeWallet(invocation.getArgument(0), invocation.getArgument(1))
        }
    }

    @AfterEach
    fun cleanup() {
        productBackend.clear()
        userBackend.clear()
        cartRepository.deleteAll()
        orderRepository.deleteAll()
    }

    private fun createTestUser(balanceInCents: Long = 0): String {
        val userId = ObjectId().toHexString()
        userBackend.seedUser(userId, balanceInCents)
        return userId
    }

    private fun createTestProduct(name: String = "Milk", priceInCents: Long = 249): CatalogProductResponse =
        productBackend.addProduct(name = name, priceInCents = priceInCents)

    @Test
    fun checkout_shouldCreateOrder_whenCartHasItems() {
        val userId = createTestUser(balanceInCents = 10000)
        val product = createTestProduct(priceInCents = 249)

        cartService.addItem(userId, product.id, 2)

        val result = checkoutService.checkout(userId)

        assertEquals(498L, result.totalInCents)
        assertEquals(1, orderRepository.count())
    }

    @Test
    fun checkout_shouldPersistOrderWithProductSnapshot() {
        val userId = createTestUser(balanceInCents = 10000)
        val product = createTestProduct(name = "Milk", priceInCents = 249)

        cartService.addItem(userId, product.id, 2)

        checkoutService.checkout(userId)

        val orders = orderRepository.findByUserId(userId)

        assertEquals(1, orders.size)

        val orderItem = orders.first().items.first()

        assertEquals("Milk", orderItem.productName)
        assertEquals(249L, orderItem.unitPriceInCents)
        assertEquals(2, orderItem.quantity)
        assertEquals(498L, orderItem.subtotalInCents)
    }

    @Test
    fun checkout_shouldDeductTotalFromUserBalance() {
        val userId = createTestUser(balanceInCents = 10000)
        val product = createTestProduct(priceInCents = 249)

        cartService.addItem(userId, product.id, 2)

        checkoutService.checkout(userId)

        assertEquals(10000L - 498L, userBackend.getBalance(userId))
    }

    @Test
    fun checkout_shouldClearCart_afterSuccessfulCheckout() {
        val userId = createTestUser(balanceInCents = 10000)
        val product = createTestProduct(priceInCents = 249)

        cartService.addItem(userId, product.id, 2)

        checkoutService.checkout(userId)

        val cart = cartService.getCart(userId)

        assertTrue(cart.items.isEmpty())
    }

    @Test
    fun checkout_shouldThrowBadRequestException_whenCartIsEmpty() {
        val userId = createTestUser(balanceInCents = 10000)

        assertThrows(BadRequestException::class.java) {
            checkoutService.checkout(userId)
        }
    }

    @Test
    fun checkout_shouldThrowBadRequestException_whenBalanceIsInsufficient() {
        val userId = createTestUser(balanceInCents = 100)
        val product = createTestProduct(priceInCents = 249)

        cartService.addItem(userId, product.id, 2)

        assertThrows(BadRequestException::class.java) {
            checkoutService.checkout(userId)
        }

        assertEquals(100L, userBackend.getBalance(userId))
    }

    @Test
    fun checkout_shouldThrowBadRequestException_whenProductWasDeletedAfterBeingAddedToCart() {
        val userId = createTestUser(balanceInCents = 10000)
        val product = createTestProduct(priceInCents = 249)

        cartService.addItem(userId, product.id, 1)

        productBackend.removeProduct(product.id)

        assertThrows(BadRequestException::class.java) {
            checkoutService.checkout(userId)
        }
    }

    @Test
    fun checkout_shouldThrowNotFoundException_whenUserDoesNotExist() {
        val product = createTestProduct(priceInCents = 249)
        val unknownUserId = ObjectId().toHexString()

        cartService.addItem(unknownUserId, product.id, 1)

        assertThrows(NotFoundException::class.java) {
            checkoutService.checkout(unknownUserId)
        }
    }

    @Test
    fun checkout_shouldNotChargeTwice_whenRetriedForSameOrder() {
        val userId = createTestUser(balanceInCents = 10000)
        val product = createTestProduct(priceInCents = 249)

        cartService.addItem(userId, product.id, 2)
        checkoutService.checkout(userId)

        assertEquals(10000L - 498L, userBackend.getBalance(userId))
        assertEquals(1, orderRepository.count())
    }
}


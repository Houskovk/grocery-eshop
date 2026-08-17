package com.example.grocery.checkout

import com.example.grocery.cart.CartRepository
import com.example.grocery.cart.CartService
import com.example.grocery.client.CatalogClient
import com.example.grocery.client.CatalogProductResponse
import com.example.grocery.order.OrderRepository
import com.example.grocery.testsupport.MockedCatalogClient
import com.example.grocery.user.User
import com.example.grocery.user.UserRepository
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

@QuarkusTest
class CheckoutServiceTest {

    @Inject
    lateinit var checkoutService: CheckoutService

    @Inject
    lateinit var cartService: CartService

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var cartRepository: CartRepository

    @Inject
    lateinit var orderRepository: OrderRepository

    @InjectMock
    @RestClient
    lateinit var catalogClient: CatalogClient

    private val productBackend = MockedCatalogClient()

    @BeforeEach
    fun setup() {
        Mockito.`when`(catalogClient.getProduct(anyString())).thenAnswer { invocation ->
            productBackend.getProduct(invocation.getArgument(0))
        }
        Mockito.`when`(catalogClient.getProducts()).thenAnswer {
            productBackend.getProducts()
        }
    }

    @AfterEach
    fun cleanup() {
        userRepository.deleteAll()
        productBackend.clear()
        cartRepository.deleteAll()
        orderRepository.deleteAll()
    }

    private fun createTestUser(balanceInCents: Long = 0): User {
        val user = User(
            username = "checkout-user-${System.nanoTime()}",
            passwordHash = "not-used-in-this-test",
            balanceInCents = balanceInCents
        )

        userRepository.persist(user)

        return user
    }

    private fun createTestProduct(name: String = "Milk", priceInCents: Long = 249): CatalogProductResponse =
        productBackend.addProduct(name = name, priceInCents = priceInCents)

    @Test
    fun checkout_shouldCreateOrder_whenCartHasItems() {
        val user = createTestUser(balanceInCents = 10000)
        val product = createTestProduct(priceInCents = 249)

        cartService.addItem(user.id.toString(), product.id, 2)

        val result = checkoutService.checkout(user.id.toString())

        assertEquals(498L, result.totalInCents)
        assertEquals(1, orderRepository.count())
    }

    @Test
    fun checkout_shouldPersistOrderWithProductSnapshot() {
        val user = createTestUser(balanceInCents = 10000)
        val product = createTestProduct(name = "Milk", priceInCents = 249)

        cartService.addItem(user.id.toString(), product.id, 2)

        checkoutService.checkout(user.id.toString())

        val orders = orderRepository.findByUserId(user.id.toString())

        assertEquals(1, orders.size)

        val orderItem = orders.first().items.first()

        assertEquals("Milk", orderItem.productName)
        assertEquals(249L, orderItem.unitPriceInCents)
        assertEquals(2, orderItem.quantity)
        assertEquals(498L, orderItem.subtotalInCents)
    }

    @Test
    fun checkout_shouldDeductTotalFromUserBalance() {
        val user = createTestUser(balanceInCents = 10000)
        val product = createTestProduct(priceInCents = 249)

        cartService.addItem(user.id.toString(), product.id, 2)

        checkoutService.checkout(user.id.toString())

        val persistedUser = userRepository.findById(user.id!!)

        assertEquals(10000L - 498L, persistedUser?.balanceInCents)
    }

    @Test
    fun checkout_shouldClearCart_afterSuccessfulCheckout() {
        val user = createTestUser(balanceInCents = 10000)
        val product = createTestProduct(priceInCents = 249)

        cartService.addItem(user.id.toString(), product.id, 2)

        checkoutService.checkout(user.id.toString())

        val cart = cartService.getCart(user.id.toString())

        assertTrue(cart.items.isEmpty())
    }

    @Test
    fun checkout_shouldThrowBadRequestException_whenCartIsEmpty() {
        val user = createTestUser(balanceInCents = 10000)

        assertThrows(BadRequestException::class.java) {
            checkoutService.checkout(user.id.toString())
        }
    }

    @Test
    fun checkout_shouldThrowBadRequestException_whenBalanceIsInsufficient() {
        val user = createTestUser(balanceInCents = 100)
        val product = createTestProduct(priceInCents = 249)

        cartService.addItem(user.id.toString(), product.id, 2)

        assertThrows(BadRequestException::class.java) {
            checkoutService.checkout(user.id.toString())
        }

        val persistedUser = userRepository.findById(user.id!!)

        assertEquals(100L, persistedUser?.balanceInCents)
    }

    @Test
    fun checkout_shouldThrowBadRequestException_whenProductWasDeletedAfterBeingAddedToCart() {
        val user = createTestUser(balanceInCents = 10000)
        val product = createTestProduct(priceInCents = 249)

        cartService.addItem(user.id.toString(), product.id, 1)

        productBackend.removeProduct(product.id)

        assertThrows(BadRequestException::class.java) {
            checkoutService.checkout(user.id.toString())
        }
    }

    @Test
    fun checkout_shouldThrowNotFoundException_whenUserDoesNotExist() {
        assertThrows(NotFoundException::class.java) {
            checkoutService.checkout(ObjectId().toString())
        }
    }
}




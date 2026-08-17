package com.example.grocery.cart

import com.example.grocery.client.CatalogClient
import com.example.grocery.client.CatalogProductResponse
import com.example.grocery.testsupport.MockedCatalogClient
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.InjectMock
import jakarta.inject.Inject
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.bson.types.ObjectId
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito

@QuarkusTest
class CartServiceTest {

    @Inject
    lateinit var cartService: CartService

    @Inject
    lateinit var cartRepository: CartRepository

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
        cartRepository.deleteAll()
        productBackend.clear()
    }

    private fun createTestProduct(name: String = "Milk", priceInCents: Long = 249): CatalogProductResponse =
        productBackend.addProduct(name = name, priceInCents = priceInCents)

    @Test
    fun addItem_shouldAddNewProduct_whenProductNotAlreadyInCart() {
        val userId = "test-user-${System.nanoTime()}"
        val product = createTestProduct()

        cartService.addItem(userId, product.id, 1)

        val cart = cartService.getCart(userId)

        assertEquals(1, cart.items.size)
        assertEquals(1, cart.items.first().quantity)
    }

    @Test
    fun addItem_shouldIncreaseQuantity_whenProductAlreadyInCart() {
        val userId = "test-user-${System.nanoTime()}"
        val product = createTestProduct()

        cartService.addItem(userId, product.id, 1)
        cartService.addItem(userId, product.id, 2)

        val cart = cartService.getCart(userId)

        assertEquals(1, cart.items.size)
        assertEquals(3, cart.items.first().quantity)
    }

    @Test
    fun updateQuantity_shouldChangeItemQuantity_whenProductInCart() {
        val userId = "test-user-${System.nanoTime()}"
        val product = createTestProduct()

        cartService.addItem(userId, product.id, 1)
        cartService.updateQuantity(userId, product.id, 5)

        val cart = cartService.getCart(userId)

        assertEquals(5, cart.items.first().quantity)
    }

    @Test
    fun removeItem_shouldRemoveProduct_whenProductInCart() {
        val userId = "test-user-${System.nanoTime()}"
        val product = createTestProduct()

        cartService.addItem(userId, product.id, 1)
        cartService.removeItem(userId, product.id)

        val cart = cartService.getCart(userId)

        assertTrue(cart.items.isEmpty())
    }

    @Test
    fun clearCart_shouldRemoveAllItems() {
        val userId = "test-user-${System.nanoTime()}"
        val firstProduct = createTestProduct(name = "Milk")
        val secondProduct = createTestProduct(name = "Bread")

        cartService.addItem(userId, firstProduct.id, 1)
        cartService.addItem(userId, secondProduct.id, 2)

        cartService.clearCart(userId)

        val cart = cartService.getCart(userId)

        assertTrue(cart.items.isEmpty())
    }

    @Test
    fun addItem_shouldThrowBadRequestException_whenQuantityIsZero() {
        val userId = "test-user-${System.nanoTime()}"
        val product = createTestProduct()

        assertThrows(BadRequestException::class.java) {
            cartService.addItem(userId, product.id, 0)
        }
    }

    @Test
    fun addItem_shouldThrowBadRequestException_whenQuantityIsNegative() {
        val userId = "test-user-${System.nanoTime()}"
        val product = createTestProduct()

        assertThrows(BadRequestException::class.java) {
            cartService.addItem(userId, product.id, -10)
        }
    }

    @Test
    fun addItem_shouldThrowNotFoundException_whenProductDoesNotExist() {
        val userId = "test-user-${System.nanoTime()}"

        assertThrows(NotFoundException::class.java) {
            cartService.addItem(userId, ObjectId().toHexString(), 1)
        }
    }

    @Test
    fun updateQuantity_shouldThrowBadRequestException_whenQuantityIsZero() {
        val userId = "test-user-${System.nanoTime()}"
        val product = createTestProduct()

        cartService.addItem(userId, product.id, 1)

        assertThrows(BadRequestException::class.java) {
            cartService.updateQuantity(userId, product.id, 0)
        }
    }

    @Test
    fun updateQuantity_shouldThrowBadRequestException_whenQuantityIsNegative() {
        val userId = "test-user-${System.nanoTime()}"
        val product = createTestProduct()

        cartService.addItem(userId, product.id, 1)

        assertThrows(BadRequestException::class.java) {
            cartService.updateQuantity(userId, product.id, -5)
        }
    }

    @Test
    fun updateQuantity_shouldThrowNotFoundException_whenProductNotInCart() {
        val userId = "test-user-${System.nanoTime()}"
        val product = createTestProduct()

        assertThrows(NotFoundException::class.java) {
            cartService.updateQuantity(userId, product.id, 1)
        }
    }
}















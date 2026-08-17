package com.example.grocery.cart

import com.example.grocery.product.Product
import com.example.grocery.product.ProductRepository
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.bson.types.ObjectId

@QuarkusTest
class CartServiceTest {

    @Inject
    lateinit var cartService: CartService

    @Inject
    lateinit var cartRepository: CartRepository

    @Inject
    lateinit var productRepository: ProductRepository

    @AfterEach
    fun cleanup() {
        cartRepository.deleteAll()
        productRepository.deleteAll()
    }

    private fun createTestProduct(name: String = "Milk", priceInCents: Long = 249): Product {
        val product = Product(name = name, priceInCents = priceInCents)
        productRepository.persist(product)
        return product
    }

    @Test
    fun addItem_shouldAddNewProduct_whenProductNotAlreadyInCart() {
        val userId = "test-user-${System.nanoTime()}"
        val product = createTestProduct()

        cartService.addItem(userId, product.id.toString(), 1)

        val cart = cartService.getCart(userId)

        assertEquals(1, cart.items.size)
        assertEquals(1, cart.items.first().quantity)
    }

    @Test
    fun addItem_shouldIncreaseQuantity_whenProductAlreadyInCart() {
        val userId = "test-user-${System.nanoTime()}"
        val product = createTestProduct()

        cartService.addItem(userId, product.id.toString(), 1)
        cartService.addItem(userId, product.id.toString(), 2)

        val cart = cartService.getCart(userId)

        assertEquals(1, cart.items.size)
        assertEquals(3, cart.items.first().quantity)
    }

    @Test
    fun updateQuantity_shouldChangeItemQuantity_whenProductInCart() {
        val userId = "test-user-${System.nanoTime()}"
        val product = createTestProduct()

        cartService.addItem(userId, product.id.toString(), 1)
        cartService.updateQuantity(userId, product.id.toString(), 5)

        val cart = cartService.getCart(userId)

        assertEquals(5, cart.items.first().quantity)
    }

    @Test
    fun removeItem_shouldRemoveProduct_whenProductInCart() {
        val userId = "test-user-${System.nanoTime()}"
        val product = createTestProduct()

        cartService.addItem(userId, product.id.toString(), 1)
        cartService.removeItem(userId, product.id.toString())

        val cart = cartService.getCart(userId)

        assertTrue(cart.items.isEmpty())
    }

    @Test
    fun clearCart_shouldRemoveAllItems() {
        val userId = "test-user-${System.nanoTime()}"
        val firstProduct = createTestProduct(name = "Milk")
        val secondProduct = createTestProduct(name = "Bread")

        cartService.addItem(userId, firstProduct.id.toString(), 1)
        cartService.addItem(userId, secondProduct.id.toString(), 2)

        cartService.clearCart(userId)

        val cart = cartService.getCart(userId)

        assertTrue(cart.items.isEmpty())
    }

    @Test
    fun addItem_shouldThrowBadRequestException_whenQuantityIsZero() {
        val userId = "test-user-${System.nanoTime()}"
        val product = createTestProduct()

        assertThrows(BadRequestException::class.java) {
            cartService.addItem(userId, product.id.toString(), 0)
        }
    }

    @Test
    fun addItem_shouldThrowBadRequestException_whenQuantityIsNegative() {
        val userId = "test-user-${System.nanoTime()}"
        val product = createTestProduct()

        assertThrows(BadRequestException::class.java) {
            cartService.addItem(userId, product.id.toString(), -10)
        }
    }

    @Test
    fun addItem_shouldThrowNotFoundException_whenProductDoesNotExist() {
        val userId = "test-user-${System.nanoTime()}"

        assertThrows(NotFoundException::class.java) {
            cartService.addItem(userId, ObjectId().toString(), 1)
        }
    }

    @Test
    fun updateQuantity_shouldThrowBadRequestException_whenQuantityIsZero() {
        val userId = "test-user-${System.nanoTime()}"
        val product = createTestProduct()

        cartService.addItem(userId, product.id.toString(), 1)

        assertThrows(BadRequestException::class.java) {
            cartService.updateQuantity(userId, product.id.toString(), 0)
        }
    }

    @Test
    fun updateQuantity_shouldThrowBadRequestException_whenQuantityIsNegative() {
        val userId = "test-user-${System.nanoTime()}"
        val product = createTestProduct()

        cartService.addItem(userId, product.id.toString(), 1)

        assertThrows(BadRequestException::class.java) {
            cartService.updateQuantity(userId, product.id.toString(), -5)
        }
    }

    @Test
    fun updateQuantity_shouldThrowNotFoundException_whenProductNotInCart() {
        val userId = "test-user-${System.nanoTime()}"
        val product = createTestProduct()

        assertThrows(NotFoundException::class.java) {
            cartService.updateQuantity(userId, product.id.toString(), 1)
        }
    }
}













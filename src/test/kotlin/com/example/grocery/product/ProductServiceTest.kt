package com.example.grocery.product

import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

@QuarkusTest
class ProductRepositoryTest {

    @Inject
    lateinit var productRepository: ProductRepository

    @AfterEach
    fun cleanup() {
        productRepository.deleteAll()
    }

    @Test
    fun persist_shouldStoreProductInMongoDB() {
        val product = Product(
            name = "Milk",
            priceInCents = 249
        )

        productRepository.persist(product)

        assertNotNull(product.id)
        assertEquals(1L, productRepository.count())
    }

    @Test
    fun listAll_shouldReturnStoredProducts() {
        productRepository.persist(
            Product(
                name = "Milk",
                priceInCents = 249
            )
        )

        productRepository.persist(
            Product(
                name = "Bread",
                priceInCents = 179
            )
        )

        val products = productRepository.listAll()

        assertEquals(2, products.size)
    }
}
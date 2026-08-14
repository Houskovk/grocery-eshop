package com.example.grocery.product

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ProductServiceTest {

    @Test
    fun getAllProducts_shouldReturnInitialProductsCount() {
        val productService = ProductService()

        val products = productService.getAllProducts()

        assertEquals(5, products.size)
    }

    @Test
    fun getProductById_shouldReturnCorrectProduct() {
        val productService = ProductService()

        val product = productService.getProductById("1")

        assertEquals("Milk", product?.name)
        assertEquals(249, product?.priceInCents)
    }

    @Test
    fun getProductById_shouldReturnNull_whenProductDoesNotExist() {
        val productService = ProductService()

        val product = productService.getProductById("999")

        assertNull(product)
    }

    @Test
    fun addProduct_shouldAddProductSuccessfully() {
        val productService = ProductService()
        val newProduct = Product(id = "6", name = "Butter", priceInCents = 399)

        productService.addProduct(newProduct)

        val products = productService.getAllProducts()

        assertEquals(6, products.size)
        assertEquals(newProduct, productService.getProductById("6"))
    }
}
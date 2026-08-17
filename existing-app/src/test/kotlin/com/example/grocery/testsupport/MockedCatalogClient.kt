package com.example.grocery.testsupport

import com.example.grocery.client.CatalogClient
import com.example.grocery.client.CatalogProductResponse
import jakarta.ws.rs.NotFoundException
import org.bson.types.ObjectId

class MockedCatalogClient : CatalogClient {

    private val products = mutableMapOf<String, CatalogProductResponse>()

    fun addProduct(
        name: String = "Milk",
        priceInCents: Long = 249
    ): CatalogProductResponse {
        val product = CatalogProductResponse(
            id = ObjectId().toHexString(),
            name = name,
            priceInCents = priceInCents
        )
        products[product.id] = product
        return product
    }

    fun removeProduct(productId: String) {
        products.remove(productId)
    }

    fun clear() {
        products.clear()
    }

    override fun getProducts(): List<CatalogProductResponse> = products.values.toList()

    override fun getProduct(productId: String): CatalogProductResponse =
        products[productId] ?: throw NotFoundException("Product not found: $productId")
}


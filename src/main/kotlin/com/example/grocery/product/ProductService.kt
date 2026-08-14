package com.example.grocery.product

import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ProductService {

    private val products = mutableListOf<Product>(
        Product(id = "1", name = "Milk", priceInCents = 249),
        Product(id = "2", name = "Bread", priceInCents = 179),
        Product(id = "3", name = "Apples", priceInCents = 329),
        Product(id = "4", name = "Pasta", priceInCents = 199),
        Product(id = "5", name = "Cheese", priceInCents = 449)
    )

    init {
        reset()
    }

    fun getAllProducts(): List<Product> = products

    fun getProductById(id: String): Product? {
        return products.find { product -> product.id == id }
    }

    fun addProduct(product: Product): Product {
        products.add(product)
        return product
    }

    // TODO: This method is added for testing purposes to reset the product list to its initial state and should be removed in production code.
    fun reset() {
        products.clear()
        products.addAll(
            listOf(
                Product(id = "1", name = "Milk", priceInCents = 249),
                Product(id = "2", name = "Bread", priceInCents = 179),
                Product(id = "3", name = "Apples", priceInCents = 329),
                Product(id = "4", name = "Pasta", priceInCents = 199),
                Product(id = "5", name = "Cheese", priceInCents = 449)
            )
        )
    }
}
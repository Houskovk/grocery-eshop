package com.eshop.catalog.repository

import com.eshop.catalog.model.Product
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import org.eclipse.microprofile.config.inject.ConfigProperty

@ApplicationScoped
class ProductSeeder(
    private val productRepository: ProductRepository,
    @ConfigProperty(name = "app.seed-products", defaultValue = "true")
    private val seedProducts: Boolean
) {

    fun seed(@Observes startupEvent: StartupEvent) {
        if (!seedProducts || productRepository.count() > 0) {
            return
        }

        productRepository.persist(listOf(
            Product(name = "Milk", priceInCents = 249),
            Product(name = "Bread", priceInCents = 179),
            Product(name = "Apples", priceInCents = 329),
            Product(name = "Pasta", priceInCents = 199),
            Product(name = "Cheese", priceInCents = 449)
        ))
    }
}
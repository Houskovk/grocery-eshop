package com.eshop.catalog.repository

import com.eshop.catalog.model.Product
import io.quarkus.mongodb.panache.kotlin.PanacheMongoRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ProductRepository : PanacheMongoRepository<Product> {
}
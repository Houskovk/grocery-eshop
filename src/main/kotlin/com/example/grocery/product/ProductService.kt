package com.example.grocery.product

import jakarta.enterprise.context.ApplicationScoped
import org.bson.types.ObjectId

@ApplicationScoped
class ProductService(private val productRepository: ProductRepository) {

    fun getAllProducts(): List<ProductResponse> = productRepository.listAll().map {product -> product.toResponse()}

    fun getProductById(id: String): ProductResponse? {
        if (!ObjectId.isValid(id)) {
            return null
        }
        return productRepository.findById(ObjectId(id))?.toResponse()
    }

    fun addProduct(request: CreateProductRequest): ProductResponse {
        val product = Product(
            name = request.name,
            priceInCents = request.priceInCents
        )
        productRepository.persist(product)
        return product.toResponse()
    }

    private fun Product.toResponse(): ProductResponse =
        ProductResponse(
            id = requireNotNull(id).toHexString(),
            name = name,
            priceInCents = priceInCents
        )
}
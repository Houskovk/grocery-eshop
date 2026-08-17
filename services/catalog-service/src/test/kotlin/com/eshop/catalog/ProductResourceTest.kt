package com.eshop.catalog

import com.eshop.catalog.model.Product
import com.eshop.catalog.repository.ProductRepository
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@QuarkusTest
class ProductResourceTest {

    @Inject
    lateinit var productRepository: ProductRepository

    @AfterEach
    fun cleanup() {
        productRepository.deleteAll()
    }

    @Test
    fun GET_products_shouldReturnProductsFromMongoDB() {
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

        given()
            .`when`()
            .get("/products")
            .then()
            .statusCode(200)
            .body("", hasSize<Any>(2))
            .body("[0].name", equalTo("Milk"))
            .body("[1].name", equalTo("Bread"))
    }

    @Test
    fun GET_product_shouldReturnProduct() {
        val product = Product(
            name = "Milk",
            priceInCents = 249
        )

        productRepository.persist(product)

        given()
            .`when`()
            .get("/products/${product.id!!.toHexString()}")
            .then()
            .statusCode(200)
            .body("name", equalTo("Milk"))
            .body("priceInCents", equalTo(249))
    }

    @Test
    fun GET_product_shouldReturn404_whenIdIsInvalid() {
        given()
            .`when`()
            .get("/products/not-an-object-id")
            .then()
            .statusCode(404)
    }

    @Test
    fun GET_product_shouldReturn404_whenProductIsMissing() {
        val missingId = org.bson.types.ObjectId().toHexString()

        given()
            .`when`()
            .get("/products/$missingId")
            .then()
            .statusCode(404)
    }

    @Test
    fun POST_product_shouldCreateProduct() {
        val requestBody = """
            {
                "name": "Eggs",
                "priceInCents": 299
            }
        """.trimIndent()

        given()
            .contentType("application/json")
            .body(requestBody)
            .`when`()
            .post("/products")
            .then()
            .statusCode(201)
            .body("name", equalTo("Eggs"))
            .body("priceInCents", equalTo(299))

        assertEquals(1L, productRepository.count())

        val storedProduct = productRepository.listAll().first()

        assertEquals("Eggs", storedProduct.name)
        assertEquals(299L, storedProduct.priceInCents)
    }

    @Test
    fun createdProduct_shouldBeRetrieved_whenRequested() {
        val requestBody = """
            {
                "name": "Cheese",
                "priceInCents": 499
            }
        """.trimIndent()

        val id = given()
            .contentType("application/json")
            .body(requestBody)
            .`when`()
            .post("/products")
            .then()
            .statusCode(201)
            .extract()
            .path<String>("id")

        given()
            .`when`()
            .get("/products/$id")
            .then()
            .statusCode(200)
            .body("name", equalTo("Cheese"))
            .body("priceInCents", equalTo(499))
    }
}
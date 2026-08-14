package com.example.grocery.product

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import jakarta.inject.Inject

@QuarkusTest
class ProductResourceTest {

    @Inject
    lateinit var productService: ProductService

    @BeforeEach
    fun resetProductService() {
        productService.reset()
    }

    @Test
    fun GET_products_shouldReturnAllProducts() {

        given()
            .`when`()
            .get("/products")
            .then()
            .statusCode(200)
            .body("", hasSize<Any>(5))
            .body("[0].name", equalTo("Milk"))
    }

    @Test
    fun GET_productById_shouldReturnCorrectProduct() {

        given()
            .`when`()
            .get("/products/1")
            .then()
            .statusCode(200)
            .body("id", equalTo("1"))
            .body("name", equalTo("Milk"))
            .body("priceInCents", equalTo(249))
    }

    @Test
    fun GET_productById_shouldReturnNotFound_whenProductDoesNotExist() {

        given()
            .`when`()
            .get("/products/999")
            .then()
            .statusCode(404)
    }

    @Test
    fun POST_product_shouldCreateProductSuccessfully() {
        val newProduct = Product(id = "100", name = "Butter", priceInCents = 399)

        given()
            .contentType("application/json")
            .body(newProduct)
            .`when`()
            .post("/products")
            .then()
            .statusCode(201)
            .body("id", equalTo("100"))
            .body("name", equalTo("Butter"))
            .body("priceInCents", equalTo(399))
    }
}
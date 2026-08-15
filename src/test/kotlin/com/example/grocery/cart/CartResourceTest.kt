package com.example.grocery.cart

import com.example.grocery.product.Product
import com.example.grocery.product.ProductRepository
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import jakarta.inject.Inject
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

@QuarkusTest
class CartResourceTest {

    @Inject
    lateinit var productRepository: ProductRepository

    @AfterEach
    fun cleanup() {
        productRepository.deleteAll()
    }

    private fun register(username: String, password: String) {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "username": "$username",
                    "password": "$password"
                }
                """.trimIndent()
            )
            .`when`()
            .post("/auth/register")
            .then()
            .statusCode(201)
    }

    private fun login(username: String, password: String): String {
        return given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "username": "$username",
                    "password": "$password"
                }
                """.trimIndent()
            )
            .`when`()
            .post("/auth/login")
            .then()
            .statusCode(200)
            .extract()
            .path("token")
    }

    private fun createTestUserToken(): String {
        val username = "cart-user-${System.nanoTime()}"
        val password = "password123"

        register(username, password)
        return login(username, password)
    }

    private fun createTestProduct(name: String = "Milk", priceInCents: Long = 249): Product {
        val product = Product(name = name, priceInCents = priceInCents)
        productRepository.persist(product)
        return product
    }

    @Test
    fun getCart_shouldRequireAuthentication() {
        given()
            .`when`()
            .get("/cart")
            .then()
            .statusCode(401)
    }

    @Test
    fun getCart_shouldReturnEmptyCart_whenAuthenticatedUserHasNoItems() {
        val token = createTestUserToken()

        given()
            .auth()
            .oauth2(token)
            .`when`()
            .get("/cart")
            .then()
            .statusCode(200)
            .body("items", equalTo(emptyList<Any>()))
            .body("total", equalTo(0))
    }

    @Test
    fun addItem_shouldRequireAuthentication() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "productId": "000000000000000000000000",
                    "quantity": 1
                }
                """.trimIndent()
            )
            .`when`()
            .post("/cart/items")
            .then()
            .statusCode(401)
    }

    @Test
    fun addItem_shouldAddProductToCart_whenAuthenticated() {
        val token = createTestUserToken()
        val product = createTestProduct()

        given()
            .auth()
            .oauth2(token)
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "productId": "${product.id}",
                    "quantity": 2
                }
                """.trimIndent()
            )
            .`when`()
            .post("/cart/items")
            .then()
            .statusCode(200)
            .body("items[0].productId", equalTo(product.id.toString()))
            .body("items[0].quantity", equalTo(2))
            .body("total", equalTo(498))
    }

    @Test
    fun addItem_shouldRejectZeroQuantity() {
        val token = createTestUserToken()
        val product = createTestProduct()

        given()
            .auth()
            .oauth2(token)
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "productId": "${product.id}",
                    "quantity": 0
                }
                """.trimIndent()
            )
            .`when`()
            .post("/cart/items")
            .then()
            .statusCode(400)
    }

    @Test
    fun addItem_shouldRejectNegativeQuantity() {
        val token = createTestUserToken()
        val product = createTestProduct()

        given()
            .auth()
            .oauth2(token)
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "productId": "${product.id}",
                    "quantity": -1
                }
                """.trimIndent()
            )
            .`when`()
            .post("/cart/items")
            .then()
            .statusCode(400)
    }

    @Test
    fun addItem_shouldRejectNonexistentProduct() {
        val token = createTestUserToken()

        given()
            .auth()
            .oauth2(token)
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "productId": "000000000000000000000000",
                    "quantity": 1
                }
                """.trimIndent()
            )
            .`when`()
            .post("/cart/items")
            .then()
            .statusCode(404)
    }
}


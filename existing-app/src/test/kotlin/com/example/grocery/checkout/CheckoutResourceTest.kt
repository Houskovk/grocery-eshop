package com.example.grocery.checkout

import com.example.grocery.product.Product
import com.example.grocery.product.ProductRepository
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import jakarta.inject.Inject
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

@QuarkusTest
class CheckoutResourceTest {

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
        val username = "checkout-user-${System.nanoTime()}"
        val password = "password123"

        register(username, password)
        return login(username, password)
    }

    private fun createTestProduct(name: String = "Milk", priceInCents: Long = 249): Product {
        val product = Product(name = name, priceInCents = priceInCents)
        productRepository.persist(product)
        return product
    }

    private fun addFunds(token: String, amountInCents: Long) {
        given()
            .auth()
            .oauth2(token)
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "amountInCents": $amountInCents
                }
                """.trimIndent()
            )
            .`when`()
            .post("/users/me/balance/add")
            .then()
            .statusCode(200)
    }

    private fun addToCart(token: String, productId: String, quantity: Int) {
        given()
            .auth()
            .oauth2(token)
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "productId": "$productId",
                    "quantity": $quantity
                }
                """.trimIndent()
            )
            .`when`()
            .post("/cart/items")
            .then()
            .statusCode(200)
    }

    @Test
    fun checkout_shouldRequireAuthentication() {
        given()
            .`when`()
            .post("/checkout")
            .then()
            .statusCode(401)
    }

    @Test
    fun checkout_shouldCompletePurchase_whenCartHasItemsAndBalanceIsSufficient() {
        val token = createTestUserToken()
        val product = createTestProduct(priceInCents = 249)

        addFunds(token, 10000)
        addToCart(token, product.id.toString(), 2)

        given()
            .auth()
            .oauth2(token)
            .`when`()
            .post("/checkout")
            .then()
            .statusCode(200)
            .body("orderId", notNullValue())
            .body("totalInCents", equalTo(498))
            .body("remainingBalanceInCents", equalTo(10000 - 498))
    }

    @Test
    fun checkout_shouldRejectCheckout_whenCartIsEmpty() {
        val token = createTestUserToken()

        addFunds(token, 10000)

        given()
            .auth()
            .oauth2(token)
            .`when`()
            .post("/checkout")
            .then()
            .statusCode(400)
    }

    @Test
    fun checkout_shouldRejectCheckout_whenBalanceIsInsufficient() {
        val token = createTestUserToken()
        val product = createTestProduct(priceInCents = 249)

        addToCart(token, product.id.toString(), 2)

        given()
            .auth()
            .oauth2(token)
            .`when`()
            .post("/checkout")
            .then()
            .statusCode(400)
    }

    @Test
    fun checkout_shouldMakeOrderAvailable_afterSuccessfulCheckout() {
        val token = createTestUserToken()
        val product = createTestProduct(priceInCents = 249)

        addFunds(token, 10000)
        addToCart(token, product.id.toString(), 2)

        given()
            .auth()
            .oauth2(token)
            .`when`()
            .post("/checkout")
            .then()
            .statusCode(200)

        given()
            .auth()
            .oauth2(token)
            .`when`()
            .get("/orders")
            .then()
            .statusCode(200)
            .body("size()", equalTo(1))
            .body("[0].totalInCents", equalTo(498))
    }
}


package com.example.grocery.checkout

import com.example.grocery.client.CatalogClient
import com.example.grocery.client.dto.CatalogProductResponse
import com.example.grocery.client.UserClient
import com.example.grocery.testsupport.MockedCatalogClient
import com.example.grocery.testsupport.MockedUserClient
import com.example.grocery.testsupport.TestJwt
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.InjectMock
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.bson.types.ObjectId
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.kotlin.any

@QuarkusTest
class CheckoutResourceTest {

    @InjectMock
    @RestClient
    lateinit var catalogClient: CatalogClient

    @InjectMock
    @RestClient
    lateinit var userClient: UserClient

    private val productBackend = MockedCatalogClient()
    private val userBackend = MockedUserClient()

    @BeforeEach
    fun setup() {
        Mockito.`when`(catalogClient.getProduct(anyString())).thenAnswer { invocation ->
            productBackend.getProduct(invocation.getArgument(0))
        }
        Mockito.`when`(catalogClient.getProducts()).thenAnswer {
            productBackend.getProducts()
        }
        Mockito.`when`(userClient.getUser(anyString())).thenAnswer { invocation ->
            userBackend.getUser(invocation.getArgument(0))
        }
        Mockito.`when`(userClient.updateBalance(anyString(), any())).thenAnswer { invocation ->
            userBackend.updateBalance(invocation.getArgument(0), invocation.getArgument(1))
        }
    }

    @AfterEach
    fun cleanup() {
        productBackend.clear()
        userBackend.clear()
    }

    private fun createTestUserToken(balanceInCents: Long = 0): String {
        val userId = ObjectId().toHexString()
        userBackend.seedUser(userId, balanceInCents)
        return TestJwt.generate(userId)
    }

    private fun createTestProduct(name: String = "Milk", priceInCents: Long = 249): CatalogProductResponse =
        productBackend.addProduct(name = name, priceInCents = priceInCents)

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
        val token = createTestUserToken(balanceInCents = 10000)
        val product = createTestProduct(priceInCents = 249)

        addToCart(token, product.id, 2)

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
        val token = createTestUserToken(balanceInCents = 10000)

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

        addToCart(token, product.id, 2)

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
        val token = createTestUserToken(balanceInCents = 10000)
        val product = createTestProduct(priceInCents = 249)

        addToCart(token, product.id, 2)

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
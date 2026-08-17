package com.example.grocery.cart

import com.example.grocery.client.CatalogClient
import com.example.grocery.client.dto.CatalogProductResponse
import com.example.grocery.testsupport.MockedCatalogClient
import com.example.grocery.testsupport.TestJwt
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.InjectMock
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.bson.types.ObjectId
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito

@QuarkusTest
class CartResourceTest {

    @InjectMock
    @RestClient
    lateinit var catalogClient: CatalogClient

    private val productBackend = MockedCatalogClient()

    @BeforeEach
    fun setup() {
        Mockito.`when`(catalogClient.getProduct(anyString())).thenAnswer { invocation ->
            productBackend.getProduct(invocation.getArgument(0))
        }
        Mockito.`when`(catalogClient.getProducts()).thenAnswer {
            productBackend.getProducts()
        }
    }

    @AfterEach
    fun cleanup() {
        productBackend.clear()
    }

    private fun createTestUserToken(): String {
        val userId = ObjectId().toHexString()
        return TestJwt.generate(userId)
    }

    private fun createTestProduct(name: String = "Milk", priceInCents: Long = 249): CatalogProductResponse =
        productBackend.addProduct(name = name, priceInCents = priceInCents)


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
            .body("items[0].productId", equalTo(product.id))
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


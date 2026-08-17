package com.eshop.user

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
class WalletResourceTest {

    private lateinit var token: String

    @BeforeEach
    fun setUp() {
        val username = "wallet-user-${System.nanoTime()}"
        val password = "password123"

        register(username, password)
        token = login(username, password)
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

    private fun addMoney(token: String, amountInCents: Long) {
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

    @Test
    fun balance_shouldRetrieveMoney_whenUserIsAuthenticated() {
        given()
            .auth()
            .oauth2(token)
            .`when`()
            .get("/users/me/balance")
            .then()
            .statusCode(200)
            .body("balanceInCents", equalTo(0))
    }

    @Test
    fun balance_shouldBeRejected_whenUserIsUnauthenticated() {
        given()
            .`when`()
            .get("/users/me/balance")
            .then()
            .statusCode(401)
    }

    @Test
    fun add_shouldIncreaseBalance() {
        given()
            .auth()
            .oauth2(token)
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "amountInCents": 5000
                }
                """.trimIndent()
            )
            .`when`()
            .post("/users/me/balance/add")
            .then()
            .statusCode(200)
            .body("balanceInCents", equalTo(5000))
    }

    @Test
    fun add_shouldAccumulateBalance() {
        addMoney(token, 1000)
        addMoney(token, 5000)

        given()
            .auth()
            .oauth2(token)
            .`when`()
            .get("/users/me/balance")
            .then()
            .statusCode(200)
            .body("balanceInCents", equalTo(6000))
    }

    @Test
    fun add_shouldBeRejected_whenAmountIsNegative() {
        given()
            .auth()
            .oauth2(token)
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "amountInCents": -5000
                }
                """.trimIndent()
            )
            .`when`()
            .post("/users/me/balance/add")
            .then()
            .statusCode(400)
    }

    @Test
    fun add_shouldBeRejected_whenAmountIsZero() {
        given()
            .auth()
            .oauth2(token)
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "amountInCents": 0
                }
                """.trimIndent()
            )
            .`when`()
            .post("/users/me/balance/add")
            .then()
            .statusCode(400)
    }

    @Test
    fun add_shouldIncreaseBalance_whenAmountIsOneCent() {
        given()
            .auth()
            .oauth2(token)
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "amountInCents": 1
                }
                """.trimIndent()
            )
            .`when`()
            .post("/users/me/balance/add")
            .then()
            .statusCode(200)
            .body("balanceInCents", equalTo(1))
    }

    @Test
    fun add_shouldBeRejected_whenUserIsUnauthenticated() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "amountInCents": 5000
                }
                """.trimIndent()
            )
            .`when`()
            .post("/users/me/balance/add")
            .then()
            .statusCode(401)
    }
}


package com.example.grocery.order

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

@QuarkusTest
class OrderResourceTest {

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
        val username = "order-user-${System.nanoTime()}"
        val password = "password123"

        register(username, password)
        return login(username, password)
    }

    @Test
    fun getMyOrders_shouldRequireAuthentication() {
        given()
            .`when`()
            .get("/orders")
            .then()
            .statusCode(401)
    }

    @Test
    fun getMyOrders_shouldReturnEmptyList_whenUserHasNoOrders() {
        val token = createTestUserToken()

        given()
            .auth()
            .oauth2(token)
            .`when`()
            .get("/orders")
            .then()
            .statusCode(200)
            .body("", equalTo(emptyList<Any>()))
    }
}


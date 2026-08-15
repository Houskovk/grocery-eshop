package com.example.grocery.user

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test

@QuarkusTest
class AuthenticationResourceTest {

    @Test
    fun register_shouldReturnCreatedUser() {

        val username = "user-${System.nanoTime()}"

        given()
            .contentType("application/json")
            .body(
                """
                {
                    "username": "$username",
                    "password": "password123"
                }
                """.trimIndent()
            )
            .`when`()
            .post("/auth/register")
            .then()
            .statusCode(201)
            .body("username", equalTo(username))
            .body("role", equalTo("USER"))
            .body("id", notNullValue())
    }

    @Test
    fun login_shouldReturnToken() {

        val username = "login-${System.nanoTime()}"

        given()
            .contentType("application/json")
            .body(
                """
            {
                "username": "$username",
                "password": "password123"
            }
            """.trimIndent()
            )
            .post("/auth/register")
            .then()
            .statusCode(201)

        given()
            .contentType("application/json")
            .body(
                """
            {
                "username": "$username",
                "password": "password123"
            }
            """.trimIndent()
            )
            .post("/auth/login")
            .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("username", equalTo(username))
    }

    @Test
    fun login_shouldRejectPassword_whenPasswordIsIncorrect() {

        val username = "wrong-password-${System.nanoTime()}"

        given()
            .contentType("application/json")
            .body(
                """
            {
                "username": "$username",
                "password": "correct-password"
            }
            """.trimIndent()
            )
            .post("/auth/register")
            .then()
            .statusCode(201)

        given()
            .contentType("application/json")
            .body(
                """
            {
                "username": "$username",
                "password": "wrong-password"
            }
            """.trimIndent()
            )
            .post("/auth/login")
            .then()
            .statusCode(401)
    }

    @Test
    fun login_shouldRejectProfileRequest_whenTokenIsMissing() {

        given()
            .get("/users/me")
            .then()
            .statusCode(401)
    }

    @Test
    fun usersMe_shouldReturnCurrentUser() {

        val username = "profile-${System.nanoTime()}"

        given()
            .contentType("application/json")
            .body(
                """
            {
                "username": "$username",
                "password": "password123"
            }
            """.trimIndent()
            )
            .post("/auth/register")
            .then()
            .statusCode(201)

        val token =
            given()
                .contentType("application/json")
                .body(
                    """
                {
                    "username": "$username",
                    "password": "password123"
                }
                """.trimIndent()
                )
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path<String>("token")

        given()
            .header(
                "Authorization",
                "Bearer $token"
            )
            .get("/users/me")
            .then()
            .statusCode(200)
            .body("username", equalTo(username))
            .body("role", equalTo("USER"))
    }
}
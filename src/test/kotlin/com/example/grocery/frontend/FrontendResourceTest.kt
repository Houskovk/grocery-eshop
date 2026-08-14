package com.example.grocery.frontend

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test

@QuarkusTest
class FrontendResourceTest {

    @Test
    fun homePage_shouldBeServed() {
        given()
            .`when`()
            .get("/")
            .then()
            .statusCode(200)
            .contentType("text/html")
            .body(
                containsString("Grocery Shop")
            )
    }

    @Test
    fun stylesheet_shouldBeServed() {
        given()
            .`when`()
            .get("/styles.css")
            .then()
            .statusCode(200)
            .body(
                containsString(".product-grid")
            )
    }

    @Test
    fun frontendJavaScript_shouldBeServed() {
        given()
            .`when`()
            .get("/app.js")
            .then()
            .statusCode(200)
            .body(
                containsString("loadProducts")
            )
            .body(
                containsString("fetch(\"/products\")")
            )
    }
}
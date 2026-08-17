package com.eshop.order

import com.eshop.order.testsupport.TestJwt
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.bson.types.ObjectId
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

@QuarkusTest
class OrderResourceTest {

    private fun createTestUserToken(): String {
        val userId = ObjectId().toHexString()
        return TestJwt.generate(userId)
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


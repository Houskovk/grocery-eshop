package com.eshop.order.resource

import com.eshop.order.dto.AddCartItemRequest
import com.eshop.order.dto.CartResponse
import com.eshop.order.dto.UpdateCartItemRequest
import com.eshop.order.service.CartService
import io.quarkus.security.Authenticated
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.jwt.JsonWebToken

@Path("/cart")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class CartResource(private val cartService: CartService, private val jwt: JsonWebToken) {

    @GET
    @Authenticated
    fun getCart(): CartResponse {
        return cartService.getCart(currentUserId())
    }

    @POST
    @Path("/items")
    @Authenticated
    fun addItem(request: AddCartItemRequest): CartResponse {
        val userId = currentUserId()

        cartService.addItem(userId, request.productId, request.quantity)

        return cartService.getCart(userId)
    }

    @PUT
    @Path("/items/{productId}")
    @Authenticated
    fun updateQuantity(@PathParam("productId") productId: String, request: UpdateCartItemRequest): CartResponse {
        val userId = currentUserId()

        cartService.updateQuantity(userId, productId, request.quantity)

        return cartService.getCart(userId)
    }

    @DELETE
    @Path("/items/{productId}")
    @Authenticated
    fun removeItem(@PathParam("productId") productId: String): CartResponse {
        val userId = currentUserId()

        cartService.removeItem(userId, productId)
        return cartService.getCart(userId)
    }

    @DELETE
    @Authenticated
    fun clearCart(): CartResponse {
        val userId = currentUserId()

        cartService.clearCart(userId)
        return cartService.getCart(userId)
    }

    private fun currentUserId(): String {
        return jwt.subject
    }
}


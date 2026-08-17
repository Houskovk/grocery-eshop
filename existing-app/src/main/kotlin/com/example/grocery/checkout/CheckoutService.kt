package com.example.grocery.checkout

import com.example.grocery.cart.CartService
import com.example.grocery.checkout.dto.CheckoutResponse
import com.example.grocery.client.CatalogClient
import com.example.grocery.client.dto.UpdateBalanceRequest
import com.example.grocery.client.UserClient
import com.example.grocery.order.OrderItem
import com.example.grocery.order.OrderService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.WebApplicationException
import org.eclipse.microprofile.rest.client.inject.RestClient

@ApplicationScoped
class CheckoutService(
    @RestClient
    private val catalogClient: CatalogClient,
    @RestClient
    private val userClient: UserClient,
    private val cartService: CartService,
    private val orderService: OrderService
) {
    fun checkout(userId: String): CheckoutResponse {

        val user = try {
            userClient.getUser(userId)
        } catch (e: WebApplicationException) {
            if (e.response.status == 404) {
                throw NotFoundException("User not found: $userId")
            }
            throw e
        }

        val cart = cartService.getCart(userId)

        if (cart.items.isEmpty()) {
            throw BadRequestException("Cart is empty")
        }

        val orderItems = cart.items.map { cartItem ->
            if (cartItem.quantity <= 0) {
                throw BadRequestException("Invalid cart quantity")
            }

            val product = try {
                catalogClient.getProduct(cartItem.productId)
            } catch (e: WebApplicationException) {
                if (e.response.status == 404) {
                    throw BadRequestException("Product no longer available: ${cartItem.productId}")
                }
                throw e
            }

            val subtotal = product.priceInCents * cartItem.quantity

            OrderItem(
                productId = product.id,
                productName = product.name,
                quantity = cartItem.quantity,
                unitPriceInCents = product.priceInCents,
                subtotalInCents = subtotal
            )
        }

        val total = orderItems.sumOf { it.subtotalInCents }

        if (user.balanceInCents < total) {
            throw BadRequestException("Insufficient balance")
        }

        val remainingBalance = user.balanceInCents - total

        userClient.updateBalance(userId, UpdateBalanceRequest(remainingBalance))

        val order = orderService.createOrder(userId, orderItems, total)

        cartService.clearCart(userId)

        return CheckoutResponse(
            orderId = order.id.toString(),
            totalInCents = total,
            remainingBalanceInCents = remainingBalance,
            message = "Purchase completed successfully"
        )
    }
}
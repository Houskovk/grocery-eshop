package com.eshop.order.service

import com.eshop.order.client.CatalogClient
import com.eshop.order.client.UserClient
import com.eshop.order.client.WalletChargeRequest
import com.eshop.order.dto.CheckoutResponse
import com.eshop.order.model.OrderItem
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.WebApplicationException
import org.bson.types.ObjectId
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

        val orderId = ObjectId()

        val chargeResult = try {
            userClient.chargeWallet(userId, WalletChargeRequest(amountInCents = total, orderReference = orderId.toHexString()))
        } catch (e: WebApplicationException) {
            when (e.response.status) {
                404 -> throw NotFoundException("User not found: $userId")
                402 -> throw BadRequestException("Insufficient balance")
                else -> throw e
            }
        }

        val order = orderService.createOrder(orderId, userId, orderItems, total)

        cartService.clearCart(userId)

        return CheckoutResponse(
            orderId = order.id.toString(),
            totalInCents = total,
            remainingBalanceInCents = chargeResult.balanceInCents,
            message = "Purchase completed successfully"
        )
    }
}


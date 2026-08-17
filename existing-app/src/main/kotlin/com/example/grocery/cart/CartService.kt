package com.example.grocery.cart

import com.example.grocery.cart.dto.CartItemResponse
import com.example.grocery.cart.dto.CartResponse
import com.example.grocery.client.CatalogClient
import com.example.grocery.client.dto.CatalogProductResponse
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.WebApplicationException
import org.eclipse.microprofile.rest.client.inject.RestClient

@ApplicationScoped
class CartService(
    private val cartRepository: CartRepository,
    @RestClient
    private val catalogClient: CatalogClient
) {

    private fun getOrCreateCart(userId: String): Cart {

        val existingCart = cartRepository.findByUserId(userId)

        if (existingCart != null) {
            return existingCart
        }

        val newCart = Cart(userId = userId, items = mutableListOf())
        cartRepository.persist(newCart)

        return newCart
    }

    fun addItem(userId: String, productId: String, quantity: Int): Cart {

        if (quantity <= 0) {
            throw BadRequestException("Quantity must be greater than zero")
        }

        findProductOrThrow(productId)

        val cart = getOrCreateCart(userId)

        val existingItem = cart.items.find { it.productId == productId }

        if (existingItem != null) {
            existingItem.quantity += quantity
        } else {
            cart.items.add(CartItem(productId = productId, quantity = quantity))
        }

        cartRepository.update(cart)
        return cart
    }

    fun updateQuantity(userId: String, productId: String, quantity: Int): Cart {

        if (quantity <= 0) {
            throw BadRequestException("Quantity must be greater than zero")
        }

        val cart = getOrCreateCart(userId)

        val item = cart.items.find { it.productId == productId } ?: throw NotFoundException("Product not found in cart")

        item.quantity = quantity

        cartRepository.update(cart)

        return cart
    }

    fun removeItem(userId: String, productId: String): Cart {

        val cart = getOrCreateCart(userId)

        cart.items.removeIf { it.productId == productId }

        cartRepository.update(cart)

        return cart
    }

    fun clearCart(userId: String) {

        val cart = getOrCreateCart(userId)

        cart.items.clear()

        cartRepository.update(cart)
    }

    fun getCart(userId: String): CartResponse {

        val cart = getOrCreateCart(userId)

        val itemResponses = cart.items.mapNotNull { item ->
            val product = try {
                findProductOrThrow(item.productId)
            } catch (e: NotFoundException) {
                null
            }

            product?.let {
                val lineTotal = it.priceInCents * item.quantity
                CartItemResponse(
                    productId = it.id.toString(),
                    name = it.name,
                    price = it.priceInCents,
                    quantity = item.quantity,
                    lineTotal = lineTotal
                )
            }
        }

        val total = itemResponses.sumOf { it.lineTotal }

        return CartResponse(items = itemResponses, total = total)
    }

    private fun findProductOrThrow(productId: String): CatalogProductResponse {

        return try {
            catalogClient.getProduct(productId)
        } catch (e: WebApplicationException) {
            if (e.response.status == 404) {
                throw NotFoundException("Product not found: $productId")
            } else {
                throw e
            }
        }
    }

}


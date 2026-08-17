package com.example.grocery.client.dto

// Mirrors com.eshop.catalog.dto.ProductResponse in catalog-service. Only id, name and
// priceInCents are returned by catalog-service today - extend both sides together if
// description/availability get added to the catalog domain model later.
data class CatalogProductResponse(
    val id: String,
    val name: String,
    val priceInCents: Long
)
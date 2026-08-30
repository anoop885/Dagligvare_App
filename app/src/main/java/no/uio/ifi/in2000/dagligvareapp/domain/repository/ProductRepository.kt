package no.uio.ifi.in2000.dagligvareapp.domain.repository

import no.uio.ifi.in2000.dagligvareapp.domain.model.Deal
import no.uio.ifi.in2000.dagligvareapp.domain.model.PriceComparison
import no.uio.ifi.in2000.dagligvareapp.domain.model.Product

interface ProductRepository {
    suspend fun searchProducts(query: String): List<Product>
    suspend fun getPriceComparison(ean: String): PriceComparison
    suspend fun getDeals(): List<Deal>
}

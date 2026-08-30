package no.uio.ifi.in2000.dagligvareapp.data.repository

import no.uio.ifi.in2000.dagligvareapp.data.remote.KassalappApi
import no.uio.ifi.in2000.dagligvareapp.data.remote.dto.EanProductDto
import no.uio.ifi.in2000.dagligvareapp.data.remote.dto.PriceHistoryDto
import no.uio.ifi.in2000.dagligvareapp.data.remote.dto.SearchProductDto
import no.uio.ifi.in2000.dagligvareapp.domain.model.Deal
import no.uio.ifi.in2000.dagligvareapp.domain.model.PriceComparison
import no.uio.ifi.in2000.dagligvareapp.domain.model.Product
import no.uio.ifi.in2000.dagligvareapp.domain.model.StorePrice
import no.uio.ifi.in2000.dagligvareapp.domain.repository.ProductRepository
import java.time.Instant
import java.time.temporal.ChronoUnit

private const val SALE_THRESHOLD = 0.05 // 5% below average = sale

class ProductRepositoryImpl(
    private val api: KassalappApi
) : ProductRepository {

    override suspend fun searchProducts(query: String): List<Product> {
        val response = api.searchProducts(query = query, unique = 0, size = 30)
        return response.data
            .filter { it.ean != null && it.isFresh() }
            .groupBy { it.ean!! }
            .mapNotNull { (ean, entries) ->
                val cheapest = entries.minByOrNull { it.currentPrice ?: Double.MAX_VALUE } ?: return@mapNotNull null
                cheapest.toProduct(ean)
            }
    }

    override suspend fun getPriceComparison(ean: String): PriceComparison {
        val response = api.getProductsByEan(ean)
        val data = response.data
        val products = data.products

        val firstName = products.firstOrNull()?.name ?: ""
        val firstImage = products.firstOrNull()?.image

        val storeEntries = products
            .mapNotNull { it.toStorePrice() }
            .sortedBy { it.currentPrice }

        return PriceComparison(
            ean = data.ean,
            productName = firstName,
            imageUrl = firstImage,
            storeEntries = storeEntries
        )
    }

    override suspend fun getDeals(): List<Deal> {
        val response = api.searchProducts(query = "", size = 100, sort = "date_desc")
        return response.data
            .filter { it.isFresh() }
            .mapNotNull { it.toDeal() }
            .sortedByDescending { it.discountPercent }
    }

    // ── Mapping helpers ────────────────────────────────────────────────────

    private fun SearchProductDto.toProduct(ean: String): Product? {
        val price = currentPrice ?: return null
        return Product(
            ean = ean,
            name = name,
            brand = brand,
            imageUrl = image,
            weight = weight,
            weightUnit = weightUnit,
            bestPrice = price,
            bestPriceStoreName = store?.name ?: "",
            bestPriceStoreLogo = store?.logo
        )
    }

    private fun SearchProductDto.toDeal(): Deal? {
        val dealEan = ean ?: return null
        val price = currentPrice ?: return null
        val history = priceHistory ?: return null
        val avgPrice = averagePriceLast30Days(history) ?: return null

        if (price >= avgPrice * (1 - SALE_THRESHOLD)) return null

        val discountPercent = ((avgPrice - price) / avgPrice * 100).toInt()
        val savings = avgPrice - price

        return Deal(
            productId = id,
            ean = dealEan,
            name = name,
            brand = brand,
            imageUrl = image,
            storeName = store?.name ?: "",
            storeCode = store?.code ?: "",
            storeLogo = store?.logo,
            currentPrice = price,
            originalPrice = avgPrice,
            discountPercent = discountPercent,
            savingsAmount = savings
        )
    }

    private fun EanProductDto.toStorePrice(): StorePrice? {
        val price = currentPrice?.price ?: return null
        val history = priceHistory
        val avgPrice = if (!history.isNullOrEmpty()) averagePriceLast30Days(history) else null
        val isOnSale = avgPrice != null && price < avgPrice * (1 - SALE_THRESHOLD)
        val discountPercent = if (isOnSale && avgPrice != null)
            ((avgPrice - price) / avgPrice * 100).toInt()
        else null

        return StorePrice(
            productId = id,
            storeName = store?.name ?: "",
            storeCode = store?.code ?: "",
            storeLogo = store?.logo,
            currentPrice = price,
            unitPrice = currentPrice?.unitPrice,
            isOnSale = isOnSale,
            originalPrice = if (isOnSale) avgPrice else null,
            discountPercent = discountPercent
        )
    }

    // Returns true only if this product has a price history entry within the last 30 days.
    // Products the scraper hasn't touched recently are considered stale and are excluded
    // from both search results and deals so we never show outdated prices.
    private fun SearchProductDto.isFresh(): Boolean {
        val history = priceHistory ?: return false
        val cutoff = Instant.now().minus(30, ChronoUnit.DAYS)
        val latest = history.maxOfOrNull { entry ->
            runCatching { Instant.parse(entry.date) }.getOrNull() ?: Instant.MIN
        } ?: return false
        return latest.isAfter(cutoff)
    }

    private fun averagePriceLast30Days(history: List<PriceHistoryDto>): Double? {
        val cutoff = Instant.now().minus(30, ChronoUnit.DAYS)
        val recent = history.filter { entry ->
            runCatching { Instant.parse(entry.date).isAfter(cutoff) }.getOrDefault(true)
        }
        val list = recent.ifEmpty { history }
        if (list.isEmpty()) return null
        return list.map { it.price }.average()
    }
}

package no.uio.ifi.in2000.dagligvareapp.domain.model

data class PriceComparison(
    val ean: String,
    val productName: String,
    val imageUrl: String?,
    val storeEntries: List<StorePrice>
)

data class StorePrice(
    val productId: Int,
    val storeName: String,
    val storeCode: String,
    val storeLogo: String?,
    val currentPrice: Double,
    val unitPrice: Double?,
    val isOnSale: Boolean,
    val originalPrice: Double?,
    val discountPercent: Int?
)

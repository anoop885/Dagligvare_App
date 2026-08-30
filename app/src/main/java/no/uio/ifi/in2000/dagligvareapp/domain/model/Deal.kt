package no.uio.ifi.in2000.dagligvareapp.domain.model

data class Deal(
    val productId: Int,
    val ean: String,
    val name: String,
    val brand: String?,
    val imageUrl: String?,
    val storeName: String,
    val storeCode: String,
    val storeLogo: String?,
    val currentPrice: Double,
    val originalPrice: Double,
    val discountPercent: Int,
    val savingsAmount: Double
)

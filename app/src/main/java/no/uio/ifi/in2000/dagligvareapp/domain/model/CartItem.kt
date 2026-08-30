package no.uio.ifi.in2000.dagligvareapp.domain.model

data class CartItem(
    val productId: Int,
    val ean: String,
    val name: String,
    val brand: String?,
    val imageUrl: String?,
    val storeName: String,
    val storeCode: String,
    val price: Double,
    val unitPrice: Double?,
    val quantity: Int = 1
)

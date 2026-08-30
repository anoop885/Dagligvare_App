package no.uio.ifi.in2000.dagligvareapp.domain.model

/** A product grouped by EAN — shows only the best (cheapest) store price in the search list. */
data class Product(
    val ean: String,
    val name: String,
    val brand: String?,
    val imageUrl: String?,
    val weight: Double?,
    val weightUnit: String?,
    val bestPrice: Double,
    val bestPriceStoreName: String,
    val bestPriceStoreLogo: String?
)

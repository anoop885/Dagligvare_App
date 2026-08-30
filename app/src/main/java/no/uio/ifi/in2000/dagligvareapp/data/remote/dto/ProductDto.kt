package no.uio.ifi.in2000.dagligvareapp.data.remote.dto

import com.google.gson.annotations.SerializedName

// ── Search endpoint: GET /products?search=... ──────────────────────────────

data class SearchResponseDto(
    @SerializedName("data") val data: List<SearchProductDto>
)

data class SearchProductDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("brand") val brand: String?,
    @SerializedName("vendor") val vendor: String?,
    @SerializedName("ean") val ean: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("current_price") val currentPrice: Double?,
    @SerializedName("current_unit_price") val currentUnitPrice: Double?,
    @SerializedName("weight") val weight: Double?,
    @SerializedName("weight_unit") val weightUnit: String?,
    @SerializedName("store") val store: StoreDtoSimple?,
    @SerializedName("price_history") val priceHistory: List<PriceHistoryDto>?
)

// ── EAN comparison endpoint: GET /products/ean/{ean} ──────────────────────

data class EanResponseDto(
    @SerializedName("data") val data: EanDataDto
)

data class EanDataDto(
    @SerializedName("ean") val ean: String,
    @SerializedName("products") val products: List<EanProductDto>
)

data class EanProductDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("brand") val brand: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("store") val store: StoreDtoSimple?,
    @SerializedName("current_price") val currentPrice: CurrentPriceDto?,
    @SerializedName("price_history") val priceHistory: List<PriceHistoryDto>?
)

data class CurrentPriceDto(
    @SerializedName("price") val price: Double?,
    @SerializedName("unit_price") val unitPrice: Double?,
    @SerializedName("date") val date: String?
)

// ── Shared ─────────────────────────────────────────────────────────────────

data class StoreDtoSimple(
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String,
    @SerializedName("logo") val logo: String?
)

data class PriceHistoryDto(
    @SerializedName("price") val price: Double,
    @SerializedName("date") val date: String
)

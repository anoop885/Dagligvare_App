package no.uio.ifi.in2000.dagligvareapp.data.remote

import no.uio.ifi.in2000.dagligvareapp.data.remote.dto.EanResponseDto
import no.uio.ifi.in2000.dagligvareapp.data.remote.dto.SearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface KassalappApi {

    @GET("products")
    suspend fun searchProducts(
        @Query("search") query: String = "",
        @Query("unique") unique: Int = 0,
        @Query("size") size: Int = 20,
        @Query("page") page: Int = 1,
        @Query("sort") sort: String? = null
    ): SearchResponseDto

    @GET("products/ean/{ean}")
    suspend fun getProductsByEan(
        @Path("ean") ean: String
    ): EanResponseDto
}

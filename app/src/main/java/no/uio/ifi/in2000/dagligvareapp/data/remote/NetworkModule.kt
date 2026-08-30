package no.uio.ifi.in2000.dagligvareapp.data.remote

import no.uio.ifi.in2000.dagligvareapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    private const val BASE_URL = "https://kassal.app/api/v1/"

    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${BuildConfig.KASSAL_API_KEY}")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .build()
    }

    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    fun provideKassalappApi(retrofit: Retrofit): KassalappApi =
        retrofit.create(KassalappApi::class.java)
}

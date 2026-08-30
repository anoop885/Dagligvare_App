package no.uio.ifi.in2000.dagligvareapp.di

import com.google.gson.Gson
import no.uio.ifi.in2000.dagligvareapp.data.local.CartStorage
import no.uio.ifi.in2000.dagligvareapp.data.remote.NetworkModule
import no.uio.ifi.in2000.dagligvareapp.data.repository.ProductRepositoryImpl
import no.uio.ifi.in2000.dagligvareapp.domain.repository.ProductRepository
import no.uio.ifi.in2000.dagligvareapp.presentation.cart.CartViewModel
import no.uio.ifi.in2000.dagligvareapp.presentation.deals.DealsViewModel
import no.uio.ifi.in2000.dagligvareapp.presentation.search.SearchViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Network
    single { NetworkModule.provideOkHttpClient() }
    single { NetworkModule.provideRetrofit(get()) }
    single { NetworkModule.provideKassalappApi(get()) }

    // Storage
    single { Gson() }
    single { CartStorage(androidContext(), get()) }

    // Repository
    single<ProductRepository> { ProductRepositoryImpl(get()) }

    // ViewModels — call repository/storage directly, no use cases
    viewModel { SearchViewModel(get()) }
    viewModel { DealsViewModel(get()) }
    viewModel { CartViewModel(get()) }
}

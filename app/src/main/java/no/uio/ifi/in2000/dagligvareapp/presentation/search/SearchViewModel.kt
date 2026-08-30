package no.uio.ifi.in2000.dagligvareapp.presentation.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.dagligvareapp.domain.model.PriceComparison
import no.uio.ifi.in2000.dagligvareapp.domain.model.Product
import no.uio.ifi.in2000.dagligvareapp.domain.repository.ProductRepository
import no.uio.ifi.in2000.dagligvareapp.presentation.common.UiState

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _searchState = MutableStateFlow<UiState<List<Product>>>(UiState.Idle)
    val searchState: StateFlow<UiState<List<Product>>> = _searchState.asStateFlow()

    private val _comparisonState = MutableStateFlow<UiState<PriceComparison>>(UiState.Idle)
    val comparisonState: StateFlow<UiState<PriceComparison>> = _comparisonState.asStateFlow()

    init {
        _query
            .debounce(300)
            .distinctUntilChanged()
            .filter { it.isNotBlank() }
            .onEach { q -> search(q) }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        if (newQuery.isBlank()) _searchState.value = UiState.Idle
    }

    private fun search(query: String) {
        viewModelScope.launch {
            _searchState.value = UiState.Loading
            runCatching { repository.searchProducts(query) }
                .onSuccess { products ->
                    Log.d("SearchViewModel", "Got ${products.size} products for '$query'")
                    _searchState.value = UiState.Success(products)
                }
                .onFailure { e ->
                    Log.e("SearchViewModel", "Search failed: ${e.message}", e)
                    _searchState.value = UiState.Error(friendlyError(e))
                }
        }
    }

    fun loadPriceComparison(ean: String) {
        viewModelScope.launch {
            _comparisonState.value = UiState.Loading
            runCatching { repository.getPriceComparison(ean) }
                .onSuccess { _comparisonState.value = UiState.Success(it) }
                .onFailure { _comparisonState.value = UiState.Error(friendlyError(it)) }
        }
    }

    fun clearComparison() {
        _comparisonState.value = UiState.Idle
    }

    private fun friendlyError(e: Throwable): String = when {
        e.message?.contains("Unable to resolve host") == true ->
            "Ingen nettverkstilkobling. Sjekk internett og prøv igjen."
        e.message?.contains("timeout") == true ->
            "Forespørselen tok for lang tid. Prøv igjen."
        else -> "Noe gikk galt. Prøv igjen."
    }
}

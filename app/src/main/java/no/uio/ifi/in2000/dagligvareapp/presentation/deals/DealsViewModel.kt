package no.uio.ifi.in2000.dagligvareapp.presentation.deals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.dagligvareapp.domain.model.Deal
import no.uio.ifi.in2000.dagligvareapp.domain.model.PriceComparison
import no.uio.ifi.in2000.dagligvareapp.domain.repository.ProductRepository
import no.uio.ifi.in2000.dagligvareapp.presentation.common.UiState

class DealsViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    private val _dealsState = MutableStateFlow<UiState<List<Deal>>>(UiState.Idle)
    val dealsState: StateFlow<UiState<List<Deal>>> = _dealsState.asStateFlow()

    private val _comparisonState = MutableStateFlow<UiState<PriceComparison>>(UiState.Idle)
    val comparisonState: StateFlow<UiState<PriceComparison>> = _comparisonState.asStateFlow()

    init {
        loadDeals()
    }

    fun loadDeals() {
        viewModelScope.launch {
            _dealsState.value = UiState.Loading
            runCatching { repository.getDeals() }
                .onSuccess { _dealsState.value = UiState.Success(it) }
                .onFailure { _dealsState.value = UiState.Error(friendlyError(it)) }
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

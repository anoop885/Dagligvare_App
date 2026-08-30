package no.uio.ifi.in2000.dagligvareapp.presentation.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.dagligvareapp.presentation.cart.CartViewModel
import no.uio.ifi.in2000.dagligvareapp.presentation.common.UiState
import no.uio.ifi.in2000.dagligvareapp.presentation.search.components.PriceComparisonSheet
import no.uio.ifi.in2000.dagligvareapp.presentation.search.components.ProductListItem
import no.uio.ifi.in2000.dagligvareapp.presentation.search.components.ProductSearchBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchScreen(cartViewModel: CartViewModel, snackbarHostState: SnackbarHostState) {
    val viewModel: SearchViewModel = koinViewModel()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val comparisonState by viewModel.comparisonState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {

        ProductSearchBar(
            query = query,
            onQueryChange = viewModel::onQueryChange
        )

        when (val state = searchState) {
            is UiState.Idle -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Skriv i søkefeltet for å finne varer",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            }

            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.onQueryChange(query) },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Prøv igjen")
                        }
                    }
                }
            }

            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ingen varer funnet for \"$query\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(items = state.data, key = { it.ean }) { product ->
                            ProductListItem(
                                product = product,
                                onClick = { viewModel.loadPriceComparison(product.ean) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Price comparison bottom sheet — only shown when a product is tapped
    if (comparisonState !is UiState.Idle) {
        PriceComparisonSheet(
            state = comparisonState,
            onDismiss = viewModel::clearComparison,
            onAddToCart = { item ->
                cartViewModel.addToCart(item)
                viewModel.clearComparison()
                scope.launch {
                    snackbarHostState.showSnackbar("${item.name} lagt til i handlekurven")
                }
            }
        )
    }
}

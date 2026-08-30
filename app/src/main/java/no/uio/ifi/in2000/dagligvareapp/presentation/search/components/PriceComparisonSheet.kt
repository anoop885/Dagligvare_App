package no.uio.ifi.in2000.dagligvareapp.presentation.search.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.dagligvareapp.domain.model.CartItem
import no.uio.ifi.in2000.dagligvareapp.domain.model.PriceComparison
import no.uio.ifi.in2000.dagligvareapp.domain.model.StorePrice
import no.uio.ifi.in2000.dagligvareapp.presentation.common.UiState
import no.uio.ifi.in2000.dagligvareapp.ui.theme.SaleGreen
import no.uio.ifi.in2000.dagligvareapp.ui.theme.DiscountBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceComparisonSheet(
    state: UiState<PriceComparison>,
    onDismiss: () -> Unit,
    onAddToCart: (CartItem) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Prissammenligning",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Divider()
            when (state) {
                is UiState.Loading -> Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                is UiState.Error -> Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.message)
                }

                is UiState.Success -> {
                    val comparison = state.data
                    Text(
                        text = comparison.productName,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    LazyColumn {
                        items(comparison.storeEntries) { entry ->
                            StorePriceRow(
                                entry = entry,
                                productName = comparison.productName,
                                productEan = comparison.ean,
                                imageUrl = comparison.imageUrl,
                                onAddToCart = onAddToCart
                            )
                            Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }

                else -> Unit
            }
        }
    }
}

@Composable
private fun StorePriceRow(
    entry: StorePrice,
    productName: String,
    productEan: String,
    imageUrl: String?,
    onAddToCart: (CartItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.storeName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            entry.unitPrice?.let {
                Text(
                    text = "kr %.2f / kg".format(it),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(horizontal = 8.dp)) {
            if (entry.isOnSale && entry.originalPrice != null) {
                Text(
                    text = "kr %.2f".format(entry.originalPrice),
                    style = MaterialTheme.typography.bodySmall,
                    textDecoration = TextDecoration.LineThrough,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "kr %.2f".format(entry.currentPrice),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (entry.isOnSale) SaleGreen else MaterialTheme.colorScheme.onSurface
            )
            if (entry.isOnSale && entry.discountPercent != null) {
                Surface(
                    color = DiscountBadge,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "-${entry.discountPercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        IconButton(
            onClick = {
                onAddToCart(
                    CartItem(
                        productId = entry.productId,
                        ean = productEan,
                        name = productName,
                        brand = null,
                        imageUrl = imageUrl,
                        storeName = entry.storeName,
                        storeCode = entry.storeCode,
                        price = entry.currentPrice,
                        unitPrice = entry.unitPrice
                    )
                )
            }
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Legg i handlekurv",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

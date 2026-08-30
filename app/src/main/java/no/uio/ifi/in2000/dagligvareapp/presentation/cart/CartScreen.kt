package no.uio.ifi.in2000.dagligvareapp.presentation.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import no.uio.ifi.in2000.dagligvareapp.domain.model.CartItem

@Composable
fun CartScreen(viewModel: CartViewModel) {
    val summary by viewModel.cartSummary.collectAsStateWithLifecycle()

    // Empty state — nothing in the cart yet
    if (summary == null || summary!!.totalItems == 0) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Handlekurven er tom.\nSøk etter varer for å legge til.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
        }
        return
    }

    val itemsByStore = summary!!.itemsByStore
    val totalPrice = summary!!.totalPrice
    var showClearDialog by remember { mutableStateOf(false) }

    // Confirmation dialog before wiping the whole cart
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Tøm handlekurv?") },
            text = { Text("Er du sikker på at du vil fjerne alle varene fra handlekurven?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearCart()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Tøm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Avbryt")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // Scrollable list of items grouped by store
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsByStore.forEach { (storeName, items) ->

                // Store header
                item {
                    Text(
                        text = storeName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }

                // One row per cart item under this store
                items(items = items, key = { "${it.productId}-${it.storeCode}" }) { item ->
                    CartItemRow(
                        item = item,
                        onIncrement = {
                            viewModel.updateQuantity(item.productId, item.storeCode, item.quantity + 1)
                        },
                        onDecrement = {
                            viewModel.updateQuantity(item.productId, item.storeCode, item.quantity - 1)
                        },
                        onRemove = {
                            viewModel.removeItem(item.productId, item.storeCode)
                        }
                    )
                }
            }

            // Bottom spacing so the total bar doesn't overlap last item
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        // Total + clear cart at the bottom
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Totalt",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "kr %.2f".format(totalPrice),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tøm handlekurv")
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Product image with placeholder when missing or failed to load
        SubcomposeAsyncImage(
            model = item.imageUrl,
            contentDescription = item.name,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Fit,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            success = { SubcomposeAsyncImageContent() }
        )
        Spacer(modifier = Modifier.width(12.dp))

        // Name + price per unit
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
            Text(
                text = "kr %.2f / stk".format(item.price),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Quantity controls: − number +
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrement, modifier = Modifier.size(32.dp)) {
                // Minus icon using text since minus isn't in core icons
                Text(
                    text = "−",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = item.quantity.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(28.dp),
                textAlign = TextAlign.Center
            )
            IconButton(onClick = onIncrement, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Øk antall",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Line total (price × quantity)
        Text(
            text = "kr %.2f".format(item.price * item.quantity),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(64.dp),
            textAlign = TextAlign.End
        )
    }
}

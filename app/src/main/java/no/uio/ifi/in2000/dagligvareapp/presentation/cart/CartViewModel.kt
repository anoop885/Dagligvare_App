package no.uio.ifi.in2000.dagligvareapp.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.dagligvareapp.data.local.CartStorage
import no.uio.ifi.in2000.dagligvareapp.domain.model.CartItem

data class CartSummary(
    val itemsByStore: Map<String, List<CartItem>>,
    val totalPrice: Double,
    val totalItems: Int
)

class CartViewModel(
    private val cartStorage: CartStorage
) : ViewModel() {

    val cartSummary: StateFlow<CartSummary?> = cartStorage.cartItems
        .map { items ->
            CartSummary(
                itemsByStore = items.groupBy { it.storeName },
                totalPrice = items.sumOf { it.price * it.quantity },
                totalItems = items.sumOf { it.quantity }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun addToCart(item: CartItem) {
        viewModelScope.launch { cartStorage.addOrUpdateItem(item) }
    }

    fun updateQuantity(productId: Int, storeCode: String, quantity: Int) {
        viewModelScope.launch { cartStorage.updateQuantity(productId, storeCode, quantity) }
    }

    fun removeItem(productId: Int, storeCode: String) {
        viewModelScope.launch { cartStorage.removeItem(productId, storeCode) }
    }

    fun clearCart() {
        viewModelScope.launch { cartStorage.clearCart() }
    }
}

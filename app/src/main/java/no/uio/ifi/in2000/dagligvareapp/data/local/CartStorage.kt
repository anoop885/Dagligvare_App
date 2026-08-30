package no.uio.ifi.in2000.dagligvareapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import no.uio.ifi.in2000.dagligvareapp.domain.model.CartItem

// Creates a single DataStore file called "cart_prefs.preferences_pb" on the device.
// The `by` delegate means this is a lazy property — the file is only opened when first accessed.
// Stored at: /data/data/<app-package>/files/datastore/cart_prefs.preferences_pb
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cart_prefs")

class CartStorage(
    private val context: Context,
    private val gson: Gson  // Gson converts CartItem objects to/from JSON strings
) {
    // The key used to store the cart inside DataStore — think of it like a map key.
    // DataStore is a key-value store, so everything in our cart is stored under this one key
    // as a single JSON string (e.g. "[{...item1...}, {...item2...}]")
    private val cartKey = stringPreferencesKey("cart_items")

    // A continuous stream (Flow) of the current cart items.
    // Any time the DataStore file changes, this Flow emits the updated list automatically.
    // The ViewModel collects this and exposes it to the UI.
    val cartItems: Flow<List<CartItem>> = context.dataStore.data.map { prefs ->
        // Read the raw JSON string from DataStore. If it doesn't exist yet, return empty list.
        val json = prefs[cartKey] ?: return@map emptyList()
        // Try to convert the JSON string back into a List<CartItem>.
        // If parsing fails for any reason (corrupted data etc.), fall back to empty list.
        runCatching {
            val type = object : TypeToken<List<CartItem>>() {}.type
            gson.fromJson<List<CartItem>>(json, type)
        }.getOrDefault(emptyList())
    }

    // Adds a new item to the cart, or increments its quantity if it already exists.
    // "Same item" means same productId AND same storeCode (same product, same store).
    suspend fun addOrUpdateItem(item: CartItem) {
        context.dataStore.edit { prefs ->
            val current = deserialize(prefs[cartKey])  // load current cart
            val existing = current.indexOfFirst {
                it.productId == item.productId && it.storeCode == item.storeCode
            }
            val updated = if (existing >= 0) {
                // Item already in cart — just bump its quantity by 1
                current.toMutableList().also {
                    it[existing] = it[existing].copy(quantity = it[existing].quantity + 1)
                }
            } else {
                // New item — append it to the list
                current + item
            }
            // Serialize the updated list back to JSON and save it
            prefs[cartKey] = gson.toJson(updated)
        }
    }

    // Sets the quantity of a specific item directly.
    // If quantity is 0 or less, the item is removed from the cart entirely.
    suspend fun updateQuantity(productId: Int, storeCode: String, quantity: Int) {
        context.dataStore.edit { prefs ->
            val current = deserialize(prefs[cartKey])
            val updated = if (quantity <= 0) {
                // Remove the item completely
                current.filter { !(it.productId == productId && it.storeCode == storeCode) }
            } else {
                // Update the quantity for the matching item, leave all others unchanged
                current.map {
                    if (it.productId == productId && it.storeCode == storeCode) it.copy(quantity = quantity)
                    else it
                }
            }
            prefs[cartKey] = gson.toJson(updated)
        }
    }

    // Removes a specific item from the cart entirely, regardless of quantity.
    suspend fun removeItem(productId: Int, storeCode: String) {
        context.dataStore.edit { prefs ->
            val current = deserialize(prefs[cartKey])
            prefs[cartKey] = gson.toJson(
                current.filter { !(it.productId == productId && it.storeCode == storeCode) }
            )
        }
    }

    // Wipes the entire cart by saving an empty JSON array.
    suspend fun clearCart() {
        context.dataStore.edit { prefs -> prefs[cartKey] = "[]" }
    }

    // Helper that safely converts a JSON string to List<CartItem>.
    // Returns empty list if the string is null, blank, or malformed.
    private fun deserialize(json: String?): List<CartItem> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            val type = object : TypeToken<List<CartItem>>() {}.type
            gson.fromJson<List<CartItem>>(json, type)
        }.getOrDefault(emptyList())
    }
}

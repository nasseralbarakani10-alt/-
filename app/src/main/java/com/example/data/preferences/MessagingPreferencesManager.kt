package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MessagingPreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "trend_messaging_preferences",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_SHOP_PHONE_NUMBER = "shop_phone_number"
        private const val KEY_STOP_SHOP_MESSAGING = "stop_shop_messaging"
        private const val KEY_STOP_CUSTOMER_MESSAGING = "stop_customer_messaging_on_ready"
        private const val PREF_CUSTOMER_ALLOWED_PREFIX = "cust_msg_allowed_"
    }

    private val _shopPhoneNumber = MutableStateFlow(prefs.getString(KEY_SHOP_PHONE_NUMBER, "") ?: "")
    val shopPhoneNumber: StateFlow<String> = _shopPhoneNumber.asStateFlow()

    private val _stopShopMessaging = MutableStateFlow(prefs.getBoolean(KEY_STOP_SHOP_MESSAGING, false))
    val stopShopMessaging: StateFlow<Boolean> = _stopShopMessaging.asStateFlow()

    private val _stopCustomerMessagingOnReady = MutableStateFlow(prefs.getBoolean(KEY_STOP_CUSTOMER_MESSAGING, false))
    val stopCustomerMessagingOnReady: StateFlow<Boolean> = _stopCustomerMessagingOnReady.asStateFlow()

    fun getShopPhoneNumber(): String = prefs.getString(KEY_SHOP_PHONE_NUMBER, "") ?: ""

    fun setShopPhoneNumber(phone: String) {
        val trimmed = phone.trim()
        prefs.edit().putString(KEY_SHOP_PHONE_NUMBER, trimmed).apply()
        _shopPhoneNumber.value = trimmed
    }

    fun isStopShopMessaging(): Boolean = prefs.getBoolean(KEY_STOP_SHOP_MESSAGING, false)

    fun setStopShopMessaging(stop: Boolean) {
        prefs.edit().putBoolean(KEY_STOP_SHOP_MESSAGING, stop).apply()
        _stopShopMessaging.value = stop
    }

    fun isStopCustomerMessagingOnReady(): Boolean = prefs.getBoolean(KEY_STOP_CUSTOMER_MESSAGING, false)

    fun setStopCustomerMessagingOnReady(stop: Boolean) {
        prefs.edit().putBoolean(KEY_STOP_CUSTOMER_MESSAGING, stop).apply()
        _stopCustomerMessagingOnReady.value = stop
    }

    /**
     * Customer-specific messaging configuration.
     * Defaults to true (messaging enabled for this customer).
     * Changing global settings does not modify this preference.
     * Changing one customer does not affect any other customer.
     */
    fun isCustomerMessagingAllowed(customerId: Long): Boolean {
        return prefs.getBoolean(PREF_CUSTOMER_ALLOWED_PREFIX + customerId, true)
    }

    fun setCustomerMessagingAllowed(customerId: Long, allowed: Boolean) {
        prefs.edit().putBoolean(PREF_CUSTOMER_ALLOWED_PREFIX + customerId, allowed).apply()
    }
}

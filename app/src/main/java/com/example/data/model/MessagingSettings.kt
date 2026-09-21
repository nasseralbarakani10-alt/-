package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messaging_settings")
data class MessagingSettings(
    @PrimaryKey
    val id: Int = 1,
    val messageType: String = MESSAGE_TYPE_SMS,
    val delaySeconds: Int = 15,
    val autoSendEnabled: Boolean = true,
    val readyMessageTemplate: String = DEFAULT_TEMPLATE,
    val shopPhoneNumber: String = "",
    val stopShopMessaging: Boolean = false,
    val stopCustomerMessagingOnReady: Boolean = false
) {
    companion object {
        const val MESSAGE_TYPE_SMS = "SMS"
        const val MESSAGE_TYPE_WHATSAPP_BUSINESS = "واتساب أعمال"
        const val MESSAGE_TYPE_WHATSAPP = "واتساب"
        const val DEFAULT_TEMPLATE = "الملابس جاهزة للتسليم شكراً لثقتكم بنا♡"
    }
}

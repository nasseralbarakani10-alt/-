package com.example.util

import android.content.Context
import android.os.Build
import android.telephony.SmsManager

object SmsHelper {
    fun buildReadySmsText(customerName: String, customerNumber: String): String {
        return "عميلنا: $customerName / $customerNumber\nالملابس جاهزة للتسليم شكراً لثقتكم بنا♡"
    }

    fun sendReadySms(
        context: Context,
        customerName: String,
        customerNumber: String,
        phoneNumber: String,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        val cleanPhone = phoneNumber.trim()
        if (cleanPhone.isBlank()) {
            onResult(false, "رقم هاتف العميل غير متوفر")
            return
        }

        val messageText = buildReadySmsText(customerName, customerNumber)

        try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val parts = smsManager.divideMessage(messageText)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(cleanPhone, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(cleanPhone, null, messageText, null, null)
            }
            onResult(true, "تم إرسال رسالة SMS للعميل بنجاح")
        } catch (e: SecurityException) {
            onResult(false, "لم يتم منح إذن إرسال الرسائل القصيرة (SMS)")
        } catch (e: Exception) {
            onResult(false, "تعذر إرسال الرسالة: ${e.message ?: "خطأ غير معروف"}")
        }
    }
}

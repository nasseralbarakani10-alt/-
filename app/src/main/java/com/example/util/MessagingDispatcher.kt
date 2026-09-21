package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder

object MessagingDispatcher {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var lastSmsSentTimestamp: Long = 0L

    data class SmsQueueItem(
        val context: Context,
        val phoneNumber: String,
        val messageText: String,
        val delaySeconds: Int,
        val onResult: (success: Boolean, message: String) -> Unit
    )

    private val smsChannel = Channel<SmsQueueItem>(Channel.UNLIMITED)

    init {
        // Sequential consumer ensuring rate limit / delay spacing
        scope.launch {
            for (item in smsChannel) {
                processSmsItem(item)
            }
        }
    }

    private suspend fun processSmsItem(item: SmsQueueItem) {
        val cleanPhone = item.phoneNumber.trim()
        if (cleanPhone.isBlank()) {
            item.onResult(false, "رقم هاتف العميل غير متوفر")
            return
        }

        val now = System.currentTimeMillis()
        val spacingMillis = (item.delaySeconds.coerceAtLeast(1)) * 1000L
        val elapsed = now - lastSmsSentTimestamp

        if (lastSmsSentTimestamp > 0L && elapsed < spacingMillis) {
            val waitMillis = spacingMillis - elapsed
            delay(waitMillis)
        }

        try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                item.context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val parts = smsManager.divideMessage(item.messageText)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(cleanPhone, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(cleanPhone, null, item.messageText, null, null)
            }
            lastSmsSentTimestamp = System.currentTimeMillis()
            item.onResult(true, "تم إرسال رسالة SMS للعميل بنجاح")
        } catch (e: SecurityException) {
            item.onResult(false, "لم يتم منح إذن إرسال الرسائل القصيرة (SMS)")
        } catch (e: Exception) {
            item.onResult(false, "تعذر إرسال الرسالة: ${e.message ?: "خطأ غير معروف"}")
        }
    }

    fun enqueueSms(
        context: Context,
        phoneNumber: String,
        messageText: String,
        delaySeconds: Int,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        val cleanPhone = phoneNumber.trim()
        if (cleanPhone.isBlank()) {
            onResult(false, "رقم هاتف العميل غير متوفر")
            return
        }

        smsChannel.trySend(
            SmsQueueItem(
                context = context.applicationContext,
                phoneNumber = cleanPhone,
                messageText = messageText,
                delaySeconds = delaySeconds,
                onResult = onResult
            )
        )
    }

    fun openWhatsApp(
        context: Context,
        phoneNumber: String,
        messageText: String,
        isBusiness: Boolean,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        val cleanPhone = phoneNumber.filter { it.isDigit() }
        if (cleanPhone.isBlank()) {
            onResult(false, "رقم هاتف العميل غير متوفر")
            return
        }

        val targetPackage = if (isBusiness) "com.whatsapp.w4b" else "com.whatsapp"

        val isInstalled = try {
            context.packageManager.getPackageInfo(targetPackage, 0)
            true
        } catch (e: Exception) {
            false
        }

        if (!isInstalled) {
            onResult(false, "التطبيق غير مثبت على الجهاز")
            return
        }

        try {
            val encodedMessage = URLEncoder.encode(messageText, "UTF-8")
            val url = "https://wa.me/$cleanPhone?text=$encodedMessage"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage(targetPackage)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onResult(true, if (isBusiness) "جاري فتح واتساب أعمال..." else "جاري فتح واتساب...")
        } catch (e: ActivityNotFoundException) {
            onResult(false, "التطبيق غير مثبت على الجهاز")
        } catch (e: Exception) {
            onResult(false, "التطبيق غير مثبت على الجهاز")
        }
    }
}

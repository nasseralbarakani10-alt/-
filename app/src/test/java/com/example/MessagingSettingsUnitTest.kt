package com.example

import com.example.data.model.MessagingSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MessagingSettingsUnitTest {

    @Test
    fun testDefaultMessagingSettings() {
        val settings = MessagingSettings()
        assertEquals(1, settings.id)
        assertEquals(MessagingSettings.MESSAGE_TYPE_SMS, settings.messageType)
        assertEquals(15, settings.delaySeconds)
        assertTrue(settings.autoSendEnabled)
        assertEquals("الملابس جاهزة للتسليم شكراً لثقتكم بنا♡", settings.readyMessageTemplate)
    }

    @Test
    fun testTemplateFormatting() {
        val settings = MessagingSettings(
            readyMessageTemplate = "طلبك جاهز للاستلام"
        )
        val customerName = "أحمد محمد"
        val customerNumber = "104"
        val formatted = "عميلنا: $customerName / $customerNumber\n${settings.readyMessageTemplate}"
        assertEquals("عميلنا: أحمد محمد / 104\nطلبك جاهز للاستلام", formatted)
    }
}

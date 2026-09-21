package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.MessagingSettings
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MessagingViewModel(
    private val repository: AppRepository
) : ViewModel() {

    val settings: StateFlow<MessagingSettings> = repository.messagingSettings
        .map { it ?: MessagingSettings() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = MessagingSettings()
        )

    fun saveSettings(
        messageType: String,
        delaySeconds: Int,
        autoSendEnabled: Boolean,
        readyMessageTemplate: String,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                val updated = MessagingSettings(
                    id = 1,
                    messageType = messageType,
                    delaySeconds = delaySeconds.coerceAtLeast(1),
                    autoSendEnabled = autoSendEnabled,
                    readyMessageTemplate = readyMessageTemplate.trim()
                )
                repository.saveMessagingSettings(updated)
                onSuccess?.invoke()
            } catch (e: Exception) {
                onError?.invoke(e.message ?: "فشل في حفظ إعدادات الرسائل")
            }
        }
    }
}

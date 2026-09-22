package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppSettings
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: AppRepository
) : ViewModel() {

    val appSettings: StateFlow<AppSettings> = repository.appSettings
        .map { it ?: AppSettings() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppSettings()
        )

    fun setSortByFrequencyEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveAppSettings(
                AppSettings(
                    id = 1,
                    sortByFrequencyEnabled = enabled
                )
            )
        }
    }
}

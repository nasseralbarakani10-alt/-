package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.LinkedDevice
import com.example.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DevicesViewModel(
    private val repository: AppRepository
) : ViewModel() {

    // All linked devices from Room Flow
    val allLinkedDevices: StateFlow<List<LinkedDevice>> = repository.allLinkedDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tracks current running device's ID for highlighting in UI
    private val _currentDeviceId = MutableStateFlow<String?>(null)
    val currentDeviceId: StateFlow<String?> = _currentDeviceId.asStateFlow()

    /**
     * On app startup, check if a LinkedDevice row already exists for this device's current ANDROID_ID;
     * if not, insert one automatically with the real device model and current timestamp.
     * Never insert a duplicate for the same device.
     */
    fun registerCurrentDeviceIfNeeded(
        deviceId: String,
        deviceModel: String,
        onComplete: (LinkedDevice) -> Unit = {}
    ) {
        val cleanId = deviceId.trim()
        if (cleanId.isBlank()) return
        _currentDeviceId.value = cleanId

        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getDeviceByDeviceId(cleanId)
            if (existing == null) {
                val cleanModel = if (deviceModel.isNotBlank()) deviceModel.trim() else "جهاز أندرويد"
                val newDevice = LinkedDevice(
                    deviceModel = cleanModel,
                    deviceId = cleanId,
                    linkedAt = System.currentTimeMillis(),
                    lastSyncAt = null,
                    isConnected = true
                )
                val newId = repository.insertLinkedDevice(newDevice)
                val savedDevice = if (newId > 0) newDevice.copy(id = newId) else newDevice
                withContext(Dispatchers.Main) {
                    onComplete(savedDevice)
                }
            } else {
                // If it exists, ensure it's marked connected
                if (!existing.isConnected) {
                    val updated = existing.copy(isConnected = true)
                    repository.updateLinkedDevice(updated)
                    withContext(Dispatchers.Main) {
                        onComplete(updated)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onComplete(existing)
                    }
                }
            }
        }
    }

    /**
     * Delete a device from Room and refresh list
     */
    fun deleteDevice(
        device: LinkedDevice,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteLinkedDevice(device)
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "فشل حذف الجهاز")
                }
            }
        }
    }

    fun deleteDeviceById(
        id: Long,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteLinkedDeviceById(id)
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "فشل حذف الجهاز")
                }
            }
        }
    }

    /**
     * Update device (e.g. sync timestamp or connection status)
     */
    fun updateDevice(
        device: LinkedDevice,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateLinkedDevice(device)
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
}

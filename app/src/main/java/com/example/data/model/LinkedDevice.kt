package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "linked_devices",
    indices = [
        Index(value = ["deviceId"], unique = true)
    ]
)
data class LinkedDevice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deviceModel: String,
    val deviceId: String,
    val linkedAt: Long,
    val lastSyncAt: Long? = null,
    val isConnected: Boolean = true
)

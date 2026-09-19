package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tailors")
data class Tailor(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumber: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

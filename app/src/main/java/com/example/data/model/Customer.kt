package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val customerNumber: String,
    val phoneNumber: String,
    val createdAt: Long = System.currentTimeMillis()
)

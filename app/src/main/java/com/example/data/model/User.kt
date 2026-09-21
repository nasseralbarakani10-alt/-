package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true)
    ]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val salt: String,
    val isAdmin: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val canAccessReportsRecent: Boolean = true,
    val canAccessReportsStatement: Boolean = true,
    val canAccessReportsCustomSearch: Boolean = true,
    val canAccessReportsDaily: Boolean = true,
    val canAccessReportsMonthly: Boolean = true,
    val canAccessReportsYearly: Boolean = true,
    val canAccessCutterReports: Boolean = true,
    val canAccessTailorReports: Boolean = true
)

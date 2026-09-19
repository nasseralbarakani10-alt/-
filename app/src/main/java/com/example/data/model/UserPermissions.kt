package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_permissions",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userId")
    ]
)
data class UserPermissions(
    @PrimaryKey
    val userId: Long,
    val canAccessReports: Boolean = true,
    val canAccessReportsRecent: Boolean = true,
    val canAccessReportsStatement: Boolean = true,
    val canAccessReportsCustomSearch: Boolean = true,
    val canAccessReportsDaily: Boolean = true,
    val canAccessReportsMonthly: Boolean = true,
    val canAccessReportsYearly: Boolean = true,
    val canAccessSettings: Boolean = true,
    val canEdit: Boolean = true,
    val canDelete: Boolean = true,
    val canChangeReadyStatus: Boolean = true
) {
    companion object {
        fun allEnabled(userId: Long) = UserPermissions(
            userId = userId,
            canAccessReports = true,
            canAccessReportsRecent = true,
            canAccessReportsStatement = true,
            canAccessReportsCustomSearch = true,
            canAccessReportsDaily = true,
            canAccessReportsMonthly = true,
            canAccessReportsYearly = true,
            canAccessSettings = true,
            canEdit = true,
            canDelete = true,
            canChangeReadyStatus = true
        )

        fun defaultNonAdmin(userId: Long) = UserPermissions(
            userId = userId,
            canAccessReports = false,
            canAccessReportsRecent = false,
            canAccessReportsStatement = false,
            canAccessReportsCustomSearch = false,
            canAccessReportsDaily = false,
            canAccessReportsMonthly = false,
            canAccessReportsYearly = false,
            canAccessSettings = false,
            canEdit = false,
            canDelete = false,
            canChangeReadyStatus = false
        )
    }
}

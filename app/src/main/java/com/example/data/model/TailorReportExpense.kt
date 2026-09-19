package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tailor_report_expenses",
    foreignKeys = [
        ForeignKey(
            entity = Tailor::class,
            parentColumns = ["id"],
            childColumns = ["tailorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["tailorId", "periodStart", "periodEnd"], unique = true)
    ]
)
data class TailorReportExpense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tailorId: Long,
    val periodStart: Long,
    val periodEnd: Long,
    val expenseAmount: Double,
    val updatedAt: Long = System.currentTimeMillis()
)

package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cutter_report_expenses",
    foreignKeys = [
        ForeignKey(
            entity = Cutter::class,
            parentColumns = ["id"],
            childColumns = ["cutterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["cutterId", "periodStart", "periodEnd"], unique = true)
    ]
)
data class CutterReportExpense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cutterId: Long,
    val periodStart: Long,
    val periodEnd: Long,
    val expenseAmount: Double,
    val updatedAt: Long = System.currentTimeMillis()
)

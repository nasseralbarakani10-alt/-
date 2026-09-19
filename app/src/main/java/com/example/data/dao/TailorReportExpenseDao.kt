package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.TailorReportExpense
import kotlinx.coroutines.flow.Flow

@Dao
interface TailorReportExpenseDao {
    @Query("SELECT * FROM tailor_report_expenses WHERE tailorId = :tailorId AND periodStart = :periodStart AND periodEnd = :periodEnd LIMIT 1")
    fun getExpense(tailorId: Long, periodStart: Long, periodEnd: Long): Flow<TailorReportExpense?>

    @Query("SELECT * FROM tailor_report_expenses WHERE tailorId = :tailorId AND periodStart = :periodStart AND periodEnd = :periodEnd LIMIT 1")
    suspend fun getExpenseDirect(tailorId: Long, periodStart: Long, periodEnd: Long): TailorReportExpense?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExpense(expense: TailorReportExpense): Long
}

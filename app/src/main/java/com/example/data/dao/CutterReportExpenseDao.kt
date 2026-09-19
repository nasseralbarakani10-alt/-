package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CutterReportExpense
import kotlinx.coroutines.flow.Flow

@Dao
interface CutterReportExpenseDao {
    @Query("SELECT * FROM cutter_report_expenses WHERE cutterId = :cutterId AND periodStart = :periodStart AND periodEnd = :periodEnd LIMIT 1")
    fun getExpense(cutterId: Long, periodStart: Long, periodEnd: Long): Flow<CutterReportExpense?>

    @Query("SELECT * FROM cutter_report_expenses WHERE cutterId = :cutterId AND periodStart = :periodStart AND periodEnd = :periodEnd LIMIT 1")
    suspend fun getExpenseDirect(cutterId: Long, periodStart: Long, periodEnd: Long): CutterReportExpense?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExpense(expense: CutterReportExpense): Long
}

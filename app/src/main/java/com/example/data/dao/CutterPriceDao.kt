package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CutterPrice
import kotlinx.coroutines.flow.Flow

@Dao
interface CutterPriceDao {
    @Query("SELECT * FROM cutter_prices WHERE cutterId = :cutterId")
    fun getPricesForCutter(cutterId: Long): Flow<List<CutterPrice>>

    @Query("SELECT * FROM cutter_prices WHERE cutterId = :cutterId")
    suspend fun getPricesListForCutter(cutterId: Long): List<CutterPrice>

    @Query("SELECT * FROM cutter_prices WHERE cutterId = :cutterId AND categoryId = :categoryId")
    fun getPrice(cutterId: Long, categoryId: Long): Flow<CutterPrice?>

    @Query("SELECT * FROM cutter_prices WHERE cutterId = :cutterId AND categoryId = :categoryId LIMIT 1")
    suspend fun getPriceDirect(cutterId: Long, categoryId: Long): CutterPrice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cutterPrice: CutterPrice): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prices: List<CutterPrice>)

    @Update
    suspend fun update(cutterPrice: CutterPrice)

    @Delete
    suspend fun delete(cutterPrice: CutterPrice)
}

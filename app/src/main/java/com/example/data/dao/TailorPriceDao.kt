package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TailorPrice
import kotlinx.coroutines.flow.Flow

@Dao
interface TailorPriceDao {
    @Query("SELECT * FROM tailor_prices WHERE tailorId = :tailorId")
    fun getPricesForTailor(tailorId: Long): Flow<List<TailorPrice>>

    @Query("SELECT * FROM tailor_prices WHERE tailorId = :tailorId")
    suspend fun getPricesListForTailor(tailorId: Long): List<TailorPrice>

    @Query("SELECT * FROM tailor_prices WHERE tailorId = :tailorId AND categoryId = :categoryId")
    fun getPrice(tailorId: Long, categoryId: Long): Flow<TailorPrice?>

    @Query("SELECT * FROM tailor_prices WHERE tailorId = :tailorId AND categoryId = :categoryId LIMIT 1")
    suspend fun getPriceDirect(tailorId: Long, categoryId: Long): TailorPrice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tailorPrice: TailorPrice): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prices: List<TailorPrice>)

    @Update
    suspend fun update(tailorPrice: TailorPrice)

    @Delete
    suspend fun delete(tailorPrice: TailorPrice)
}

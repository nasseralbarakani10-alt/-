package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Order
import com.example.data.model.OrderWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Transaction
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrdersWithCategory(): Flow<List<OrderWithCategory>>

    @Transaction
    @Query("SELECT * FROM orders WHERE createdAt >= :startTime AND createdAt <= :endTime ORDER BY createdAt DESC")
    fun getOrdersWithCategoryBetween(startTime: Long, endTime: Long): Flow<List<OrderWithCategory>>

    @Transaction
    @Query("""
        SELECT * FROM orders 
        WHERE (:startTime IS NULL OR createdAt >= :startTime)
          AND (:endTime IS NULL OR createdAt <= :endTime)
          AND (:cutterId IS NULL OR cutterId = :cutterId)
          AND (:tailorId IS NULL OR tailorId = :tailorId)
          AND (:categoryId IS NULL OR categoryId = :categoryId)
        ORDER BY createdAt DESC
    """)
    fun getFilteredOrders(
        startTime: Long?,
        endTime: Long?,
        cutterId: Long?,
        tailorId: Long?,
        categoryId: Long?
    ): Flow<List<OrderWithCategory>>

    @Query("SELECT COUNT(*) FROM orders")
    fun getOrderCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM orders WHERE cutterId = :cutterId")
    suspend fun getOrderCountForCutter(cutterId: Long): Int

    @Query("SELECT COUNT(*) FROM orders WHERE tailorId = :tailorId")
    suspend fun getOrderCountForTailor(tailorId: Long): Int

    @Query("SELECT COUNT(*) FROM orders WHERE categoryId = :categoryId")
    suspend fun getOrderCountForCategory(categoryId: Long): Int

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY sequenceNumber ASC")
    fun getOrdersForCustomer(customerId: Long): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY sequenceNumber ASC")
    suspend fun getOrdersForCustomerDirect(customerId: Long): List<Order>

    @Query("SELECT * FROM orders WHERE id = :id")
    fun getOrderById(id: Long): Flow<Order?>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderByIdDirect(id: Long): Order?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: Order): Long

    @Update
    suspend fun update(order: Order)

    @Delete
    suspend fun delete(order: Order)

    @Query("DELETE FROM orders WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}

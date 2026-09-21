package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Tailor
import kotlinx.coroutines.flow.Flow

@Dao
interface TailorDao {
    @Query("SELECT * FROM tailors ORDER BY name ASC")
    fun getAllTailors(): Flow<List<Tailor>>

    @Query("SELECT * FROM tailors WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveTailors(): Flow<List<Tailor>>

    @Query("SELECT * FROM tailors WHERE id = :id")
    fun getById(id: Long): Flow<Tailor?>

    @Query("UPDATE tailors SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementUsageCount(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tailor: Tailor): Long

    @Update
    suspend fun update(tailor: Tailor)

    @Delete
    suspend fun delete(tailor: Tailor)
}

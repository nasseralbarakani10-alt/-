package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Cutter
import kotlinx.coroutines.flow.Flow

@Dao
interface CutterDao {
    @Query("SELECT * FROM cutters ORDER BY name ASC")
    fun getAllCutters(): Flow<List<Cutter>>

    @Query("SELECT * FROM cutters WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveCutters(): Flow<List<Cutter>>

    @Query("SELECT * FROM cutters WHERE id = :id")
    fun getById(id: Long): Flow<Cutter?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cutter: Cutter): Long

    @Update
    suspend fun update(cutter: Cutter)

    @Delete
    suspend fun delete(cutter: Cutter)
}

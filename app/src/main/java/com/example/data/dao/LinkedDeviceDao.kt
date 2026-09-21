package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LinkedDevice
import kotlinx.coroutines.flow.Flow

@Dao
interface LinkedDeviceDao {

    @Query("SELECT * FROM linked_devices ORDER BY linkedAt DESC")
    fun getAllLinkedDevices(): Flow<List<LinkedDevice>>

    @Query("SELECT * FROM linked_devices WHERE deviceId = :deviceId LIMIT 1")
    suspend fun getDeviceByDeviceId(deviceId: String): LinkedDevice?

    @Query("SELECT * FROM linked_devices WHERE id = :id LIMIT 1")
    suspend fun getDeviceById(id: Long): LinkedDevice?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(device: LinkedDevice): Long

    @Update
    suspend fun update(device: LinkedDevice)

    @Delete
    suspend fun delete(device: LinkedDevice)

    @Query("DELETE FROM linked_devices WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM linked_devices")
    suspend fun getDeviceCount(): Int
}

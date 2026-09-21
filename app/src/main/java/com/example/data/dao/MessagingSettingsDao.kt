package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.MessagingSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface MessagingSettingsDao {

    @Query("SELECT * FROM messaging_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<MessagingSettings?>

    @Query("SELECT * FROM messaging_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): MessagingSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: MessagingSettings)
}

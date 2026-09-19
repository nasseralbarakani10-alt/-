package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserPermissions
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPermissionsDao {

    @Query("SELECT * FROM user_permissions WHERE userId = :userId")
    suspend fun getPermissionsForUser(userId: Long): UserPermissions?

    @Query("SELECT * FROM user_permissions WHERE userId = :userId")
    fun getPermissionsForUserFlow(userId: Long): Flow<UserPermissions?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(permissions: UserPermissions): Long

    @Update
    suspend fun update(permissions: UserPermissions)

    @Delete
    suspend fun delete(permissions: UserPermissions)
}

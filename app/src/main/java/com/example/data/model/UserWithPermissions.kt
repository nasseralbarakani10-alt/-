package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class UserWithPermissions(
    @Embedded
    val user: User,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val permissions: UserPermissions?
)

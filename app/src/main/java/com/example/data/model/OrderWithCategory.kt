package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class OrderWithCategory(
    @Embedded val order: Order,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: Category? = null,
    @Relation(
        parentColumn = "cutterId",
        entityColumn = "id"
    )
    val cutter: Cutter? = null,
    @Relation(
        parentColumn = "tailorId",
        entityColumn = "id"
    )
    val tailor: Tailor? = null
)

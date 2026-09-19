package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tailor_prices",
    foreignKeys = [
        ForeignKey(
            entity = Tailor::class,
            parentColumns = ["id"],
            childColumns = ["tailorId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["tailorId", "categoryId"], unique = true),
        Index("tailorId"),
        Index("categoryId")
    ]
)
data class TailorPrice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tailorId: Long,
    val categoryId: Long,
    val price: Double
)

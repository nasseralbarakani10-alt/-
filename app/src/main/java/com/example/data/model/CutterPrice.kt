package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cutter_prices",
    foreignKeys = [
        ForeignKey(
            entity = Cutter::class,
            parentColumns = ["id"],
            childColumns = ["cutterId"],
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
        Index(value = ["cutterId", "categoryId"], unique = true),
        Index("cutterId"),
        Index("categoryId")
    ]
)
data class CutterPrice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cutterId: Long,
    val categoryId: Long,
    val price: Double
)

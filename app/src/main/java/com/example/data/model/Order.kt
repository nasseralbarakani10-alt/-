package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Cutter::class,
            parentColumns = ["id"],
            childColumns = ["cutterId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Tailor::class,
            parentColumns = ["id"],
            childColumns = ["tailorId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("customerId"),
        Index("categoryId"),
        Index("cutterId"),
        Index("tailorId")
    ]
)
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val sequenceNumber: Int = 1,
    val categoryId: Long,
    val fabricType: String,
    val cutterId: Long? = null,
    val tailorId: Long? = null,
    val cutterPrice: Double = 0.0,
    val tailorPrice: Double = 0.0,
    val buttonIroning: Boolean = false,
    val laundry: Boolean = false,
    val ready: Boolean = false,
    val readyLockedByAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

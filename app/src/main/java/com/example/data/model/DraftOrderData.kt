package com.example.data.model

data class DraftOrderData(
    val categoryId: Long,
    val fabricType: String,
    val cutterId: Long? = null,
    val tailorId: Long? = null,
    val cutterPrice: Double = 0.0,
    val tailorPrice: Double = 0.0,
    val buttonIroning: Boolean = false,
    val laundry: Boolean = false,
    // Optional display names for UI convenience
    val categoryName: String = "",
    val cutterName: String = "",
    val tailorName: String = ""
)

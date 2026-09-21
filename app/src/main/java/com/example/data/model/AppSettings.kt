package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1,
    val sortByFrequencyEnabled: Boolean = true,
    val appTitle: String = DEFAULT_APP_TITLE
) {
    companion object {
        const val DEFAULT_APP_TITLE = "كشف متابعة العمل لمحل ترند"
    }
}

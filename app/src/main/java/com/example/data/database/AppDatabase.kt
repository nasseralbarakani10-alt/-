package com.example.data.database

import android.content.ContentValues
import android.content.Context
import androidx.room.Database
import androidx.room.OnConflictStrategy
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AppSettingsDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.CutterDao
import com.example.data.dao.CutterPriceDao
import com.example.data.dao.CutterReportExpenseDao
import com.example.data.dao.LinkedDeviceDao
import com.example.data.dao.MessagingSettingsDao
import com.example.data.dao.OrderDao
import com.example.data.dao.TailorDao
import com.example.data.dao.TailorPriceDao
import com.example.data.dao.TailorReportExpenseDao
import com.example.data.dao.UserDao
import com.example.data.dao.UserPermissionsDao
import com.example.data.model.AppSettings
import com.example.data.model.Category
import com.example.data.model.Cutter
import com.example.data.model.CutterPrice
import com.example.data.model.CutterReportExpense
import com.example.data.model.LinkedDevice
import com.example.data.model.MessagingSettings
import com.example.data.model.Order
import com.example.data.model.Tailor
import com.example.data.model.TailorPrice
import com.example.data.model.TailorReportExpense
import com.example.data.model.User
import com.example.data.model.UserPermissions
import com.example.data.security.PasswordHasher

@Database(
    entities = [
        Order::class,
        Category::class,
        Cutter::class,
        Tailor::class,
        CutterPrice::class,
        TailorPrice::class,
        User::class,
        UserPermissions::class,
        CutterReportExpense::class,
        TailorReportExpense::class,
        LinkedDevice::class,
        MessagingSettings::class,
        AppSettings::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
    abstract fun categoryDao(): CategoryDao
    abstract fun cutterDao(): CutterDao
    abstract fun tailorDao(): TailorDao
    abstract fun cutterPriceDao(): CutterPriceDao
    abstract fun tailorPriceDao(): TailorPriceDao
    abstract fun cutterReportExpenseDao(): CutterReportExpenseDao
    abstract fun tailorReportExpenseDao(): TailorReportExpenseDao
    abstract fun userDao(): UserDao
    abstract fun userPermissionsDao(): UserPermissionsDao
    abstract fun linkedDeviceDao(): LinkedDeviceDao
    abstract fun messagingSettingsDao(): MessagingSettingsDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to users table
                db.execSQL("ALTER TABLE users ADD COLUMN canAccessReportsRecent INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE users ADD COLUMN canAccessReportsStatement INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE users ADD COLUMN canAccessReportsCustomSearch INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE users ADD COLUMN canAccessReportsDaily INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE users ADD COLUMN canAccessReportsMonthly INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE users ADD COLUMN canAccessReportsYearly INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE users ADD COLUMN canAccessCutterReports INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE users ADD COLUMN canAccessTailorReports INTEGER NOT NULL DEFAULT 1")

                // Add new columns to user_permissions table
                db.execSQL("ALTER TABLE user_permissions ADD COLUMN canAccessCutterReports INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_permissions ADD COLUMN canAccessTailorReports INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS messaging_settings (
                        id INTEGER PRIMARY KEY NOT NULL,
                        messageType TEXT NOT NULL,
                        delaySeconds INTEGER NOT NULL,
                        autoSendEnabled INTEGER NOT NULL,
                        readyMessageTemplate TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT OR IGNORE INTO messaging_settings (id, messageType, delaySeconds, autoSendEnabled, readyMessageTemplate)
                    VALUES (1, 'SMS', 15, 1, 'الملابس جاهزة للتسليم شكراً لثقتكم بنا♡')
                """.trimIndent())
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add usageCount to categories, cutters, tailors
                db.execSQL("ALTER TABLE categories ADD COLUMN usageCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE cutters ADD COLUMN usageCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tailors ADD COLUMN usageCount INTEGER NOT NULL DEFAULT 0")

                // Create app_settings table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS app_settings (
                        id INTEGER PRIMARY KEY NOT NULL,
                        sortByFrequencyEnabled INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT OR IGNORE INTO app_settings (id, sortByFrequencyEnabled)
                    VALUES (1, 1)
                """.trimIndent())
            }
        }

        @Volatile
        private var restoredCategories: List<Pair<String, Int>>? = null
        @Volatile
        private var restoredCutters: List<Pair<String, String>>? = null
        @Volatile
        private var restoredTailors: List<Pair<String, String>>? = null

        private fun sanitizeDatabaseIfNeeded(context: Context) {
            try {
                val dbFile = context.getDatabasePath("trend_tailoring_db")
                if (!dbFile.exists()) return

                var needsReset = false
                var extractedCategories: MutableList<Pair<String, Int>>? = null
                var extractedCutters: MutableList<Pair<String, String>>? = null
                var extractedTailors: MutableList<Pair<String, String>>? = null

                try {
                    android.database.sqlite.SQLiteDatabase.openDatabase(
                        dbFile.path,
                        null,
                        android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                    ).use { db ->
                        // 1. Check if room_master_table exists
                        val masterCursor = db.rawQuery(
                            "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='room_master_table'",
                            null
                        )
                        val hasMasterTable = masterCursor.use {
                            if (it.moveToFirst()) it.getInt(0) > 0 else false
                        }

                        // 2. Check if orders table exists and has valid columns
                        val ordersCursor = db.rawQuery(
                            "PRAGMA table_info('orders')",
                            null
                        )
                        val ordersColumnCount = ordersCursor.use { it.count }

                        if (!hasMasterTable || ordersColumnCount == 0) {
                            needsReset = true

                            // Try to extract existing categories if table exists
                            try {
                                val catCursor = db.rawQuery("SELECT name, sortOrder FROM categories", null)
                                catCursor.use {
                                    val list = mutableListOf<Pair<String, Int>>()
                                    while (it.moveToNext()) {
                                        val name = it.getString(0)
                                        val sort = it.getInt(1)
                                        list.add(name to sort)
                                    }
                                    if (list.isNotEmpty()) extractedCategories = list
                                }
                            } catch (_: Exception) {}

                            // Try to extract existing cutters if table exists
                            try {
                                val cCursor = db.rawQuery("SELECT name, phoneNumber FROM cutters", null)
                                cCursor.use {
                                    val list = mutableListOf<Pair<String, String>>()
                                    while (it.moveToNext()) {
                                        list.add(it.getString(0) to it.getString(1))
                                    }
                                    if (list.isNotEmpty()) extractedCutters = list
                                }
                            } catch (_: Exception) {}

                            // Try to extract existing tailors if table exists
                            try {
                                val tCursor = db.rawQuery("SELECT name, phoneNumber FROM tailors", null)
                                tCursor.use {
                                    val list = mutableListOf<Pair<String, String>>()
                                    while (it.moveToNext()) {
                                        list.add(it.getString(0) to it.getString(1))
                                    }
                                    if (list.isNotEmpty()) extractedTailors = list
                                }
                            } catch (_: Exception) {}
                        }
                    }
                } catch (e: Exception) {
                    needsReset = true
                }

                if (needsReset) {
                    restoredCategories = extractedCategories
                    restoredCutters = extractedCutters
                    restoredTailors = extractedTailors
                    context.deleteDatabase("trend_tailoring_db")
                    try {
                        if (dbFile.exists()) {
                            dbFile.delete()
                        }
                    } catch (_: Exception) {}
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: run {
                    sanitizeDatabaseIfNeeded(context.applicationContext)
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "trend_tailoring_db"
                    )
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .fallbackToDestructiveMigration()
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            seedCategoriesIfEmpty(db)
                            seedUsersIfEmpty(db)
                            seedMessagingSettingsIfEmpty(db)
                            seedAppSettingsIfEmpty(db)
                            restoreExtractedDataIfAny(db)
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            seedCategoriesIfEmpty(db)
                            seedUsersIfEmpty(db)
                            seedMessagingSettingsIfEmpty(db)
                            seedAppSettingsIfEmpty(db)
                            restoreExtractedDataIfAny(db)
                        }

                        private fun restoreExtractedDataIfAny(db: SupportSQLiteDatabase) {
                            try {
                                restoredCutters?.let { cutters ->
                                    cutters.forEach { (name, phone) ->
                                        val cv = ContentValues().apply {
                                            put("name", name)
                                            put("phoneNumber", phone)
                                            put("isActive", 1)
                                            put("createdAt", System.currentTimeMillis())
                                            put("usageCount", 0)
                                        }
                                        db.insert("cutters", OnConflictStrategy.IGNORE, cv)
                                    }
                                    restoredCutters = null
                                }
                                restoredTailors?.let { tailors ->
                                    tailors.forEach { (name, phone) ->
                                        val cv = ContentValues().apply {
                                            put("name", name)
                                            put("phoneNumber", phone)
                                            put("isActive", 1)
                                            put("createdAt", System.currentTimeMillis())
                                            put("usageCount", 0)
                                        }
                                        db.insert("tailors", OnConflictStrategy.IGNORE, cv)
                                    }
                                    restoredTailors = null
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                    private fun seedAppSettingsIfEmpty(db: SupportSQLiteDatabase) {
                        try {
                            db.execSQL("""
                                INSERT OR IGNORE INTO app_settings (id, sortByFrequencyEnabled)
                                VALUES (1, 1)
                            """.trimIndent())
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    private fun seedMessagingSettingsIfEmpty(db: SupportSQLiteDatabase) {
                        try {
                            db.execSQL("""
                                INSERT OR IGNORE INTO messaging_settings (id, messageType, delaySeconds, autoSendEnabled, readyMessageTemplate)
                                VALUES (1, 'SMS', 15, 1, 'الملابس جاهزة للتسليم شكراً لثقتكم بنا♡')
                            """.trimIndent())
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    private fun seedCategoriesIfEmpty(db: SupportSQLiteDatabase) {
                        try {
                            val cursor = db.query("SELECT COUNT(*) FROM categories")
                            var count = 0
                            if (cursor.moveToFirst()) {
                                count = cursor.getInt(0)
                            }
                            cursor.close()

                            if (count == 0) {
                                val defaultCategories = restoredCategories?.map { it.first } ?: listOf(
                                    "قطري", "سعودي", "إماراتي", "كويتي", "عماني",
                                    "كوت", "شميز", "بنطلون", "دجلة", "يلق",
                                    "صدرية", "لاب كوت", "سفاري", "سكراب", "موديل"
                                )
                                restoredCategories = null
                                defaultCategories.forEachIndexed { index, name ->
                                    val values = ContentValues().apply {
                                        put("name", name)
                                        put("sortOrder", index + 1)
                                        put("isDefault", 1)
                                        put("usageCount", 0)
                                    }
                                    db.insert("categories", OnConflictStrategy.IGNORE, values)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    private fun seedUsersIfEmpty(db: SupportSQLiteDatabase) {
                        try {
                            val cursor = db.query("SELECT COUNT(*) FROM users")
                            var count = 0
                            if (cursor.moveToFirst()) {
                                count = cursor.getInt(0)
                            }
                            cursor.close()

                            if (count == 0) {
                                val salt = PasswordHasher.generateSalt()
                                val passwordHash = PasswordHasher.hashPassword("0000", salt)
                                val now = System.currentTimeMillis()

                                db.beginTransaction()
                                try {
                                    val userValues = ContentValues().apply {
                                        put("username", "1")
                                        put("passwordHash", passwordHash)
                                        put("salt", salt)
                                        put("isAdmin", 1)
                                        put("isActive", 1)
                                        put("createdAt", now)
                                        put("canAccessReportsRecent", 1)
                                        put("canAccessReportsStatement", 1)
                                        put("canAccessReportsCustomSearch", 1)
                                        put("canAccessReportsDaily", 1)
                                        put("canAccessReportsMonthly", 1)
                                        put("canAccessReportsYearly", 1)
                                        put("canAccessCutterReports", 1)
                                        put("canAccessTailorReports", 1)
                                    }
                                    val userId = db.insert("users", OnConflictStrategy.IGNORE, userValues)
                                    if (userId > 0) {
                                        val permissionsValues = ContentValues().apply {
                                            put("userId", userId)
                                            put("canAccessReports", 1)
                                            put("canAccessReportsRecent", 1)
                                            put("canAccessReportsStatement", 1)
                                            put("canAccessReportsCustomSearch", 1)
                                            put("canAccessReportsDaily", 1)
                                            put("canAccessReportsMonthly", 1)
                                            put("canAccessReportsYearly", 1)
                                            put("canAccessCutterReports", 1)
                                            put("canAccessTailorReports", 1)
                                            put("canAccessSettings", 1)
                                            put("canEdit", 1)
                                            put("canDelete", 1)
                                            put("canChangeReadyStatus", 1)
                                        }
                                        db.insert("user_permissions", OnConflictStrategy.IGNORE, permissionsValues)
                                    }
                                    db.setTransactionSuccessful()
                                } finally {
                                    db.endTransaction()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
}

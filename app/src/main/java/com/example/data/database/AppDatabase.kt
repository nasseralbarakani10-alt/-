package com.example.data.database

import android.content.ContentValues
import android.content.Context
import androidx.room.Database
import androidx.room.OnConflictStrategy
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.CategoryDao
import com.example.data.dao.CutterDao
import com.example.data.dao.CutterPriceDao
import com.example.data.dao.CutterReportExpenseDao
import com.example.data.dao.OrderDao
import com.example.data.dao.TailorDao
import com.example.data.dao.TailorPriceDao
import com.example.data.dao.TailorReportExpenseDao
import com.example.data.dao.UserDao
import com.example.data.dao.UserPermissionsDao
import com.example.data.model.Category
import com.example.data.model.Cutter
import com.example.data.model.CutterPrice
import com.example.data.model.CutterReportExpense
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
        TailorReportExpense::class
    ],
    version = 3,
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

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "trend_tailoring_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        seedCategoriesIfEmpty(db)
                        seedUsersIfEmpty(db)
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        seedCategoriesIfEmpty(db)
                        seedUsersIfEmpty(db)
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
                                val defaultCategories = listOf(
                                    "قطري", "سعودي", "إماراتي", "كويتي", "عماني",
                                    "كوت", "شميز", "بنطلون", "دجلة", "يلق",
                                    "صدرية", "لاب كوت", "سفاري", "سكراب", "موديل"
                                )
                                db.beginTransaction()
                                try {
                                    defaultCategories.forEachIndexed { index, name ->
                                        val values = ContentValues().apply {
                                            put("name", name)
                                            put("sortOrder", index + 1)
                                            put("isDefault", 1)
                                        }
                                        db.insert("categories", OnConflictStrategy.IGNORE, values)
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

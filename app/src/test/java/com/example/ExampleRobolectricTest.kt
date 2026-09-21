package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("متابعة العمل", appName)
  }

  @Test
  fun `database recovers from invalid schema or missing tables without crash`() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // Simulate an invalid database file on disk with missing orders and missing room_master_table
    val dbFile = context.getDatabasePath("trend_tailoring_db")
    dbFile.parentFile?.mkdirs()
    android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(dbFile, null).use { sqlite ->
      sqlite.execSQL("CREATE TABLE dummy (id INTEGER PRIMARY KEY)")
    }

    // Attempt to get AppDatabase - verify it recovers and queries cleanly
    val db = AppDatabase.getDatabase(context)
    val count = runBlocking { db.orderDao().getOrderCount().first() }
    assertEquals(0, count)
  }
}

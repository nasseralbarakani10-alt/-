package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.Category
import com.example.data.model.Cutter
import com.example.data.model.Order
import com.example.data.model.Tailor
import com.example.data.model.User
import com.example.data.model.UserPermissions
import com.example.data.repository.AppRepository
import com.example.data.session.SessionManager
import com.example.ui.screens.reports.getEndOfDay
import com.example.ui.screens.reports.getStartOfDay
import com.example.ui.viewmodel.OrdersViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ReportsRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: AppRepository
    private lateinit var ordersViewModel: OrdersViewModel

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AppRepository(
            orderDao = db.orderDao(),
            categoryDao = db.categoryDao(),
            cutterDao = db.cutterDao(),
            tailorDao = db.tailorDao(),
            cutterPriceDao = db.cutterPriceDao(),
            tailorPriceDao = db.tailorPriceDao(),
            userDao = db.userDao(),
            userPermissionsDao = db.userPermissionsDao(),
            cutterReportExpenseDao = db.cutterReportExpenseDao(),
            tailorReportExpenseDao = db.tailorReportExpenseDao()
        )
        val sessionManager = SessionManager(context, repository, CoroutineScope(Dispatchers.Unconfined))
        runBlocking {
            val adminId = repository.insertUser(
                User(
                    username = "admin",
                    passwordHash = "hash",
                    salt = "salt",
                    isAdmin = true
                )
            )
            val adminUser = repository.getUserById(adminId)!!
            val adminPerms = UserPermissions.allEnabled(adminId)
            repository.insertOrUpdatePermissions(adminPerms)
            sessionManager.setSession(adminUser, adminPerms)
        }
        ordersViewModel = OrdersViewModel(repository, sessionManager)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testDailyAndDateRangeQueries() = runBlocking {
        val catId = repository.insertCategory(Category(name = "قطري", isDefault = true))
        val cutterId = repository.insertCutter(Cutter(name = "أحمد القصاص"))
        val tailorId = repository.insertTailor(Tailor(name = "محمد الخياط"))

        val today = Calendar.getInstance()
        val todayStart = getStartOfDay(today)
        val todayEnd = getEndOfDay(today)

        val yesterday = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -1) }
        val yesterdayTime = yesterday.timeInMillis

        // Order created today
        repository.insertOrder(
            Order(
                customerName = "خالد اليوم",
                customerNumber = "101",
                phoneNumber = "0501234567",
                categoryId = catId,
                cutterId = cutterId,
                tailorId = tailorId,
                fabricType = "صوف",
                createdAt = today.timeInMillis
            )
        )

        // Order created yesterday
        repository.insertOrder(
            Order(
                customerName = "سالم الأمس",
                customerNumber = "102",
                phoneNumber = "0509876543",
                categoryId = catId,
                cutterId = cutterId,
                tailorId = tailorId,
                fabricType = "قطن",
                createdAt = yesterdayTime
            )
        )

        // Query today's orders
        val todayOrders = ordersViewModel.getOrdersBetween(todayStart, todayEnd).first()
        assertEquals(1, todayOrders.size)
        assertEquals("خالد اليوم", todayOrders[0].order.customerName)

        // Query yesterday to today range
        val rangeOrders = ordersViewModel.getOrdersBetween(getStartOfDay(yesterday), todayEnd).first()
        assertEquals(2, rangeOrders.size)

        // Filtered query by cutter and tailor
        val filtered = ordersViewModel.getFilteredOrders(
            startTime = getStartOfDay(yesterday),
            endTime = todayEnd,
            cutterId = cutterId,
            tailorId = tailorId,
            categoryId = catId
        ).first()
        assertEquals(2, filtered.size)
    }

    @Test
    fun testCutterAndTailorReportExpensesPersistence() = runBlocking {
        val cutterId = repository.insertCutter(Cutter(name = "سامي القصاص"))
        val tailorId = repository.insertTailor(Tailor(name = "عمر الخياط"))

        val startPeriod = 1700000000000L
        val endPeriod = 1700086400000L

        // Initially no expense saved
        val initialCutterExpense = repository.getCutterReportExpenseDirect(cutterId, startPeriod, endPeriod)
        assertEquals(null, initialCutterExpense)

        // Save cutter expense
        repository.saveCutterReportExpense(cutterId, startPeriod, endPeriod, 1500.0)
        val savedCutterExpense = repository.getCutterReportExpenseDirect(cutterId, startPeriod, endPeriod)
        assertEquals(1500.0, savedCutterExpense?.expenseAmount ?: 0.0, 0.001)

        // Update cutter expense (upsert)
        repository.saveCutterReportExpense(cutterId, startPeriod, endPeriod, 2000.0)
        val updatedCutterExpense = repository.getCutterReportExpenseDirect(cutterId, startPeriod, endPeriod)
        assertEquals(2000.0, updatedCutterExpense?.expenseAmount ?: 0.0, 0.001)

        // Save tailor expense
        repository.saveTailorReportExpense(tailorId, startPeriod, endPeriod, 3200.0)
        val savedTailorExpense = repository.getTailorReportExpenseDirect(tailorId, startPeriod, endPeriod)
        assertEquals(3200.0, savedTailorExpense?.expenseAmount ?: 0.0, 0.001)
    }
}

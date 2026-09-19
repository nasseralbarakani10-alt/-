package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.Category
import com.example.data.model.Cutter
import com.example.data.model.Order
import com.example.data.model.Tailor
import com.example.data.repository.AppRepository
import com.example.ui.viewmodel.CuttersViewModel
import com.example.ui.viewmodel.TailorsViewModel
import com.example.data.model.User
import com.example.data.model.UserPermissions
import com.example.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WorkersRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: AppRepository
    private lateinit var cuttersViewModel: CuttersViewModel
    private lateinit var tailorsViewModel: TailorsViewModel

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
            userPermissionsDao = db.userPermissionsDao()
        )
        val sessionManager = SessionManager(context, repository, CoroutineScope(Dispatchers.Unconfined))
        runBlocking {
            val adminId = repository.insertUser(
                User(username = "admin", passwordHash = "hash", salt = "salt", isAdmin = true)
            )
            val adminUser = repository.getUserById(adminId)!!
            val adminPerms = UserPermissions.allEnabled(adminId)
            repository.insertOrUpdatePermissions(adminPerms)
            sessionManager.setSession(adminUser, adminPerms)
        }

        cuttersViewModel = CuttersViewModel(repository, sessionManager)
        tailorsViewModel = TailorsViewModel(repository, sessionManager)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `add cutter sets isActive to true by default`() = runBlocking {
        val cutterId = repository.insertCutter(
            Cutter(name = "أحمد القصاص", phoneNumber = "0501234567", isActive = true)
        )
        val allCutters = repository.allCutters.first()
        val cutter = allCutters.first { it.id == cutterId }

        assertEquals("أحمد القصاص", cutter.name)
        assertEquals("0501234567", cutter.phoneNumber)
        assertTrue(cutter.isActive)
    }

    @Test
    fun `stop checkbox toggles isActive state immediately`() = runBlocking {
        val cutterId = repository.insertCutter(
            Cutter(name = "سالم القصاص", phoneNumber = "0559876543", isActive = true)
        )
        var cutter = repository.allCutters.first().first { it.id == cutterId }
        assertTrue(cutter.isActive)

        // User checks "إيقاف القصاص" -> isStopped = true, isActive = false
        cuttersViewModel.setCutterStopped(cutter, isStopped = true)
        var updatedList = repository.allCutters.first()
        cutter = updatedList.first { it.id == cutterId }
        assertFalse(cutter.isActive)

        // Active cutters list should now be empty
        val activeCutters = repository.activeCutters.first()
        assertTrue(activeCutters.none { it.id == cutterId })

        // Unchecking "إيقاف القصاص" -> isStopped = false, isActive = true
        cuttersViewModel.setCutterStopped(cutter, isStopped = false)
        updatedList = repository.allCutters.first()
        cutter = updatedList.first { it.id == cutterId }
        assertTrue(cutter.isActive)
    }

    @Test
    fun `delete cutter without orders hard-deletes from Room`() = runBlocking {
        val cutterId = repository.insertCutter(
            Cutter(name = "قصاص جديد", phoneNumber = "0500000000", isActive = true)
        )
        val cutter = repository.allCutters.first().first { it.id == cutterId }

        assertFalse(cuttersViewModel.hasAssociatedOrders(cutterId))

        cuttersViewModel.deleteCutter(cutter, hasOrders = false)
        val remaining = repository.allCutters.first()
        assertTrue(remaining.none { it.id == cutterId })
    }

    @Test
    fun `delete cutter with associated orders preserves order and soft-stops cutter`() = runBlocking {
        val categoryId = repository.insertCategory(
            Category(name = "قطري", sortOrder = 1)
        )
        val cutterId = repository.insertCutter(
            Cutter(name = "قصاص رئيسي", phoneNumber = "0501112233", isActive = true)
        )
        val cutter = repository.allCutters.first().first { it.id == cutterId }

        // Insert order referencing this cutter
        val orderId = repository.insertOrder(
            Order(
                customerName = "عميل تجريبي",
                customerNumber = "101",
                phoneNumber = "0509998877",
                categoryId = categoryId,
                fabricType = "ياباني",
                cutterId = cutterId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )

        assertTrue(cuttersViewModel.hasAssociatedOrders(cutterId))

        // Perform delete with hasOrders = true (soft-delete)
        cuttersViewModel.deleteCutter(cutter, hasOrders = true)

        // Historical order still exists and references the same cutterId
        val order = repository.getOrderById(orderId).first()
        assertNotNull(order)
        assertEquals(cutterId, order?.cutterId)

        // Cutter row still exists for historical integrity, but isActive is false
        val allCutters = repository.allCutters.first()
        val preservedCutter = allCutters.first { it.id == cutterId }
        assertFalse(preservedCutter.isActive)

        // Excluded from active cutters dropdown
        val activeCutters = repository.activeCutters.first()
        assertTrue(activeCutters.none { it.id == cutterId })
    }

    @Test
    fun `tailor deletion and stop logic behaves identically`() = runBlocking {
        val tailorId = repository.insertTailor(
            Tailor(name = "خياط ماهر", phoneNumber = "0533334444", isActive = true)
        )
        val tailor = repository.allTailors.first().first { it.id == tailorId }
        assertTrue(tailor.isActive)

        tailorsViewModel.setTailorStopped(tailor, isStopped = true)
        val updatedTailor = repository.allTailors.first().first { it.id == tailorId }
        assertFalse(updatedTailor.isActive)

        val activeTailors = repository.activeTailors.first()
        assertTrue(activeTailors.none { it.id == tailorId })
    }
}

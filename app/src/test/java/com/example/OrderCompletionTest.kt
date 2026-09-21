package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.Category
import com.example.data.model.Customer
import com.example.data.model.Order
import com.example.data.repository.AppRepository
import com.example.data.session.SessionManager
import com.example.ui.viewmodel.OrdersViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
@Config(sdk = [36])
class OrderCompletionTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: AppRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: OrdersViewModel
    private var categoryId: Long = 1

    @Before
    fun setup() = runBlocking {
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
            tailorReportExpenseDao = db.tailorReportExpenseDao(),
            linkedDeviceDao = db.linkedDeviceDao(),
            messagingSettingsDao = db.messagingSettingsDao(),
            appSettingsDao = db.appSettingsDao(),
            customerDao = db.customerDao()
        )
        sessionManager = SessionManager(
            context = context,
            repository = repository,
            scope = CoroutineScope(Dispatchers.Unconfined)
        )
        viewModel = OrdersViewModel(repository, sessionManager)
        categoryId = db.categoryDao().insert(Category(name = "ثوب"))
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `single order - does not notify when not ready, notifies when ready`() = runBlocking {
        val customerId = repository.insertCustomer(
            Customer(name = "عميل 1", customerNumber = "101", phoneNumber = "0501111111")
        )
        val order1 = Order(
            customerId = customerId,
            sequenceNumber = 1,
            categoryId = categoryId,
            fabricType = "قطن",
            ready = false
        )
        val orderId = repository.insertOrder(order1)

        val notReadyResult = viewModel.checkCustomerOrderCompletionDirect(customerId)
        assertNull("Should not trigger when order is not ready", notReadyResult)

        // Mark ready
        repository.updateOrder(order1.copy(id = orderId, ready = true))
        val readyResult = viewModel.checkCustomerOrderCompletionDirect(customerId)
        assertNotNull("Should trigger when single order becomes ready", readyResult)
        assertEquals(1, readyResult?.second?.size)
        assertTrue(viewModel.isCustomerCompletionNotified(customerId))

        // Calling again should not trigger duplicate
        val duplicateResult = viewModel.checkCustomerOrderCompletionDirect(customerId)
        assertNull("Should not trigger duplicate for same completed set", duplicateResult)
    }

    @Test
    fun `two orders - does not notify when only one is ready, notifies when all ready`() = runBlocking {
        val customerId = repository.insertCustomer(
            Customer(name = "عميل 2", customerNumber = "102", phoneNumber = "0502222222")
        )
        val order1Id = repository.insertOrder(
            Order(customerId = customerId, sequenceNumber = 1, categoryId = categoryId, fabricType = "حرير", ready = false)
        )
        val order2Id = repository.insertOrder(
            Order(customerId = customerId, sequenceNumber = 2, categoryId = categoryId, fabricType = "كتان", ready = false)
        )

        // Only first order is marked ready
        repository.updateOrder(Order(id = order1Id, customerId = customerId, sequenceNumber = 1, categoryId = categoryId, fabricType = "حرير", ready = true))
        val partialResult = viewModel.checkCustomerOrderCompletionDirect(customerId)
        assertNull("Should NOT trigger when only 1 of 2 orders is ready", partialResult)

        // Second order is now marked ready
        repository.updateOrder(Order(id = order2Id, customerId = customerId, sequenceNumber = 2, categoryId = categoryId, fabricType = "كتان", ready = true))
        val completedResult = viewModel.checkCustomerOrderCompletionDirect(customerId)
        assertNotNull("Should trigger when all 2 orders are ready", completedResult)
        assertEquals("عميل 2", completedResult?.first?.name)
        assertEquals(2, completedResult?.second?.size)

        // Second check should not duplicate
        val duplicateResult = viewModel.checkCustomerOrderCompletionDirect(customerId)
        assertNull("Should not trigger duplicate", duplicateResult)
    }

    @Test
    fun `three orders - only notifies when third order becomes ready`() = runBlocking {
        val customerId = repository.insertCustomer(
            Customer(name = "عميل 3", customerNumber = "103", phoneNumber = "0503333333")
        )
        val o1 = repository.insertOrder(Order(customerId = customerId, sequenceNumber = 1, categoryId = categoryId, fabricType = "نوع 1", ready = false))
        val o2 = repository.insertOrder(Order(customerId = customerId, sequenceNumber = 2, categoryId = categoryId, fabricType = "نوع 2", ready = false))
        val o3 = repository.insertOrder(Order(customerId = customerId, sequenceNumber = 3, categoryId = categoryId, fabricType = "نوع 3", ready = false))

        // 1 of 3 ready
        repository.updateOrder(Order(id = o1, customerId = customerId, sequenceNumber = 1, categoryId = categoryId, fabricType = "نوع 1", ready = true))
        assertNull(viewModel.checkCustomerOrderCompletionDirect(customerId))

        // 2 of 3 ready
        repository.updateOrder(Order(id = o2, customerId = customerId, sequenceNumber = 2, categoryId = categoryId, fabricType = "نوع 2", ready = true))
        assertNull(viewModel.checkCustomerOrderCompletionDirect(customerId))

        // 3 of 3 ready
        repository.updateOrder(Order(id = o3, customerId = customerId, sequenceNumber = 3, categoryId = categoryId, fabricType = "نوع 3", ready = true))
        val result = viewModel.checkCustomerOrderCompletionDirect(customerId)
        assertNotNull("Should trigger when 3 of 3 orders are ready", result)
        assertEquals(3, result?.second?.size)
    }

    @Test
    fun `unmarking ready resets notification so it can notify on future re-completion`() = runBlocking {
        val customerId = repository.insertCustomer(
            Customer(name = "عميل 4", customerNumber = "104", phoneNumber = "0504444444")
        )
        val o1 = repository.insertOrder(Order(customerId = customerId, sequenceNumber = 1, categoryId = categoryId, fabricType = "صوف", ready = true))

        val firstResult = viewModel.checkCustomerOrderCompletionDirect(customerId)
        assertNotNull(firstResult)
        assertTrue(viewModel.isCustomerCompletionNotified(customerId))

        // Unmarking order
        repository.updateOrder(Order(id = o1, customerId = customerId, sequenceNumber = 1, categoryId = categoryId, fabricType = "صوف", ready = false))
        viewModel.resetCustomerCompletionNotified(customerId)
        assertFalse(viewModel.isCustomerCompletionNotified(customerId))

        // Marking ready again
        repository.updateOrder(Order(id = o1, customerId = customerId, sequenceNumber = 1, categoryId = categoryId, fabricType = "صوف", ready = true))
        val reCompletedResult = viewModel.checkCustomerOrderCompletionDirect(customerId)
        assertNotNull("Should trigger again when re-completed", reCompletedResult)
    }

    @Test
    fun `per-customer messaging preferences are independent`() {
        val custA: Long = 1001
        val custB: Long = 1002

        // Default should be allowed
        assertTrue(viewModel.isCustomerMessagingAllowed(custA))
        assertTrue(viewModel.isCustomerMessagingAllowed(custB))

        // Disabling for customer A
        viewModel.setCustomerMessagingAllowed(custA, false)
        assertFalse(viewModel.isCustomerMessagingAllowed(custA))
        assertTrue("Customer B must remain unaffected", viewModel.isCustomerMessagingAllowed(custB))

        // Enabling customer A back
        viewModel.setCustomerMessagingAllowed(custA, true)
        assertTrue(viewModel.isCustomerMessagingAllowed(custA))
        assertTrue(viewModel.isCustomerMessagingAllowed(custB))
    }

    @Test
    fun `shop messaging controls operate persistently and independently from customer preferences`() {
        val custId: Long = 2001
        viewModel.setCustomerMessagingAllowed(custId, true)

        // Shop phone update
        viewModel.updateShopPhoneNumber("0551234567")
        assertEquals("0551234567", viewModel.shopPhoneNumber)

        // Stop shop messaging toggle
        viewModel.updateStopShopMessaging(true)
        assertTrue(viewModel.isStopShopMessaging())
        assertFalse(viewModel.isShopMessagingEnabled)
        assertTrue("Customer preference must not change when shop messaging is stopped", viewModel.isCustomerMessagingAllowed(custId))

        // Stop customer messaging globally
        viewModel.updateStopCustomerMessagingOnReady(true)
        assertTrue(viewModel.isStopCustomerMessagingOnReady())
        assertFalse(viewModel.isCustomerMessagingEnabled)
        assertTrue("Customer-specific setting must remain true even if globally stopped", viewModel.isCustomerMessagingAllowed(custId))
    }
}

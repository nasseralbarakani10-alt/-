package com.example.data.repository

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
import com.example.data.model.OrderWithCategory
import com.example.data.model.Tailor
import com.example.data.model.TailorPrice
import com.example.data.model.TailorReportExpense
import com.example.data.model.User
import com.example.data.model.UserPermissions
import com.example.data.model.UserWithPermissions
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val orderDao: OrderDao,
    private val categoryDao: CategoryDao,
    private val cutterDao: CutterDao,
    private val tailorDao: TailorDao,
    private val cutterPriceDao: CutterPriceDao,
    private val tailorPriceDao: TailorPriceDao,
    private val userDao: UserDao,
    private val userPermissionsDao: UserPermissionsDao,
    private val cutterReportExpenseDao: CutterReportExpenseDao,
    private val tailorReportExpenseDao: TailorReportExpenseDao,
    private val linkedDeviceDao: LinkedDeviceDao? = null,
    private val messagingSettingsDao: MessagingSettingsDao? = null,
    private val appSettingsDao: AppSettingsDao? = null
) {
    // Orders
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()
    val allOrdersWithCategory: Flow<List<OrderWithCategory>> = orderDao.getAllOrdersWithCategory()
    val orderCount: Flow<Int> = orderDao.getOrderCount()

    fun getOrdersBetween(startTime: Long, endTime: Long): Flow<List<OrderWithCategory>> =
        orderDao.getOrdersWithCategoryBetween(startTime, endTime)

    fun getFilteredOrders(
        startTime: Long?,
        endTime: Long?,
        cutterId: Long?,
        tailorId: Long?,
        categoryId: Long?
    ): Flow<List<OrderWithCategory>> =
        orderDao.getFilteredOrders(startTime, endTime, cutterId, tailorId, categoryId)

    fun getOrderById(id: Long): Flow<Order?> = orderDao.getOrderById(id)
    suspend fun getOrderByIdDirect(id: Long): Order? = orderDao.getOrderByIdDirect(id)

    suspend fun insertOrder(order: Order): Long = orderDao.insert(order)

    suspend fun updateOrder(order: Order) = orderDao.update(order)

    suspend fun updateOrderWithPermission(
        order: Order,
        currentUser: User?,
        currentPermissions: UserPermissions?
    ): Result<Unit> {
        if (currentUser == null || !currentUser.isActive) {
            return Result.failure(SecurityException("المستخدم غير مسجل أو حسابه معطل"))
        }

        val isAdmin = currentUser.isAdmin
        val canEdit = isAdmin || (currentPermissions?.canEdit == true)
        if (!canEdit) {
            return Result.failure(SecurityException("ليس لديك صلاحية لتعديل بيانات العمليات"))
        }

        val existingOrder = orderDao.getOrderByIdDirect(order.id)
        var newReadyLocked = order.readyLockedByAdmin

        if (existingOrder != null) {
            if (existingOrder.ready != order.ready) {
                // If ready was already locked by admin
                if (existingOrder.readyLockedByAdmin) {
                    if (!isAdmin) {
                        return Result.failure(SecurityException("لا يمكن تعديل حالة الجاهزية بعد قفلها إلا بواسطة المدير"))
                    }
                } else {
                    // Not yet locked: check canChangeReadyStatus or isAdmin
                    val canChangeReady = isAdmin || (currentPermissions?.canChangeReadyStatus == true)
                    if (!canChangeReady) {
                        return Result.failure(SecurityException("ليس لديك صلاحية لتغيير حالة جاهزية الطلب"))
                    }
                    // When setting ready to true, lock it
                    if (order.ready) {
                        newReadyLocked = true
                    }
                }
            }
        } else if (order.ready) {
            newReadyLocked = true
        }

        val updated = order.copy(
            readyLockedByAdmin = newReadyLocked,
            updatedAt = System.currentTimeMillis()
        )
        orderDao.update(updated)
        return Result.success(Unit)
    }

    suspend fun deleteOrder(order: Order) = orderDao.delete(order)

    suspend fun deleteOrderWithPermission(
        order: Order,
        currentUser: User?,
        currentPermissions: UserPermissions?
    ): Result<Unit> {
        if (currentUser == null || !currentUser.isActive) {
            return Result.failure(SecurityException("المستخدم غير مسجل أو حسابه معطل"))
        }
        val canDelete = currentUser.isAdmin || (currentPermissions?.canDelete == true)
        if (!canDelete) {
            return Result.failure(SecurityException("ليس لديك صلاحية لحذف العمليات"))
        }
        orderDao.delete(order)
        return Result.success(Unit)
    }

    suspend fun deleteOrdersByIds(ids: List<Long>) = orderDao.deleteByIds(ids)

    suspend fun deleteOrdersByIdsWithPermission(
        ids: List<Long>,
        currentUser: User?,
        currentPermissions: UserPermissions?
    ): Result<Unit> {
        if (currentUser == null || !currentUser.isActive) {
            return Result.failure(SecurityException("المستخدم غير مسجل أو حسابه معطل"))
        }
        val canDelete = currentUser.isAdmin || (currentPermissions?.canDelete == true)
        if (!canDelete) {
            return Result.failure(SecurityException("ليس لديك صلاحية لحذف العمليات"))
        }
        orderDao.deleteByIds(ids)
        return Result.success(Unit)
    }

    suspend fun getOrderCountForCutter(cutterId: Long): Int = orderDao.getOrderCountForCutter(cutterId)

    suspend fun getOrderCountForTailor(tailorId: Long): Int = orderDao.getOrderCountForTailor(tailorId)

    suspend fun getOrderCountForCategory(categoryId: Long): Int = orderDao.getOrderCountForCategory(categoryId)

    // Cutters & Tailors with Admin permission check for historical orders
    suspend fun deleteCutterWithPermission(
        cutter: Cutter,
        hasOrders: Boolean,
        currentUser: User?
    ): Result<Unit> {
        if (hasOrders) {
            if (currentUser?.isAdmin != true) {
                return Result.failure(SecurityException("فقط المدير يمكنه إيقاف أو حذف عامل لديه طلبات سابقة"))
            }
            cutterDao.update(cutter.copy(isActive = false))
        } else {
            try {
                cutterDao.delete(cutter)
            } catch (e: Exception) {
                cutterDao.update(cutter.copy(isActive = false))
            }
        }
        return Result.success(Unit)
    }

    suspend fun deleteTailorWithPermission(
        tailor: Tailor,
        hasOrders: Boolean,
        currentUser: User?
    ): Result<Unit> {
        if (hasOrders) {
            if (currentUser?.isAdmin != true) {
                return Result.failure(SecurityException("فقط المدير يمكنه إيقاف أو حذف عامل لديه طلبات سابقة"))
            }
            tailorDao.update(tailor.copy(isActive = false))
        } else {
            try {
                tailorDao.delete(tailor)
            } catch (e: Exception) {
                tailorDao.update(tailor.copy(isActive = false))
            }
        }
        return Result.success(Unit)
    }

    // Users
    val allUsers: Flow<List<User>> = userDao.getAllUsers()
    val allUsersWithPermissions: Flow<List<UserWithPermissions>> = userDao.getAllUsersWithPermissions()

    suspend fun getUserByUsername(username: String): User? = userDao.getUserByUsername(username)
    suspend fun getUserById(id: Long): User? = userDao.getUserById(id)
    fun getUserByIdFlow(id: Long): Flow<User?> = userDao.getUserByIdFlow(id)
    fun getUserWithPermissionsFlow(id: Long): Flow<UserWithPermissions?> = userDao.getUserWithPermissionsFlow(id)

    suspend fun insertUser(user: User): Long = userDao.insert(user)
    suspend fun updateUser(user: User) = userDao.update(user)
    suspend fun deleteUser(user: User) = userDao.delete(user)
    suspend fun getUserCount(): Int = userDao.getUserCount()

    // User Permissions
    suspend fun getPermissionsForUser(userId: Long): UserPermissions? = userPermissionsDao.getPermissionsForUser(userId)
    fun getPermissionsForUserFlow(userId: Long): Flow<UserPermissions?> = userPermissionsDao.getPermissionsForUserFlow(userId)
    suspend fun insertOrUpdatePermissions(permissions: UserPermissions): Long = userPermissionsDao.insert(permissions)
    suspend fun updatePermissions(permissions: UserPermissions) = userPermissionsDao.update(permissions)

    // Categories
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun getCategoryCount(): Int = categoryDao.getCategoryCount()

    suspend fun getMaxCategorySortOrder(): Int = categoryDao.getMaxSortOrder() ?: 0

    suspend fun getCategoryByName(name: String): Category? = categoryDao.getByName(name)

    fun getCategoryById(id: Long): Flow<Category?> = categoryDao.getById(id)

    suspend fun insertCategory(category: Category): Long = categoryDao.insert(category)

    suspend fun insertAllCategories(categories: List<Category>) = categoryDao.insertAll(categories)

    suspend fun updateCategory(category: Category) = categoryDao.update(category)

    suspend fun incrementCategoryUsageCount(id: Long) = categoryDao.incrementUsageCount(id)

    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)

    // Cutters
    val allCutters: Flow<List<Cutter>> = cutterDao.getAllCutters()
    val activeCutters: Flow<List<Cutter>> = cutterDao.getActiveCutters()

    fun getCutterById(id: Long): Flow<Cutter?> = cutterDao.getById(id)

    suspend fun insertCutter(cutter: Cutter): Long = cutterDao.insert(cutter)

    suspend fun updateCutter(cutter: Cutter) = cutterDao.update(cutter)

    suspend fun incrementCutterUsageCount(id: Long) = cutterDao.incrementUsageCount(id)

    suspend fun deleteCutter(cutter: Cutter) = cutterDao.delete(cutter)

    // Tailors
    val allTailors: Flow<List<Tailor>> = tailorDao.getAllTailors()
    val activeTailors: Flow<List<Tailor>> = tailorDao.getActiveTailors()

    fun getTailorById(id: Long): Flow<Tailor?> = tailorDao.getById(id)

    suspend fun insertTailor(tailor: Tailor): Long = tailorDao.insert(tailor)

    suspend fun updateTailor(tailor: Tailor) = tailorDao.update(tailor)

    suspend fun incrementTailorUsageCount(id: Long) = tailorDao.incrementUsageCount(id)

    suspend fun deleteTailor(tailor: Tailor) = tailorDao.delete(tailor)

    // Cutter Prices
    fun getPricesForCutter(cutterId: Long): Flow<List<CutterPrice>> =
        cutterPriceDao.getPricesForCutter(cutterId)

    suspend fun getPricesListForCutter(cutterId: Long): List<CutterPrice> =
        cutterPriceDao.getPricesListForCutter(cutterId)

    fun getCutterPrice(cutterId: Long, categoryId: Long): Flow<CutterPrice?> =
        cutterPriceDao.getPrice(cutterId, categoryId)

    suspend fun getCutterPriceDirect(cutterId: Long, categoryId: Long): CutterPrice? =
        cutterPriceDao.getPriceDirect(cutterId, categoryId)

    suspend fun insertCutterPrice(cutterPrice: CutterPrice): Long =
        cutterPriceDao.insert(cutterPrice)

    suspend fun saveCutterPrices(prices: List<CutterPrice>) =
        cutterPriceDao.insertAll(prices)

    suspend fun updateCutterPrice(cutterPrice: CutterPrice) =
        cutterPriceDao.update(cutterPrice)

    suspend fun deleteCutterPrice(cutterPrice: CutterPrice) =
        cutterPriceDao.delete(cutterPrice)

    // Tailor Prices
    fun getPricesForTailor(tailorId: Long): Flow<List<TailorPrice>> =
        tailorPriceDao.getPricesForTailor(tailorId)

    suspend fun getPricesListForTailor(tailorId: Long): List<TailorPrice> =
        tailorPriceDao.getPricesListForTailor(tailorId)

    fun getTailorPrice(tailorId: Long, categoryId: Long): Flow<TailorPrice?> =
        tailorPriceDao.getPrice(tailorId, categoryId)

    suspend fun getTailorPriceDirect(tailorId: Long, categoryId: Long): TailorPrice? =
        tailorPriceDao.getPriceDirect(tailorId, categoryId)

    suspend fun insertTailorPrice(tailorPrice: TailorPrice): Long =
        tailorPriceDao.insert(tailorPrice)

    suspend fun saveTailorPrices(prices: List<TailorPrice>) =
        tailorPriceDao.insertAll(prices)

    suspend fun updateTailorPrice(tailorPrice: TailorPrice) =
        tailorPriceDao.update(tailorPrice)

    suspend fun deleteTailorPrice(tailorPrice: TailorPrice) =
        tailorPriceDao.delete(tailorPrice)

    // Cutter Report Expenses
    fun getCutterReportExpense(cutterId: Long, periodStart: Long, periodEnd: Long): Flow<CutterReportExpense?> =
        cutterReportExpenseDao.getExpense(cutterId, periodStart, periodEnd)

    suspend fun getCutterReportExpenseDirect(cutterId: Long, periodStart: Long, periodEnd: Long): CutterReportExpense? =
        cutterReportExpenseDao.getExpenseDirect(cutterId, periodStart, periodEnd)

    suspend fun saveCutterReportExpense(cutterId: Long, periodStart: Long, periodEnd: Long, amount: Double) {
        val existing = cutterReportExpenseDao.getExpenseDirect(cutterId, periodStart, periodEnd)
        val expense = existing?.copy(expenseAmount = amount, updatedAt = System.currentTimeMillis())
            ?: CutterReportExpense(
                cutterId = cutterId,
                periodStart = periodStart,
                periodEnd = periodEnd,
                expenseAmount = amount
            )
        cutterReportExpenseDao.upsertExpense(expense)
    }

    // Tailor Report Expenses
    fun getTailorReportExpense(tailorId: Long, periodStart: Long, periodEnd: Long): Flow<TailorReportExpense?> =
        tailorReportExpenseDao.getExpense(tailorId, periodStart, periodEnd)

    suspend fun getTailorReportExpenseDirect(tailorId: Long, periodStart: Long, periodEnd: Long): TailorReportExpense? =
        tailorReportExpenseDao.getExpenseDirect(tailorId, periodStart, periodEnd)

    suspend fun saveTailorReportExpense(tailorId: Long, periodStart: Long, periodEnd: Long, amount: Double) {
        val existing = tailorReportExpenseDao.getExpenseDirect(tailorId, periodStart, periodEnd)
        val expense = existing?.copy(expenseAmount = amount, updatedAt = System.currentTimeMillis())
            ?: TailorReportExpense(
                tailorId = tailorId,
                periodStart = periodStart,
                periodEnd = periodEnd,
                expenseAmount = amount
            )
        tailorReportExpenseDao.upsertExpense(expense)
    }

    // Linked Devices
    val allLinkedDevices: Flow<List<LinkedDevice>> =
        linkedDeviceDao?.getAllLinkedDevices() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getDeviceByDeviceId(deviceId: String): LinkedDevice? =
        linkedDeviceDao?.getDeviceByDeviceId(deviceId)

    suspend fun getDeviceById(id: Long): LinkedDevice? =
        linkedDeviceDao?.getDeviceById(id)

    suspend fun insertLinkedDevice(device: LinkedDevice): Long =
        linkedDeviceDao?.insert(device) ?: -1L

    suspend fun updateLinkedDevice(device: LinkedDevice) {
        linkedDeviceDao?.update(device)
    }

    suspend fun deleteLinkedDevice(device: LinkedDevice) {
        linkedDeviceDao?.delete(device)
    }

    suspend fun deleteLinkedDeviceById(id: Long) {
        linkedDeviceDao?.deleteById(id)
    }

    // Messaging Settings
    val messagingSettings: Flow<MessagingSettings?> =
        messagingSettingsDao?.getSettingsFlow() ?: kotlinx.coroutines.flow.flowOf(MessagingSettings())

    suspend fun getMessagingSettingsDirect(): MessagingSettings {
        return messagingSettingsDao?.getSettings() ?: MessagingSettings()
    }

    suspend fun saveMessagingSettings(settings: MessagingSettings) {
        messagingSettingsDao?.saveSettings(settings)
    }

    // App Settings
    val appSettings: Flow<AppSettings?> =
        appSettingsDao?.getSettingsFlow() ?: kotlinx.coroutines.flow.flowOf(AppSettings())

    suspend fun getAppSettingsDirect(): AppSettings {
        return appSettingsDao?.getSettings() ?: AppSettings()
    }

    suspend fun saveAppSettings(settings: AppSettings) {
        appSettingsDao?.saveSettings(settings)
    }
}

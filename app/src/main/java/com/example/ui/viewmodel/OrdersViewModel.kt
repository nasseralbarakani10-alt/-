package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Customer
import com.example.data.model.DraftOrderData
import com.example.data.model.Order
import com.example.data.model.OrderWithCategory
import com.example.data.repository.AppRepository
import com.example.data.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OrdersViewModel(
    private val repository: AppRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedOrderIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedOrderIds: StateFlow<Set<Long>> = _selectedOrderIds.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val orderCount: StateFlow<Int> = repository.orderCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val ordersWithCategory: StateFlow<List<OrderWithCategory>> = combine(
        repository.allOrdersWithCategory,
        _searchQuery
    ) { orders, query ->
        if (query.isBlank()) {
            orders
        } else {
            val trimmed = query.trim()
            orders.filter { item ->
                item.customerName.contains(trimmed, ignoreCase = true) ||
                item.customerNumber.contains(trimmed, ignoreCase = true) ||
                item.phoneNumber.contains(trimmed, ignoreCase = true) ||
                item.order.fabricType.contains(trimmed, ignoreCase = true) ||
                (item.category?.name?.contains(trimmed, ignoreCase = true) == true) ||
                (item.cutter?.name?.contains(trimmed, ignoreCase = true) == true) ||
                (item.tailor?.name?.contains(trimmed, ignoreCase = true) == true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getCutterPriceDirect(cutterId: Long, categoryId: Long): Double =
        repository.getCutterPriceDirect(cutterId, categoryId)?.price ?: 0.0

    suspend fun getTailorPriceDirect(tailorId: Long, categoryId: Long): Double =
        repository.getTailorPriceDirect(tailorId, categoryId)?.price ?: 0.0

    suspend fun findCustomerByNumberOrPhone(number: String, phone: String): Customer? {
        val trimmedNum = number.trim()
        val trimmedPhone = phone.trim()
        if (trimmedNum.isNotBlank()) {
            val byNum = repository.getCustomerByNumber(trimmedNum)
            if (byNum != null) return byNum
        }
        if (trimmedPhone.isNotBlank()) {
            val byPhone = repository.getCustomerByPhone(trimmedPhone)
            if (byPhone != null) return byPhone
        }
        return null
    }

    suspend fun getOrdersForCustomerDirect(customerId: Long): List<Order> =
        repository.getOrdersForCustomerDirect(customerId)

    suspend fun getCustomerById(customerId: Long): Customer? =
        repository.getCustomerById(customerId)

    // Completion notification & messaging enablement flags
    val isShopMessagingEnabled: Boolean
        get() = !repository.isStopShopMessaging()

    val isCustomerMessagingEnabled: Boolean
        get() = !repository.isStopCustomerMessagingOnReady()

    val shopPhoneNumber: String
        get() = repository.getShopPhoneNumber()

    private val _notifiedCompletedCustomerIds = mutableSetOf<Long>()

    fun isCustomerCompletionNotified(customerId: Long): Boolean {
        return _notifiedCompletedCustomerIds.contains(customerId)
    }

    fun markCustomerCompletionNotified(customerId: Long) {
        _notifiedCompletedCustomerIds.add(customerId)
    }

    fun resetCustomerCompletionNotified(customerId: Long) {
        _notifiedCompletedCustomerIds.remove(customerId)
    }

    suspend fun checkCustomerOrderCompletionDirect(customerId: Long): Pair<Customer, List<Order>>? {
        val allOrders = repository.getOrdersForCustomerDirect(customerId)
        if (allOrders.isNotEmpty() && allOrders.all { it.ready }) {
            if (!_notifiedCompletedCustomerIds.contains(customerId)) {
                _notifiedCompletedCustomerIds.add(customerId)
                val customer = repository.getCustomerById(customerId)
                if (customer != null) {
                    return Pair(customer, allOrders)
                }
            }
        }
        return null
    }

    fun checkAndTriggerCompletion(
        customerId: Long,
        onTrigger: (customer: Customer, orders: List<Order>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = checkCustomerOrderCompletionDirect(customerId)
                if (result != null) {
                    onTrigger(result.first, result.second)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveCustomerAndSingleOrder(
        customerName: String,
        customerNumber: String,
        phoneNumber: String,
        existingCustomerId: Long?,
        draft: DraftOrderData,
        onSuccess: (Long, Long) -> Unit = { _, _ -> },
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val order = Order(
                    customerId = existingCustomerId ?: 0L,
                    sequenceNumber = 1,
                    categoryId = draft.categoryId,
                    fabricType = draft.fabricType.trim(),
                    cutterId = draft.cutterId,
                    tailorId = draft.tailorId,
                    cutterPrice = draft.cutterPrice,
                    tailorPrice = draft.tailorPrice,
                    buttonIroning = draft.buttonIroning,
                    laundry = draft.laundry,
                    ready = false,
                    readyLockedByAdmin = false,
                    createdAt = now,
                    updatedAt = now
                )
                val (customerId, orderId) = repository.saveCustomerAndOrder(
                    customerName = customerName.trim(),
                    customerNumber = customerNumber.trim(),
                    phoneNumber = phoneNumber.trim(),
                    existingCustomerId = existingCustomerId,
                    order = order
                )
                if (customerId > 0 && orderId > 0) {
                    onSuccess(customerId, orderId)
                } else {
                    onError("حدث خطأ أثناء حفظ العميل والطلب")
                }
            } catch (e: Exception) {
                val msg = e.message ?: "حدث خطأ أثناء الحفظ"
                _errorMessage.value = msg
                onError(msg)
            }
        }
    }

    fun addCustomerWithOrders(
        customerName: String,
        customerNumber: String,
        phoneNumber: String,
        ordersList: List<DraftOrderData>,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val customer = Customer(
                    name = customerName.trim(),
                    customerNumber = customerNumber.trim(),
                    phoneNumber = phoneNumber.trim(),
                    createdAt = now
                )
                val orders = ordersList.mapIndexed { index, draft ->
                    Order(
                        customerId = 0L, // will be set in repository.insertCustomerWithOrders
                        sequenceNumber = index + 1,
                        categoryId = draft.categoryId,
                        fabricType = draft.fabricType.trim(),
                        cutterId = draft.cutterId,
                        tailorId = draft.tailorId,
                        cutterPrice = draft.cutterPrice,
                        tailorPrice = draft.tailorPrice,
                        buttonIroning = draft.buttonIroning,
                        laundry = draft.laundry,
                        ready = false,
                        readyLockedByAdmin = false,
                        createdAt = now,
                        updatedAt = now
                    )
                }
                val customerId = repository.insertCustomerWithOrders(customer, orders)
                if (customerId > 0) {
                    onSuccess()
                } else {
                    onError("حدث خطأ أثناء حفظ العميل والطلبات")
                }
            } catch (e: Exception) {
                val msg = e.message ?: "حدث خطأ أثناء الحفظ"
                _errorMessage.value = msg
                onError(msg)
            }
        }
    }

    fun updateOrderAndCustomer(
        order: Order,
        customer: Customer?,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                if (customer != null) {
                    repository.updateCustomer(customer)
                }
                val result = repository.updateOrderWithPermission(
                    order = order,
                    currentUser = sessionManager.currentUser.value,
                    currentPermissions = sessionManager.currentPermissions.value
                )
                if (result.isSuccess) {
                    onSuccess()
                } else {
                    val msg = result.exceptionOrNull()?.message ?: "ليس لديك صلاحية لتعديل العمليات"
                    _errorMessage.value = msg
                    onError(msg)
                }
            } catch (e: Exception) {
                val msg = e.message ?: "حدث خطأ أثناء الحفظ"
                _errorMessage.value = msg
                onError(msg)
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun getOrdersBetween(startTime: Long, endTime: Long): Flow<List<OrderWithCategory>> =
        repository.getOrdersBetween(startTime, endTime)

    fun getFilteredOrders(
        startTime: Long?,
        endTime: Long?,
        cutterId: Long?,
        tailorId: Long?,
        categoryId: Long?
    ): Flow<List<OrderWithCategory>> =
        repository.getFilteredOrders(startTime, endTime, cutterId, tailorId, categoryId)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSelection(orderId: Long) {
        _selectedOrderIds.value = if (_selectedOrderIds.value.contains(orderId)) {
            _selectedOrderIds.value - orderId
        } else {
            _selectedOrderIds.value + orderId
        }
    }

    fun clearSelection() {
        _selectedOrderIds.value = emptySet()
    }

    fun deleteSingleOrderDirect(orderId: Long, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.deleteOrdersByIds(listOf(orderId))
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "فشل حذف الطلب")
            }
        }
    }

    fun deleteSelectedOrders(onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val toDelete = _selectedOrderIds.value.toList()
        if (toDelete.isNotEmpty()) {
            viewModelScope.launch {
                val result = repository.deleteOrdersByIdsWithPermission(
                    ids = toDelete,
                    currentUser = sessionManager.currentUser.value,
                    currentPermissions = sessionManager.currentPermissions.value
                )
                if (result.isSuccess) {
                    clearSelection()
                    onSuccess()
                } else {
                    val msg = result.exceptionOrNull()?.message ?: "حدث خطأ أثناء محاولة الحذف"
                    _errorMessage.value = msg
                    onError(msg)
                }
            }
        }
    }

    fun addOrder(
        customerName: String,
        customerNumber: String,
        phoneNumber: String,
        categoryId: Long,
        fabricType: String,
        cutterId: Long? = null,
        tailorId: Long? = null,
        buttonIroning: Boolean = false,
        laundry: Boolean = false,
        ready: Boolean = false
    ) {
        viewModelScope.launch {
            // Silently look up cutter price if combination exists
            val cutterPrice = if (cutterId != null) {
                repository.getCutterPriceDirect(cutterId, categoryId)?.price ?: 0.0
            } else 0.0

            // Silently look up tailor price if combination exists
            val tailorPrice = if (tailorId != null) {
                repository.getTailorPriceDirect(tailorId, categoryId)?.price ?: 0.0
            } else 0.0

            val now = System.currentTimeMillis()
            val customer = Customer(
                name = customerName,
                customerNumber = customerNumber,
                phoneNumber = phoneNumber,
                createdAt = now
            )
            val customerId = repository.insertCustomer(customer)
            val order = Order(
                customerId = customerId,
                sequenceNumber = 1,
                categoryId = categoryId,
                fabricType = fabricType,
                cutterId = cutterId,
                tailorId = tailorId,
                cutterPrice = cutterPrice,
                tailorPrice = tailorPrice,
                buttonIroning = buttonIroning,
                laundry = laundry,
                ready = ready,
                readyLockedByAdmin = ready,
                createdAt = now,
                updatedAt = now
            )
            val newOrderId = repository.insertOrder(order)
            if (newOrderId > 0) {
                // Increment usageCount for the selected category, cutter, and tailor
                repository.incrementCategoryUsageCount(categoryId)
                if (cutterId != null) {
                    repository.incrementCutterUsageCount(cutterId)
                }
                if (tailorId != null) {
                    repository.incrementTailorUsageCount(tailorId)
                }
            }
        }
    }

    fun toggleLaundry(order: Order, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val updated = order.copy(
                laundry = !order.laundry,
                updatedAt = System.currentTimeMillis()
            )
            val result = repository.updateOrderWithPermission(
                order = updated,
                currentUser = sessionManager.currentUser.value,
                currentPermissions = sessionManager.currentPermissions.value
            )
            if (result.isFailure) {
                val msg = result.exceptionOrNull()?.message ?: "ليس لديك صلاحية لتعديل هذا الطلب"
                _errorMessage.value = msg
                onError(msg)
            }
        }
    }

    fun toggleButtonIroning(order: Order, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val updated = order.copy(
                buttonIroning = !order.buttonIroning,
                updatedAt = System.currentTimeMillis()
            )
            val result = repository.updateOrderWithPermission(
                order = updated,
                currentUser = sessionManager.currentUser.value,
                currentPermissions = sessionManager.currentPermissions.value
            )
            if (result.isFailure) {
                val msg = result.exceptionOrNull()?.message ?: "ليس لديك صلاحية لتعديل هذا الطلب"
                _errorMessage.value = msg
                onError(msg)
            }
        }
    }

    fun setOrderReady(order: Order, ready: Boolean, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val updated = order.copy(
                ready = ready,
                updatedAt = System.currentTimeMillis()
            )
            val result = repository.updateOrderWithPermission(
                order = updated,
                currentUser = sessionManager.currentUser.value,
                currentPermissions = sessionManager.currentPermissions.value
            )
            if (result.isSuccess) {
                onSuccess()
            } else {
                val msg = result.exceptionOrNull()?.message ?: "ليس لديك صلاحية لتغيير حالة الجاهزية"
                _errorMessage.value = msg
                onError(msg)
            }
        }
    }

    fun toggleReady(order: Order, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val targetReady = !order.ready
            val updated = order.copy(
                ready = targetReady,
                updatedAt = System.currentTimeMillis()
            )
            val result = repository.updateOrderWithPermission(
                order = updated,
                currentUser = sessionManager.currentUser.value,
                currentPermissions = sessionManager.currentPermissions.value
            )
            if (result.isFailure) {
                val msg = result.exceptionOrNull()?.message ?: "ليس لديك صلاحية لتغيير حالة الجاهزية"
                _errorMessage.value = msg
                onError(msg)
            }
        }
    }

    fun deleteOrder(order: Order, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.deleteOrderWithPermission(
                order = order,
                currentUser = sessionManager.currentUser.value,
                currentPermissions = sessionManager.currentPermissions.value
            )
            if (result.isSuccess) {
                onSuccess()
            } else {
                val msg = result.exceptionOrNull()?.message ?: "ليس لديك صلاحية لحذف العمليات"
                _errorMessage.value = msg
                onError(msg)
            }
        }
    }

    fun updateOrder(order: Order, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.updateOrderWithPermission(
                order = order,
                currentUser = sessionManager.currentUser.value,
                currentPermissions = sessionManager.currentPermissions.value
            )
            if (result.isSuccess) {
                onSuccess()
            } else {
                val msg = result.exceptionOrNull()?.message ?: "ليس لديك صلاحية لتعديل العمليات"
                _errorMessage.value = msg
                onError(msg)
            }
        }
    }

    // Messaging Settings & Preferences
    fun isStopShopMessaging(): Boolean = repository.isStopShopMessaging()
    fun isStopCustomerMessagingOnReady(): Boolean = repository.isStopCustomerMessagingOnReady()

    fun isCustomerMessagingAllowed(customerId: Long): Boolean =
        repository.isCustomerMessagingAllowed(customerId)

    fun setCustomerMessagingAllowed(customerId: Long, allowed: Boolean) {
        repository.setCustomerMessagingAllowed(customerId, allowed)
    }

    fun updateShopPhoneNumber(phone: String) {
        repository.setShopPhoneNumber(phone)
    }

    fun updateStopShopMessaging(stop: Boolean) {
        repository.setStopShopMessaging(stop)
    }

    fun updateStopCustomerMessagingOnReady(stop: Boolean) {
        repository.setStopCustomerMessagingOnReady(stop)
    }
}

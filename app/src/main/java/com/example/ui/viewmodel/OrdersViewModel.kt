package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                item.order.customerName.contains(trimmed, ignoreCase = true) ||
                item.order.customerNumber.contains(trimmed, ignoreCase = true) ||
                item.order.phoneNumber.contains(trimmed, ignoreCase = true) ||
                item.order.fabricType.contains(trimmed, ignoreCase = true) ||
                (item.category?.name?.contains(trimmed, ignoreCase = true) == true) ||
                (item.cutter?.name?.contains(trimmed, ignoreCase = true) == true) ||
                (item.tailor?.name?.contains(trimmed, ignoreCase = true) == true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
            val order = Order(
                customerName = customerName,
                customerNumber = customerNumber,
                phoneNumber = phoneNumber,
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
}

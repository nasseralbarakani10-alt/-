package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Cutter
import com.example.data.repository.AppRepository
import com.example.data.session.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CuttersViewModel(
    private val repository: AppRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val allCutters: StateFlow<List<Cutter>> = combine(
        repository.allCutters,
        repository.appSettings
    ) { cutters, settings ->
        val sortByFrequency = settings?.sortByFrequencyEnabled ?: true
        if (sortByFrequency) {
            cutters.sortedWith(
                compareByDescending<Cutter> { it.usageCount }
                    .thenBy { it.name }
            )
        } else {
            cutters.sortedBy { it.name }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCutters: StateFlow<List<Cutter>> = combine(
        repository.activeCutters,
        repository.appSettings
    ) { cutters, settings ->
        val sortByFrequency = settings?.sortByFrequencyEnabled ?: true
        if (sortByFrequency) {
            cutters.sortedWith(
                compareByDescending<Cutter> { it.usageCount }
                    .thenBy { it.name }
            )
        } else {
            cutters.sortedBy { it.name }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCutter(name: String, phoneNumber: String = "", onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insertCutter(
                Cutter(
                    name = name,
                    phoneNumber = phoneNumber,
                    isActive = true
                )
            )
            onComplete()
        }
    }

    fun setCutterStopped(cutter: Cutter, isStopped: Boolean) {
        viewModelScope.launch {
            repository.updateCutter(cutter.copy(isActive = !isStopped))
        }
    }

    fun updateCutter(cutter: Cutter, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateCutter(cutter)
            onComplete()
        }
    }

    suspend fun hasAssociatedOrders(cutterId: Long): Boolean {
        return repository.getOrderCountForCutter(cutterId) > 0
    }

    suspend fun getPricesListForCutter(cutterId: Long): List<com.example.data.model.CutterPrice> {
        return repository.getPricesListForCutter(cutterId)
    }

    fun savePricesForCutter(cutterId: Long, categoryPrices: Map<Long, Double>, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val list = categoryPrices.map { (catId, price) ->
                com.example.data.model.CutterPrice(
                    cutterId = cutterId,
                    categoryId = catId,
                    price = price
                )
            }
            repository.saveCutterPrices(list)
            onComplete()
        }
    }

    fun deleteCutter(
        cutter: Cutter,
        hasOrders: Boolean,
        onComplete: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.deleteCutterWithPermission(
                cutter = cutter,
                hasOrders = hasOrders,
                currentUser = sessionManager.currentUser.value
            )
            if (result.isSuccess) {
                onComplete()
            } else {
                onError(result.exceptionOrNull()?.message ?: "حدث خطأ أثناء الحذف")
            }
        }
    }

    fun getExpense(cutterId: Long, periodStart: Long, periodEnd: Long) =
        repository.getCutterReportExpense(cutterId, periodStart, periodEnd)

    suspend fun getExpenseDirect(cutterId: Long, periodStart: Long, periodEnd: Long): Double {
        return repository.getCutterReportExpenseDirect(cutterId, periodStart, periodEnd)?.expenseAmount ?: 0.0
    }

    fun saveExpense(cutterId: Long, periodStart: Long, periodEnd: Long, amount: Double) {
        viewModelScope.launch {
            repository.saveCutterReportExpense(cutterId, periodStart, periodEnd, amount)
        }
    }
}

package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Tailor
import com.example.data.repository.AppRepository
import com.example.data.session.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TailorsViewModel(
    private val repository: AppRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val allTailors: StateFlow<List<Tailor>> = repository.allTailors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTailors: StateFlow<List<Tailor>> = repository.activeTailors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTailor(name: String, phoneNumber: String = "", onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insertTailor(
                Tailor(
                    name = name,
                    phoneNumber = phoneNumber,
                    isActive = true
                )
            )
            onComplete()
        }
    }

    fun setTailorStopped(tailor: Tailor, isStopped: Boolean) {
        viewModelScope.launch {
            repository.updateTailor(tailor.copy(isActive = !isStopped))
        }
    }

    fun updateTailor(tailor: Tailor, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateTailor(tailor)
            onComplete()
        }
    }

    suspend fun hasAssociatedOrders(tailorId: Long): Boolean {
        return repository.getOrderCountForTailor(tailorId) > 0
    }

    suspend fun getPricesListForTailor(tailorId: Long): List<com.example.data.model.TailorPrice> {
        return repository.getPricesListForTailor(tailorId)
    }

    fun savePricesForTailor(tailorId: Long, categoryPrices: Map<Long, Double>, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val list = categoryPrices.map { (catId, price) ->
                com.example.data.model.TailorPrice(
                    tailorId = tailorId,
                    categoryId = catId,
                    price = price
                )
            }
            repository.saveTailorPrices(list)
            onComplete()
        }
    }

    fun deleteTailor(
        tailor: Tailor,
        hasOrders: Boolean,
        onComplete: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.deleteTailorWithPermission(
                tailor = tailor,
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

    fun getExpense(tailorId: Long, periodStart: Long, periodEnd: Long) =
        repository.getTailorReportExpense(tailorId, periodStart, periodEnd)

    suspend fun getExpenseDirect(tailorId: Long, periodStart: Long, periodEnd: Long): Double {
        return repository.getTailorReportExpenseDirect(tailorId, periodStart, periodEnd)?.expenseAmount ?: 0.0
    }

    fun saveExpense(tailorId: Long, periodStart: Long, periodEnd: Long, amount: Double) {
        viewModelScope.launch {
            repository.saveTailorReportExpense(tailorId, periodStart, periodEnd, amount)
        }
    }
}

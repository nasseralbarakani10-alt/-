package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Category
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class CategoryOpResult {
    data class Success(val message: String) : CategoryOpResult()
    data class Error(val message: String) : CategoryOpResult()
}

class CategoriesViewModel(private val repository: AppRepository) : ViewModel() {

    val allCategories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(name: String, onResult: (CategoryOpResult) -> Unit = {}) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            onResult(CategoryOpResult.Error("يرجى إدخال اسم النوع"))
            return
        }

        // Check if a category with this exact name already exists (case-insensitive & trimmed)
        val existsInMemory = allCategories.value.any { it.name.trim().equals(trimmed, ignoreCase = true) }
        if (existsInMemory) {
            onResult(CategoryOpResult.Error("هذا النوع موجود بالفعل"))
            return
        }

        viewModelScope.launch {
            // Also check DB query just in case
            val existingInDb = repository.getCategoryByName(trimmed)
            if (existingInDb != null) {
                onResult(CategoryOpResult.Error("هذا النوع موجود بالفعل"))
                return@launch
            }

            // Append after existing ones in sortOrder (next available number)
            val maxSort = repository.getMaxCategorySortOrder()
            val nextSortOrder = if (maxSort > 0) maxSort + 1 else (allCategories.value.maxOfOrNull { it.sortOrder } ?: 0) + 1

            try {
                repository.insertCategory(
                    Category(
                        name = trimmed,
                        sortOrder = nextSortOrder,
                        isDefault = false
                    )
                )
                onResult(CategoryOpResult.Success("تم إضافة النوع بنجاح"))
            } catch (e: Exception) {
                // Rely on Room UNIQUE constraint as final safety net
                onResult(CategoryOpResult.Error("هذا النوع موجود بالفعل"))
            }
        }
    }

    fun updateCategory(category: Category, newName: String, onResult: (CategoryOpResult) -> Unit = {}) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) {
            onResult(CategoryOpResult.Error("يرجى إدخال اسم النوع"))
            return
        }

        if (trimmed == category.name.trim()) {
            onResult(CategoryOpResult.Success("تم تحديث النوع"))
            return
        }

        // Check if duplicate name exists on other categories
        val exists = allCategories.value.any { it.id != category.id && it.name.trim().equals(trimmed, ignoreCase = true) }
        if (exists) {
            onResult(CategoryOpResult.Error("هذا النوع موجود بالفعل"))
            return
        }

        viewModelScope.launch {
            val existingInDb = repository.getCategoryByName(trimmed)
            if (existingInDb != null && existingInDb.id != category.id) {
                onResult(CategoryOpResult.Error("هذا النوع موجود بالفعل"))
                return@launch
            }

            try {
                repository.updateCategory(category.copy(name = trimmed))
                onResult(CategoryOpResult.Success("تم تعديل اسم النوع بنجاح"))
            } catch (e: Exception) {
                onResult(CategoryOpResult.Error("هذا النوع موجود بالفعل"))
            }
        }
    }

    suspend fun canDeleteCategory(category: Category): Pair<Boolean, String?> {
        // Do not allow deleting any of the 15 default seeded categories at all
        if (category.isDefault || category.sortOrder in 1..15) {
            return Pair(false, "لا يمكن حذف الأنواع الافتراضية")
        }

        val orderCount = repository.getOrderCountForCategory(category.id)
        if (orderCount > 0) {
            return Pair(false, "لا يمكن حذف هذا النوع لوجود معاملات سابقة مرتبطة به")
        }

        return Pair(true, null)
    }

    fun deleteCategory(category: Category, onResult: (CategoryOpResult) -> Unit = {}) {
        viewModelScope.launch {
            val (canDelete, reason) = canDeleteCategory(category)
            if (!canDelete) {
                onResult(CategoryOpResult.Error(reason ?: "لا يمكن حذف هذا النوع"))
                return@launch
            }

            try {
                repository.deleteCategory(category)
                onResult(CategoryOpResult.Success("تم حذف النوع بنجاح"))
            } catch (e: Exception) {
                onResult(CategoryOpResult.Error("تعذر حذف النوع: ${e.message}"))
            }
        }
    }
}

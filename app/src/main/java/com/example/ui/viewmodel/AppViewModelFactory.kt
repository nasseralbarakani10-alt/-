package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.repository.AppRepository
import com.example.data.session.SessionManager

class AppViewModelFactory(
    private val repository: AppRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(repository, sessionManager) as T
            }
            modelClass.isAssignableFrom(OrdersViewModel::class.java) -> {
                OrdersViewModel(repository, sessionManager) as T
            }
            modelClass.isAssignableFrom(CategoriesViewModel::class.java) -> {
                CategoriesViewModel(repository) as T
            }
            modelClass.isAssignableFrom(CuttersViewModel::class.java) -> {
                CuttersViewModel(repository, sessionManager) as T
            }
            modelClass.isAssignableFrom(TailorsViewModel::class.java) -> {
                TailorsViewModel(repository, sessionManager) as T
            }
            modelClass.isAssignableFrom(UsersViewModel::class.java) -> {
                UsersViewModel(repository, sessionManager) as T
            }
            modelClass.isAssignableFrom(DevicesViewModel::class.java) -> {
                DevicesViewModel(repository) as T
            }
            modelClass.isAssignableFrom(MessagingViewModel::class.java) -> {
                MessagingViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

package com.example.data.session

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.User
import com.example.data.model.UserPermissions
import com.example.data.repository.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SessionManager(
    context: Context,
    private val repository: AppRepository,
    private val scope: CoroutineScope
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("trend_app_session", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _currentPermissions = MutableStateFlow<UserPermissions?>(null)
    val currentPermissions: StateFlow<UserPermissions?> = _currentPermissions.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var observationJobs: List<Job> = emptyList()

    init {
        restoreSession()
    }

    private fun restoreSession() {
        scope.launch(Dispatchers.IO) {
            val savedUserId = prefs.getLong(KEY_USER_ID, -1L)
            if (savedUserId != -1L) {
                val user = repository.getUserById(savedUserId)
                if (user != null && user.isActive) {
                    val perms = repository.getPermissionsForUser(savedUserId)
                        ?: if (user.isAdmin) UserPermissions.allEnabled(user.id) else UserPermissions.defaultNonAdmin(user.id)

                    _currentUser.value = user
                    _currentPermissions.value = perms
                    _isLoggedIn.value = true
                    startObserving(user.id)
                } else {
                    prefs.edit().remove(KEY_USER_ID).apply()
                }
            }
            _isLoading.value = false
        }
    }

    fun setSession(user: User, permissions: UserPermissions) {
        prefs.edit().putLong(KEY_USER_ID, user.id).apply()
        _currentUser.value = user
        _currentPermissions.value = permissions
        _isLoggedIn.value = true
        startObserving(user.id)
    }

    private fun startObserving(userId: Long) {
        observationJobs.forEach { it.cancel() }
        val job1 = scope.launch(Dispatchers.IO) {
            repository.getUserByIdFlow(userId).collect { updatedUser ->
                if (updatedUser == null || !updatedUser.isActive) {
                    logout()
                } else {
                    _currentUser.value = updatedUser
                }
            }
        }
        val job2 = scope.launch(Dispatchers.IO) {
            repository.getPermissionsForUserFlow(userId).collect { updatedPerms ->
                if (updatedPerms != null) {
                    _currentPermissions.value = updatedPerms
                }
            }
        }
        observationJobs = listOf(job1, job2)
    }

    fun logout() {
        observationJobs.forEach { it.cancel() }
        observationJobs = emptyList()
        prefs.edit().remove(KEY_USER_ID).apply()
        _currentUser.value = null
        _currentPermissions.value = null
        _isLoggedIn.value = false
    }

    companion object {
        private const val KEY_USER_ID = "logged_in_user_id"
    }
}

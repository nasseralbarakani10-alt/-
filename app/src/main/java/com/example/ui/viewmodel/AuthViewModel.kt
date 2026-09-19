package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.User
import com.example.data.model.UserPermissions
import com.example.data.model.UserWithPermissions
import com.example.data.repository.AppRepository
import com.example.data.security.PasswordHasher
import com.example.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
    object Success : AuthState()
}

class AuthViewModel(
    private val repository: AppRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val currentUser: StateFlow<User?> = sessionManager.currentUser
    val currentPermissions: StateFlow<UserPermissions?> = sessionManager.currentPermissions
    val isLoggedIn: StateFlow<Boolean> = sessionManager.isLoggedIn
    val isSessionLoading: StateFlow<Boolean> = sessionManager.isLoading

    val allUsersWithPermissions: StateFlow<List<UserWithPermissions>> = repository.allUsersWithPermissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    fun clearLoginState() {
        _loginState.value = AuthState.Idle
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun login(usernameInput: String, passwordInput: String) {
        val trimmedUsername = usernameInput.trim()
        val trimmedPassword = passwordInput.trim()

        if (trimmedUsername.isEmpty() || trimmedPassword.isEmpty()) {
            _loginState.value = AuthState.Error("يرجى إدخال اسم المستخدم وكلمة المرور")
            return
        }

        _loginState.value = AuthState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserByUsername(trimmedUsername)
            if (user == null) {
                withContext(Dispatchers.Main) {
                    _loginState.value = AuthState.Error("اسم المستخدم أو كلمة المرور غير صحيحة")
                }
                return@launch
            }

            if (!user.isActive) {
                withContext(Dispatchers.Main) {
                    _loginState.value = AuthState.Error("تم إيقاف هذا الحساب، يرجى مراجعة مدير النظام")
                }
                return@launch
            }

            val isPasswordValid = PasswordHasher.verifyPassword(
                password = trimmedPassword,
                salt = user.salt,
                expectedHash = user.passwordHash
            )

            if (!isPasswordValid) {
                withContext(Dispatchers.Main) {
                    _loginState.value = AuthState.Error("اسم المستخدم أو كلمة المرور غير صحيحة")
                }
                return@launch
            }

            val permissions = repository.getPermissionsForUser(user.id)
                ?: if (user.isAdmin) UserPermissions.allEnabled(user.id) else UserPermissions.defaultNonAdmin(user.id)

            withContext(Dispatchers.Main) {
                sessionManager.setSession(user, permissions)
                _loginState.value = AuthState.Success
            }
        }
    }

    fun logout() {
        sessionManager.logout()
        _loginState.value = AuthState.Idle
    }

    fun changePassword(
        currentPass: String,
        newPass: String,
        confirmPass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = currentUser.value
        if (user == null) {
            onError("لا يوجد مستخدم مسجل حالياً")
            return
        }

        if (currentPass.isBlank() || newPass.isBlank() || confirmPass.isBlank()) {
            onError("يرجى ملء جميع حقول كلمة المرور")
            return
        }

        if (newPass != confirmPass) {
            onError("كلمة المرور الجديدة غير متطابقة مع تأكيد كلمة المرور")
            return
        }

        if (newPass.length < 4) {
            onError("يجب أن تتكون كلمة المرور من 4 خانات على الأقل")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val isCurrentValid = PasswordHasher.verifyPassword(currentPass, user.salt, user.passwordHash)
            if (!isCurrentValid) {
                withContext(Dispatchers.Main) {
                    onError("كلمة المرور الحالية غير صحيحة")
                }
                return@launch
            }

            val newSalt = PasswordHasher.generateSalt()
            val newHash = PasswordHasher.hashPassword(newPass, newSalt)
            val updatedUser = user.copy(passwordHash = newHash, salt = newSalt)

            repository.updateUser(updatedUser)

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    fun createUser(
        username: String,
        password: String,
        isAdmin: Boolean,
        permissions: UserPermissions,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Enforce admin permission at logic level
        if (currentUser.value?.isAdmin != true) {
            onError("غير مسموح إلا لمدير النظام بإضافة مستخدمين")
            return
        }

        val trimmedUsername = username.trim()
        val trimmedPassword = password.trim()

        if (trimmedUsername.isBlank()) {
            onError("يرجى إدخال اسم المستخدم")
            return
        }

        if (trimmedPassword.length < 4) {
            onError("يجب أن تتكون كلمة المرور من 4 خانات على الأقل")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getUserByUsername(trimmedUsername)
            if (existing != null) {
                withContext(Dispatchers.Main) {
                    onError("اسم المستخدم موجود بالفعل، يرجى اختيار اسم آخر")
                }
                return@launch
            }

            val salt = PasswordHasher.generateSalt()
            val hash = PasswordHasher.hashPassword(trimmedPassword, salt)
            val newUser = User(
                username = trimmedUsername,
                passwordHash = hash,
                salt = salt,
                isAdmin = isAdmin,
                isActive = true,
                createdAt = System.currentTimeMillis()
            )

            val insertedId = repository.insertUser(newUser)
            if (insertedId > 0) {
                val permsToInsert = if (isAdmin) {
                    UserPermissions.allEnabled(insertedId)
                } else {
                    permissions.copy(userId = insertedId)
                }
                repository.insertOrUpdatePermissions(permsToInsert)
            }

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    fun updateUserPermissions(
        targetUserId: Long,
        isAdmin: Boolean,
        permissions: UserPermissions,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (currentUser.value?.isAdmin != true) {
            onError("غير مسموح إلا لمدير النظام بتعديل الصلاحيات")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserById(targetUserId)
            if (user == null) {
                withContext(Dispatchers.Main) {
                    onError("المستخدم غير موجود")
                }
                return@launch
            }

            val updatedUser = user.copy(isAdmin = isAdmin)
            repository.updateUser(updatedUser)

            val effectivePerms = if (isAdmin) {
                UserPermissions.allEnabled(targetUserId)
            } else {
                permissions.copy(userId = targetUserId)
            }
            repository.insertOrUpdatePermissions(effectivePerms)

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    fun toggleUserActiveStatus(
        targetUser: User,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (currentUser.value?.isAdmin != true) {
            onError("غير مسموح إلا لمدير النظام بتعليق أو تفعيل المستخدمين")
            return
        }

        if (targetUser.id == currentUser.value?.id) {
            onError("لا يمكنك إيقاف حسابك الحالي بنفسك")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val updated = targetUser.copy(isActive = !targetUser.isActive)
            repository.updateUser(updated)
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }
}

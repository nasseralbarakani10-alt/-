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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UsersViewModel(
    private val repository: AppRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // Current logged-in user session
    val currentUser: StateFlow<User?> = sessionManager.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // List of all users from Room (Flow converted to StateFlow)
    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // List of all users with their permissions
    val allUsersWithPermissions: StateFlow<List<UserWithPermissions>> = repository.allUsersWithPermissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------------------------------------------------------------------------------------------
    // Add User Functions
    // ---------------------------------------------------------------------------------------------

    /**
     * Add a user directly with optional permissions (following standard Cutters/Tailors pattern)
     */
    fun addUser(
        user: User,
        permissions: UserPermissions? = null,
        onComplete: (Long) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = repository.insertUser(user)
            if (userId > 0) {
                val perms = permissions?.copy(userId = userId)
                    ?: if (user.isAdmin) UserPermissions.allEnabled(userId)
                    else UserPermissions.defaultNonAdmin(userId)
                repository.insertOrUpdatePermissions(perms)
            }
            withContext(Dispatchers.Main) {
                onComplete(userId)
            }
        }
    }

    /**
     * Add a user with username, plain password and admin flag
     */
    fun addUser(
        username: String,
        password: String,
        isAdmin: Boolean = false,
        onComplete: (Long) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val trimmedName = username.trim()
            val salt = PasswordHasher.generateSalt()
            val hash = PasswordHasher.hashPassword(password.trim(), salt)
            val newUser = User(
                username = trimmedName,
                passwordHash = hash,
                salt = salt,
                isAdmin = isAdmin,
                isActive = true,
                createdAt = System.currentTimeMillis()
            )
            val userId = repository.insertUser(newUser)
            if (userId > 0) {
                val perms = if (isAdmin) UserPermissions.allEnabled(userId) else UserPermissions.defaultNonAdmin(userId)
                repository.insertOrUpdatePermissions(perms)
            }
            withContext(Dispatchers.Main) {
                onComplete(userId)
            }
        }
    }

    /**
     * Create user with validation and custom permissions as used in UserManagementScreen
     */
    fun createUser(
        username: String,
        password: String,
        confirmPassword: String,
        isAdmin: Boolean,
        canAccessReports: Boolean,
        canAccessSettings: Boolean,
        canEdit: Boolean,
        canDelete: Boolean,
        canChangeReadyStatus: Boolean,
        canAccessReportsRecent: Boolean = true,
        canAccessReportsStatement: Boolean = true,
        canAccessReportsCustomSearch: Boolean = true,
        canAccessReportsDaily: Boolean = true,
        canAccessReportsMonthly: Boolean = true,
        canAccessReportsYearly: Boolean = true,
        canAccessCutterReports: Boolean = true,
        canAccessTailorReports: Boolean = true,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (currentUser.value?.isAdmin != true) {
            onError("غير مسموح إلا لمدير النظام بإضافة مستخدمين")
            return
        }

        val trimmedUsername = username.trim()
        val trimmedPassword = password.trim()
        val trimmedConfirm = confirmPassword.trim()

        if (trimmedUsername.isBlank()) {
            onError("يرجى إدخال اسم المستخدم")
            return
        }

        if (trimmedPassword.isEmpty()) {
            onError("يرجى إدخال كلمة المرور")
            return
        }

        if (trimmedPassword != trimmedConfirm) {
            onError("كلمة المرور غير متطابقة مع تأكيد كلمة المرور")
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
                createdAt = System.currentTimeMillis(),
                canAccessReportsRecent = canAccessReportsRecent,
                canAccessReportsStatement = canAccessReportsStatement,
                canAccessReportsCustomSearch = canAccessReportsCustomSearch,
                canAccessReportsDaily = canAccessReportsDaily,
                canAccessReportsMonthly = canAccessReportsMonthly,
                canAccessReportsYearly = canAccessReportsYearly,
                canAccessCutterReports = canAccessCutterReports,
                canAccessTailorReports = canAccessTailorReports
            )

            val insertedId = repository.insertUser(newUser)
            if (insertedId > 0) {
                val permsToInsert = if (isAdmin) {
                    UserPermissions.allEnabled(insertedId)
                } else {
                    UserPermissions(
                        userId = insertedId,
                        canAccessReports = canAccessReports,
                        canAccessReportsRecent = canAccessReportsRecent,
                        canAccessReportsStatement = canAccessReportsStatement,
                        canAccessReportsCustomSearch = canAccessReportsCustomSearch,
                        canAccessReportsDaily = canAccessReportsDaily,
                        canAccessReportsMonthly = canAccessReportsMonthly,
                        canAccessReportsYearly = canAccessReportsYearly,
                        canAccessCutterReports = canAccessCutterReports,
                        canAccessTailorReports = canAccessTailorReports,
                        canAccessSettings = canAccessSettings,
                        canEdit = canEdit,
                        canDelete = canDelete,
                        canChangeReadyStatus = canChangeReadyStatus
                    )
                }
                repository.insertOrUpdatePermissions(permsToInsert)
            }

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Toggle Active Status Functions
    // ---------------------------------------------------------------------------------------------

    /**
     * Toggle or set active status of a user (following standard Cutters/Tailors pattern)
     */
    fun toggleUserActive(user: User, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUser(user.copy(isActive = !user.isActive))
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun setUserStopped(user: User, isStopped: Boolean, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUser(user.copy(isActive = !isStopped))
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    /**
     * Set user active status with authorization checks as used in UserManagementScreen
     */
    fun setUserActive(
        targetUser: User,
        isActive: Boolean,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (currentUser.value?.isAdmin != true) {
            onError("غير مسموح إلا لمدير النظام بتغيير حالة الحساب")
            return
        }

        if (targetUser.id == currentUser.value?.id && !isActive) {
            onError("لا يمكنك إيقاف حسابك الحالي بنفسك")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val updated = targetUser.copy(isActive = isActive)
            repository.updateUser(updated)
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Edit Permissions Functions
    // ---------------------------------------------------------------------------------------------

    /**
     * Edit/update permissions directly for a user
     */
    fun editPermissions(
        userId: Long,
        permissions: UserPermissions,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertOrUpdatePermissions(permissions.copy(userId = userId))
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun updatePermissions(
        permissions: UserPermissions,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertOrUpdatePermissions(permissions)
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    /**
     * Update user permissions with admin check and field breakdown as used in UserManagementScreen
     */
    fun updateUserPermissions(
        targetUserId: Long,
        isAdmin: Boolean,
        canAccessReports: Boolean,
        canAccessSettings: Boolean,
        canEdit: Boolean,
        canDelete: Boolean,
        canChangeReadyStatus: Boolean,
        canAccessReportsRecent: Boolean = true,
        canAccessReportsStatement: Boolean = true,
        canAccessReportsCustomSearch: Boolean = true,
        canAccessReportsDaily: Boolean = true,
        canAccessReportsMonthly: Boolean = true,
        canAccessReportsYearly: Boolean = true,
        canAccessCutterReports: Boolean = true,
        canAccessTailorReports: Boolean = true,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
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

            val updatedUser = user.copy(
                isAdmin = isAdmin,
                canAccessReportsRecent = if (isAdmin) true else canAccessReportsRecent,
                canAccessReportsStatement = if (isAdmin) true else canAccessReportsStatement,
                canAccessReportsCustomSearch = if (isAdmin) true else canAccessReportsCustomSearch,
                canAccessReportsDaily = if (isAdmin) true else canAccessReportsDaily,
                canAccessReportsMonthly = if (isAdmin) true else canAccessReportsMonthly,
                canAccessReportsYearly = if (isAdmin) true else canAccessReportsYearly,
                canAccessCutterReports = if (isAdmin) true else canAccessCutterReports,
                canAccessTailorReports = if (isAdmin) true else canAccessTailorReports
            )
            repository.updateUser(updatedUser)

            val existingPerms = repository.getPermissionsForUser(targetUserId)
            val effectivePerms = if (isAdmin) {
                UserPermissions.allEnabled(targetUserId)
            } else {
                (existingPerms ?: UserPermissions.defaultNonAdmin(targetUserId)).copy(
                    userId = targetUserId,
                    canAccessReports = canAccessReports,
                    canAccessReportsRecent = canAccessReportsRecent,
                    canAccessReportsStatement = canAccessReportsStatement,
                    canAccessReportsCustomSearch = canAccessReportsCustomSearch,
                    canAccessReportsDaily = canAccessReportsDaily,
                    canAccessReportsMonthly = canAccessReportsMonthly,
                    canAccessReportsYearly = canAccessReportsYearly,
                    canAccessCutterReports = canAccessCutterReports,
                    canAccessTailorReports = canAccessTailorReports,
                    canAccessSettings = canAccessSettings,
                    canEdit = canEdit,
                    canDelete = canDelete,
                    canChangeReadyStatus = canChangeReadyStatus
                )
            }
            repository.insertOrUpdatePermissions(effectivePerms)

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // General Update and Delete Operations
    // ---------------------------------------------------------------------------------------------

    fun updateUser(user: User, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUser(user)
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun deleteUser(
        user: User,
        onComplete: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (currentUser.value?.isAdmin != true) {
            onError("غير مسموح إلا لمدير النظام بحذف المستخدمين")
            return
        }
        if (user.id == currentUser.value?.id) {
            onError("لا يمكنك حذف حسابك الحالي")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteUser(user)
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
}

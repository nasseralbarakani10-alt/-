package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.model.UserPermissions
import com.example.data.model.UserWithPermissions
import com.example.ui.theme.BluePrimary
import com.example.ui.viewmodel.UsersViewModel
import kotlinx.coroutines.launch

@Composable
fun UserManagementScreen(
    usersViewModel: UsersViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by usersViewModel.currentUser.collectAsState()
    val allUsersWithPerms by usersViewModel.allUsersWithPermissions.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showAddUserDialog by remember { mutableStateOf(false) }
    var editingUserWithPerms by remember { mutableStateOf<UserWithPermissions?>(null) }
    var userToToggleStatus by remember { mutableStateOf<User?>(null) }

    // Enforce admin permission at the screen / logic level (enforced in navigation too)
    if (currentUser?.isAdmin != true) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "غير مصرح لك بالدخول",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "صفحة إدارة المستخدمين مخصصة لمدير النظام فقط.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B))
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("العودة", color = Color(0xFF000000), fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddUserDialog = true },
                containerColor = Color(0xFF16A34A),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_user_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة مستخدم")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(paddingValues)
        ) {
            // Header
            Surface(
                color = BluePrimary,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("user_mgmt_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع",
                            tint = Color(0xFF000000)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "إدارة المستخدمين",
                        color = Color(0xFF000000),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                }
            }

            // User List
            if (allUsersWithPerms.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا يوجد مستخدمين مسجلين",
                        color = Color(0xFF64748B)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allUsersWithPerms, key = { it.user.id }) { item ->
                        UserCardItem(
                            userWithPerms = item,
                            isSelf = item.user.id == currentUser?.id,
                            onEditPermissions = { editingUserWithPerms = item },
                            onToggleActive = { active ->
                                usersViewModel.setUserActive(
                                    targetUser = item.user,
                                    isActive = active,
                                    onSuccess = {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                if (active) "تم تفعيل الحساب" else "تم إيقاف الحساب"
                                            )
                                        }
                                    },
                                    onError = { error ->
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(error)
                                        }
                                    }
                                )
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    // Add User Dialog
    if (showAddUserDialog) {
        AddUserDialog(
            onDismiss = { showAddUserDialog = false },
            onConfirm = { username, password, confirmPassword, isAdmin, canAccessReports, canAccessSettings, canEdit, canDelete, canChangeReadyStatus,
                          canAccessReportsRecent, canAccessReportsStatement, canAccessReportsCustomSearch, canAccessReportsDaily, canAccessReportsMonthly, canAccessReportsYearly, canAccessCutterReports, canAccessTailorReports ->
                usersViewModel.createUser(
                    username = username,
                    password = password,
                    confirmPassword = confirmPassword,
                    isAdmin = isAdmin,
                    canAccessReports = canAccessReports,
                    canAccessSettings = canAccessSettings,
                    canEdit = canEdit,
                    canDelete = canDelete,
                    canChangeReadyStatus = canChangeReadyStatus,
                    canAccessReportsRecent = canAccessReportsRecent,
                    canAccessReportsStatement = canAccessReportsStatement,
                    canAccessReportsCustomSearch = canAccessReportsCustomSearch,
                    canAccessReportsDaily = canAccessReportsDaily,
                    canAccessReportsMonthly = canAccessReportsMonthly,
                    canAccessReportsYearly = canAccessReportsYearly,
                    canAccessCutterReports = canAccessCutterReports,
                    canAccessTailorReports = canAccessTailorReports,
                    onSuccess = {
                        showAddUserDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("تمت إضافة المستخدم بنجاح")
                        }
                    },
                    onError = { error ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(error)
                        }
                    }
                )
            }
        )
    }

    // Edit Permissions Dialog
    editingUserWithPerms?.let { item ->
        EditUserPermissionsDialog(
            userWithPerms = item,
            onDismiss = { editingUserWithPerms = null },
            onConfirm = { isAdmin, canAccessReports, canAccessSettings, canEdit, canDelete, canChangeReadyStatus,
                          canAccessReportsRecent, canAccessReportsStatement, canAccessReportsCustomSearch,
                          canAccessReportsDaily, canAccessReportsMonthly, canAccessReportsYearly,
                          canAccessCutterReports, canAccessTailorReports ->
                usersViewModel.updateUserPermissions(
                    targetUserId = item.user.id,
                    isAdmin = isAdmin,
                    canAccessReports = canAccessReports,
                    canAccessSettings = canAccessSettings,
                    canEdit = canEdit,
                    canDelete = canDelete,
                    canChangeReadyStatus = canChangeReadyStatus,
                    canAccessReportsRecent = canAccessReportsRecent,
                    canAccessReportsStatement = canAccessReportsStatement,
                    canAccessReportsCustomSearch = canAccessReportsCustomSearch,
                    canAccessReportsDaily = canAccessReportsDaily,
                    canAccessReportsMonthly = canAccessReportsMonthly,
                    canAccessReportsYearly = canAccessReportsYearly,
                    canAccessCutterReports = canAccessCutterReports,
                    canAccessTailorReports = canAccessTailorReports,
                    onSuccess = {
                        editingUserWithPerms = null
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("تم تحديث الصلاحيات بنجاح")
                        }
                    },
                    onError = { error ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(error)
                        }
                    }
                )
            }
        )
    }
}

@Composable
fun UserCardItem(
    userWithPerms: UserWithPermissions,
    isSelf: Boolean,
    onEditPermissions: () -> Unit,
    onToggleActive: (Boolean) -> Unit
) {
    val user = userWithPerms.user
    val isActive = user.isActive

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("user_card_${user.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color.White else Color(0xFFF8FAFC)
        ),
        border = BorderStroke(
            1.dp,
            if (isActive) Color(0xFFE2E8F0) else Color(0xFFCBD5E1)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (user.isAdmin) Color(0xFFE0F2FE) else Color(0xFFF1F5F9),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (user.isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                contentDescription = null,
                                tint = if (user.isAdmin) BluePrimary else Color(0xFF64748B),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.username,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = if (isActive) Color(0xFF0F172A) else Color(0xFF64748B)
                            )
                            if (isSelf) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFE2E8F0)
                                ) {
                                    Text(
                                        text = "(أنت)",
                                        fontSize = 11.sp,
                                        color = Color(0xFF475569),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Admin Badge if isAdmin
                            if (user.isAdmin) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFDBEAFE)
                                ) {
                                    Text(
                                        text = "مدير",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1D4ED8),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Active / Suspended Indicator
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isActive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                            ) {
                                Text(
                                    text = if (isActive) "نشط" else "موقوف",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isActive) Color(0xFF15803D) else Color(0xFFB91C1C),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Actions: Toggle to suspend/reactivate + Edit permissions button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEditPermissions,
                        modifier = Modifier.testTag("edit_user_${user.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل الصلاحيات",
                            tint = BluePrimary
                        )
                    }

                    // Active Toggle Switch / Checkbox
                    Switch(
                        checked = isActive,
                        onCheckedChange = { onToggleActive(it) },
                        enabled = !isSelf,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF16A34A),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFEF4444)
                        ),
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .testTag("toggle_active_${user.id}")
                    )
                }
            }
        }
    }
}

@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        username: String,
        password: String,
        confirmPassword: String,
        isAdmin: Boolean,
        canAccessReports: Boolean,
        canAccessSettings: Boolean,
        canEdit: Boolean,
        canDelete: Boolean,
        canChangeReadyStatus: Boolean,
        canAccessReportsRecent: Boolean,
        canAccessReportsStatement: Boolean,
        canAccessReportsCustomSearch: Boolean,
        canAccessReportsDaily: Boolean,
        canAccessReportsMonthly: Boolean,
        canAccessReportsYearly: Boolean,
        canAccessCutterReports: Boolean,
        canAccessTailorReports: Boolean
    ) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isAdmin by remember { mutableStateOf(false) }

    // Permissions default to true for normal user as requested
    var canAccessReports by remember { mutableStateOf(true) }
    var canAccessSettings by remember { mutableStateOf(true) }
    var canEdit by remember { mutableStateOf(true) }
    var canDelete by remember { mutableStateOf(true) }
    var canChangeReadyStatus by remember { mutableStateOf(true) }

    var canAccessReportsRecent by remember { mutableStateOf(true) }
    var canAccessReportsStatement by remember { mutableStateOf(true) }
    var canAccessReportsCustomSearch by remember { mutableStateOf(true) }
    var canAccessReportsDaily by remember { mutableStateOf(true) }
    var canAccessReportsMonthly by remember { mutableStateOf(true) }
    var canAccessReportsYearly by remember { mutableStateOf(true) }
    var canAccessCutterReports by remember { mutableStateOf(true) }
    var canAccessTailorReports by remember { mutableStateOf(true) }

    var localError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إضافة مستخدم جديد",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (localError != null) {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = localError ?: "",
                            color = Color(0xFFDC2626),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        localError = null
                    },
                    label = { Text("اسم المستخدم") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_username_input")
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        localError = null
                    },
                    label = { Text("كلمة المرور") },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_password_input")
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        localError = null
                    },
                    label = { Text("تأكيد كلمة المرور") },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_confirm_password_input")
                )

                // Admin Switch
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "حساب مدير نظام (كامل الصلاحيات)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "المدير يمتلك جميع الصلاحيات دون قيود",
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Switch(
                            checked = isAdmin,
                            onCheckedChange = { isAdmin = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = BluePrimary)
                        )
                    }
                }

                if (!isAdmin) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "صلاحيات المستخدم:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1E293B)
                    )

                    PermissionCheckboxItem(
                        label = "الوصول العام للتقارير (canAccessReports)",
                        checked = canAccessReports,
                        onCheckedChange = { canAccessReports = it }
                    )

                    // Sub-report permissions
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "صلاحيات أقسام التقارير التفصيلية:",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569)
                            )
                            PermissionCheckboxItem(
                                label = "تقرير آخر العمليات (canAccessReportsRecent)",
                                checked = canAccessReportsRecent,
                                onCheckedChange = { canAccessReportsRecent = it }
                            )
                            PermissionCheckboxItem(
                                label = "كشف حساب عام (canAccessReportsStatement)",
                                checked = canAccessReportsStatement,
                                onCheckedChange = { canAccessReportsStatement = it }
                            )
                            PermissionCheckboxItem(
                                label = "بحث مخصص في التقارير (canAccessReportsCustomSearch)",
                                checked = canAccessReportsCustomSearch,
                                onCheckedChange = { canAccessReportsCustomSearch = it }
                            )
                            PermissionCheckboxItem(
                                label = "كشف حساب يومي (canAccessReportsDaily)",
                                checked = canAccessReportsDaily,
                                onCheckedChange = { canAccessReportsDaily = it }
                            )
                            PermissionCheckboxItem(
                                label = "كشف حساب شهري (canAccessReportsMonthly)",
                                checked = canAccessReportsMonthly,
                                onCheckedChange = { canAccessReportsMonthly = it }
                            )
                            PermissionCheckboxItem(
                                label = "كشف حساب سنوي (canAccessReportsYearly)",
                                checked = canAccessReportsYearly,
                                onCheckedChange = { canAccessReportsYearly = it }
                            )
                            PermissionCheckboxItem(
                                label = "تقارير القصاصين (canAccessCutterReports)",
                                checked = canAccessCutterReports,
                                onCheckedChange = { canAccessCutterReports = it }
                            )
                            PermissionCheckboxItem(
                                label = "تقارير الخياطين (canAccessTailorReports)",
                                checked = canAccessTailorReports,
                                onCheckedChange = { canAccessTailorReports = it }
                            )
                        }
                    }

                    PermissionCheckboxItem(
                        label = "الوصول للإعدادات (canAccessSettings)",
                        checked = canAccessSettings,
                        onCheckedChange = { canAccessSettings = it }
                    )

                    PermissionCheckboxItem(
                        label = "تعديل العمليات والطلبات (canEdit)",
                        checked = canEdit,
                        onCheckedChange = { canEdit = it }
                    )

                    PermissionCheckboxItem(
                        label = "حذف العمليات والطلبات (canDelete)",
                        checked = canDelete,
                        onCheckedChange = { canDelete = it }
                    )

                    PermissionCheckboxItem(
                        label = "تغيير حالة الجاهزية (canChangeReadyStatus)",
                        checked = canChangeReadyStatus,
                        onCheckedChange = { canChangeReadyStatus = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (username.isBlank()) {
                        localError = "يرجى إدخال اسم المستخدم"
                        return@Button
                    }
                    if (password.isEmpty()) {
                        localError = "يرجى إدخال كلمة المرور"
                        return@Button
                    }
                    if (password != confirmPassword) {
                        localError = "كلمة المرور غير متطابقة مع تأكيد كلمة المرور"
                        return@Button
                    }
                    if (password.length < 4) {
                        localError = "يجب أن تتكون كلمة المرور من 4 خانات على الأقل"
                        return@Button
                    }
                    onConfirm(
                        username,
                        password,
                        confirmPassword,
                        isAdmin,
                        canAccessReports,
                        canAccessSettings,
                        canEdit,
                        canDelete,
                        canChangeReadyStatus,
                        canAccessReportsRecent,
                        canAccessReportsStatement,
                        canAccessReportsCustomSearch,
                        canAccessReportsDaily,
                        canAccessReportsMonthly,
                        canAccessReportsYearly,
                        canAccessCutterReports,
                        canAccessTailorReports
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                modifier = Modifier.testTag("confirm_add_user_button")
            ) {
                Text("حفظ المستخدم", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun EditUserPermissionsDialog(
    userWithPerms: UserWithPermissions,
    onDismiss: () -> Unit,
    onConfirm: (
        isAdmin: Boolean,
        canAccessReports: Boolean,
        canAccessSettings: Boolean,
        canEdit: Boolean,
        canDelete: Boolean,
        canChangeReadyStatus: Boolean,
        canAccessReportsRecent: Boolean,
        canAccessReportsStatement: Boolean,
        canAccessReportsCustomSearch: Boolean,
        canAccessReportsDaily: Boolean,
        canAccessReportsMonthly: Boolean,
        canAccessReportsYearly: Boolean,
        canAccessCutterReports: Boolean,
        canAccessTailorReports: Boolean
    ) -> Unit
) {
    var isAdmin by remember { mutableStateOf(userWithPerms.user.isAdmin) }
    val initialPerms = userWithPerms.permissions ?: UserPermissions.defaultNonAdmin(userWithPerms.user.id)

    var canAccessReports by remember { mutableStateOf(initialPerms.canAccessReports) }
    var canAccessSettings by remember { mutableStateOf(initialPerms.canAccessSettings) }
    var canEdit by remember { mutableStateOf(initialPerms.canEdit) }
    var canDelete by remember { mutableStateOf(initialPerms.canDelete) }
    var canChangeReadyStatus by remember { mutableStateOf(initialPerms.canChangeReadyStatus) }

    var canAccessReportsRecent by remember { mutableStateOf(initialPerms.canAccessReportsRecent) }
    var canAccessReportsStatement by remember { mutableStateOf(initialPerms.canAccessReportsStatement) }
    var canAccessReportsCustomSearch by remember { mutableStateOf(initialPerms.canAccessReportsCustomSearch) }
    var canAccessReportsDaily by remember { mutableStateOf(initialPerms.canAccessReportsDaily) }
    var canAccessReportsMonthly by remember { mutableStateOf(initialPerms.canAccessReportsMonthly) }
    var canAccessReportsYearly by remember { mutableStateOf(initialPerms.canAccessReportsYearly) }
    var canAccessCutterReports by remember { mutableStateOf(initialPerms.canAccessCutterReports) }
    var canAccessTailorReports by remember { mutableStateOf(initialPerms.canAccessTailorReports) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "صلاحيات: ${userWithPerms.user.username}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Admin Switch / Checkbox
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "حساب مدير نظام (isAdmin)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "المدير يمتلك جميع الصلاحيات دون قيود",
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Switch(
                            checked = isAdmin,
                            onCheckedChange = { isAdmin = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = BluePrimary)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = "صلاحيات المستخدم:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1E293B)
                )

                PermissionCheckboxItem(
                    label = "الوصول العام للتقارير (canAccessReports)",
                    checked = if (isAdmin) true else canAccessReports,
                    onCheckedChange = { if (!isAdmin) canAccessReports = it }
                )

                // Sub-report permissions
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "صلاحيات أقسام التقارير التفصيلية:",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                        PermissionCheckboxItem(
                            label = "تقرير آخر العمليات (canAccessReportsRecent)",
                            checked = if (isAdmin) true else canAccessReportsRecent,
                            onCheckedChange = { if (!isAdmin) canAccessReportsRecent = it }
                        )
                        PermissionCheckboxItem(
                            label = "كشف حساب عام (canAccessReportsStatement)",
                            checked = if (isAdmin) true else canAccessReportsStatement,
                            onCheckedChange = { if (!isAdmin) canAccessReportsStatement = it }
                        )
                        PermissionCheckboxItem(
                            label = "بحث مخصص في التقارير (canAccessReportsCustomSearch)",
                            checked = if (isAdmin) true else canAccessReportsCustomSearch,
                            onCheckedChange = { if (!isAdmin) canAccessReportsCustomSearch = it }
                        )
                        PermissionCheckboxItem(
                            label = "كشف حساب يومي (canAccessReportsDaily)",
                            checked = if (isAdmin) true else canAccessReportsDaily,
                            onCheckedChange = { if (!isAdmin) canAccessReportsDaily = it }
                        )
                        PermissionCheckboxItem(
                            label = "كشف حساب شهري (canAccessReportsMonthly)",
                            checked = if (isAdmin) true else canAccessReportsMonthly,
                            onCheckedChange = { if (!isAdmin) canAccessReportsMonthly = it }
                        )
                        PermissionCheckboxItem(
                            label = "كشف حساب سنوي (canAccessReportsYearly)",
                            checked = if (isAdmin) true else canAccessReportsYearly,
                            onCheckedChange = { if (!isAdmin) canAccessReportsYearly = it }
                        )
                        PermissionCheckboxItem(
                            label = "تقارير القصاصين (canAccessCutterReports)",
                            checked = if (isAdmin) true else canAccessCutterReports,
                            onCheckedChange = { if (!isAdmin) canAccessCutterReports = it }
                        )
                        PermissionCheckboxItem(
                            label = "تقارير الخياطين (canAccessTailorReports)",
                            checked = if (isAdmin) true else canAccessTailorReports,
                            onCheckedChange = { if (!isAdmin) canAccessTailorReports = it }
                        )
                    }
                }

                PermissionCheckboxItem(
                    label = "الوصول للإعدادات (canAccessSettings)",
                    checked = if (isAdmin) true else canAccessSettings,
                    onCheckedChange = { if (!isAdmin) canAccessSettings = it }
                )

                PermissionCheckboxItem(
                    label = "تعديل العمليات والطلبات (canEdit)",
                    checked = if (isAdmin) true else canEdit,
                    onCheckedChange = { if (!isAdmin) canEdit = it }
                )

                PermissionCheckboxItem(
                    label = "حذف العمليات والطلبات (canDelete)",
                    checked = if (isAdmin) true else canDelete,
                    onCheckedChange = { if (!isAdmin) canDelete = it }
                )

                PermissionCheckboxItem(
                    label = "تغيير حالة الجاهزية (canChangeReadyStatus)",
                    checked = if (isAdmin) true else canChangeReadyStatus,
                    onCheckedChange = { if (!isAdmin) canChangeReadyStatus = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        isAdmin,
                        canAccessReports,
                        canAccessSettings,
                        canEdit,
                        canDelete,
                        canChangeReadyStatus,
                        canAccessReportsRecent,
                        canAccessReportsStatement,
                        canAccessReportsCustomSearch,
                        canAccessReportsDaily,
                        canAccessReportsMonthly,
                        canAccessReportsYearly,
                        canAccessCutterReports,
                        canAccessTailorReports
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("حفظ التغييرات", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun PermissionCheckboxItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = BluePrimary)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF334155)
        )
    }
}

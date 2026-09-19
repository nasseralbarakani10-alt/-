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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
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
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun UserManagementScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val allUsersWithPerms by authViewModel.allUsersWithPermissions.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showAddUserDialog by remember { mutableStateOf(false) }
    var editingUserWithPerms by remember { mutableStateOf<UserWithPermissions?>(null) }
    var userToToggleStatus by remember { mutableStateOf<User?>(null) }

    // Enforce admin permission at the screen / logic level
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
                Text("العودة", color = Color.White)
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
                shadowElevation = 4.dp,
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
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "إدارة المستخدمين والصلاحيات",
                        color = Color.White,
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
                            onToggleStatus = { userToToggleStatus = item.user }
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
            onConfirm = { username, password, isAdmin, permissions ->
                authViewModel.createUser(
                    username = username,
                    password = password,
                    isAdmin = isAdmin,
                    permissions = permissions,
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
            onConfirm = { isAdmin, permissions ->
                authViewModel.updateUserPermissions(
                    targetUserId = item.user.id,
                    isAdmin = isAdmin,
                    permissions = permissions,
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

    // Toggle Suspend Status Dialog
    userToToggleStatus?.let { user ->
        val isSuspending = user.isActive
        AlertDialog(
            onDismissRequest = { userToToggleStatus = null },
            title = {
                Text(
                    text = if (isSuspending) "إيقاف الحساب" else "تفعيل الحساب",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isSuspending) {
                        "هل أنت متأكد من إيقاف حساب المستخدم (${user.username})؟ لن يتمكن من تسجيل الدخول حتى تتم إعادة تفعيله."
                    } else {
                        "هل تريد إعادة تفعيل حساب المستخدم (${user.username}) والسماح له بتسجيل الدخول؟"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.toggleUserActiveStatus(
                            targetUser = user,
                            onSuccess = {
                                userToToggleStatus = null
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (isSuspending) "تم إيقاف الحساب" else "تم تفعيل الحساب"
                                    )
                                }
                            },
                            onError = { error ->
                                userToToggleStatus = null
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(error)
                                }
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSuspending) Color(0xFFDC2626) else Color(0xFF16A34A)
                    )
                ) {
                    Text(if (isSuspending) "إيقاف الحساب" else "تفعيل الحساب", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToToggleStatus = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun UserCardItem(
    userWithPerms: UserWithPermissions,
    isSelf: Boolean,
    onEditPermissions: () -> Unit,
    onToggleStatus: () -> Unit
) {
    val user = userWithPerms.user
    val isActive = user.isActive

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("user_card_${user.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color.White else Color(0xFFF1F5F9)
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
                            // Admin / Worker Badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (user.isAdmin) Color(0xFFDBEAFE) else Color(0xFFF3F4F6)
                            ) {
                                Text(
                                    text = if (user.isAdmin) "مدير نظام" else "مستخدم عادي",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (user.isAdmin) Color(0xFF1D4ED8) else Color(0xFF4B5563),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            // Active / Suspended Badge
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

                // Actions
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

                    if (!isSelf) {
                        IconButton(
                            onClick = onToggleStatus,
                            modifier = Modifier.testTag("toggle_status_user_${user.id}")
                        ) {
                            Icon(
                                imageVector = if (isActive) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = if (isActive) "إيقاف الحساب" else "تفعيل الحساب",
                                tint = if (isActive) Color(0xFFEF4444) else Color(0xFF16A34A)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onConfirm: (username: String, password: String, isAdmin: Boolean, permissions: UserPermissions) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isAdmin by remember { mutableStateOf(false) }

    // Permissions toggles
    var canAccessReports by remember { mutableStateOf(false) }
    var canAccessReportsRecent by remember { mutableStateOf(false) }
    var canAccessReportsStatement by remember { mutableStateOf(false) }
    var canAccessReportsCustomSearch by remember { mutableStateOf(false) }
    var canAccessReportsDaily by remember { mutableStateOf(false) }
    var canAccessReportsMonthly by remember { mutableStateOf(false) }
    var canAccessReportsYearly by remember { mutableStateOf(false) }
    var canAccessSettings by remember { mutableStateOf(false) }
    var canEdit by remember { mutableStateOf(false) }
    var canDelete by remember { mutableStateOf(false) }
    var canChangeReadyStatus by remember { mutableStateOf(false) }

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
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("اسم المستخدم") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_username_input")
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
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
                        label = "الوصول لتبويب التقارير",
                        checked = canAccessReports,
                        onCheckedChange = { canAccessReports = it }
                    )
                    if (canAccessReports) {
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            PermissionCheckboxItem(
                                label = "تقرير آخر العمليات",
                                checked = canAccessReportsRecent,
                                onCheckedChange = { canAccessReportsRecent = it }
                            )
                            PermissionCheckboxItem(
                                label = "تقرير كشف حساب",
                                checked = canAccessReportsStatement,
                                onCheckedChange = { canAccessReportsStatement = it }
                            )
                            PermissionCheckboxItem(
                                label = "بحث مخصص",
                                checked = canAccessReportsCustomSearch,
                                onCheckedChange = { canAccessReportsCustomSearch = it }
                            )
                            PermissionCheckboxItem(
                                label = "التقرير اليومي",
                                checked = canAccessReportsDaily,
                                onCheckedChange = { canAccessReportsDaily = it }
                            )
                            PermissionCheckboxItem(
                                label = "التقرير الشهري",
                                checked = canAccessReportsMonthly,
                                onCheckedChange = { canAccessReportsMonthly = it }
                            )
                            PermissionCheckboxItem(
                                label = "التقرير السنوي",
                                checked = canAccessReportsYearly,
                                onCheckedChange = { canAccessReportsYearly = it }
                            )
                        }
                    }

                    PermissionCheckboxItem(
                        label = "الوصول لتبويب الإعدادات",
                        checked = canAccessSettings,
                        onCheckedChange = { canAccessSettings = it }
                    )

                    PermissionCheckboxItem(
                        label = "تعديل العمليات والطلبات",
                        checked = canEdit,
                        onCheckedChange = { canEdit = it }
                    )

                    PermissionCheckboxItem(
                        label = "حذف العمليات والطلبات",
                        checked = canDelete,
                        onCheckedChange = { canDelete = it }
                    )

                    PermissionCheckboxItem(
                        label = "تغيير حالة الجاهزية (جاهز)",
                        checked = canChangeReadyStatus,
                        onCheckedChange = { canChangeReadyStatus = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val permissions = if (isAdmin) {
                        UserPermissions.allEnabled(0)
                    } else {
                        UserPermissions(
                            userId = 0,
                            canAccessReports = canAccessReports,
                            canAccessReportsRecent = canAccessReportsRecent,
                            canAccessReportsStatement = canAccessReportsStatement,
                            canAccessReportsCustomSearch = canAccessReportsCustomSearch,
                            canAccessReportsDaily = canAccessReportsDaily,
                            canAccessReportsMonthly = canAccessReportsMonthly,
                            canAccessReportsYearly = canAccessReportsYearly,
                            canAccessSettings = canAccessSettings,
                            canEdit = canEdit,
                            canDelete = canDelete,
                            canChangeReadyStatus = canChangeReadyStatus
                        )
                    }
                    onConfirm(username, password, isAdmin, permissions)
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
    onConfirm: (isAdmin: Boolean, permissions: UserPermissions) -> Unit
) {
    var isAdmin by remember { mutableStateOf(userWithPerms.user.isAdmin) }
    val initialPerms = userWithPerms.permissions ?: UserPermissions.defaultNonAdmin(userWithPerms.user.id)

    var canAccessReports by remember { mutableStateOf(initialPerms.canAccessReports) }
    var canAccessReportsRecent by remember { mutableStateOf(initialPerms.canAccessReportsRecent) }
    var canAccessReportsStatement by remember { mutableStateOf(initialPerms.canAccessReportsStatement) }
    var canAccessReportsCustomSearch by remember { mutableStateOf(initialPerms.canAccessReportsCustomSearch) }
    var canAccessReportsDaily by remember { mutableStateOf(initialPerms.canAccessReportsDaily) }
    var canAccessReportsMonthly by remember { mutableStateOf(initialPerms.canAccessReportsMonthly) }
    var canAccessReportsYearly by remember { mutableStateOf(initialPerms.canAccessReportsYearly) }
    var canAccessSettings by remember { mutableStateOf(initialPerms.canAccessSettings) }
    var canEdit by remember { mutableStateOf(initialPerms.canEdit) }
    var canDelete by remember { mutableStateOf(initialPerms.canDelete) }
    var canChangeReadyStatus by remember { mutableStateOf(initialPerms.canChangeReadyStatus) }

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
                        label = "الوصول لتبويب التقارير",
                        checked = canAccessReports,
                        onCheckedChange = { canAccessReports = it }
                    )
                    if (canAccessReports) {
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            PermissionCheckboxItem(
                                label = "تقرير آخر العمليات",
                                checked = canAccessReportsRecent,
                                onCheckedChange = { canAccessReportsRecent = it }
                            )
                            PermissionCheckboxItem(
                                label = "تقرير كشف حساب",
                                checked = canAccessReportsStatement,
                                onCheckedChange = { canAccessReportsStatement = it }
                            )
                            PermissionCheckboxItem(
                                label = "بحث مخصص",
                                checked = canAccessReportsCustomSearch,
                                onCheckedChange = { canAccessReportsCustomSearch = it }
                            )
                            PermissionCheckboxItem(
                                label = "التقرير اليومي",
                                checked = canAccessReportsDaily,
                                onCheckedChange = { canAccessReportsDaily = it }
                            )
                            PermissionCheckboxItem(
                                label = "التقرير الشهري",
                                checked = canAccessReportsMonthly,
                                onCheckedChange = { canAccessReportsMonthly = it }
                            )
                            PermissionCheckboxItem(
                                label = "التقرير السنوي",
                                checked = canAccessReportsYearly,
                                onCheckedChange = { canAccessReportsYearly = it }
                            )
                        }
                    }

                    PermissionCheckboxItem(
                        label = "الوصول لتبويب الإعدادات",
                        checked = canAccessSettings,
                        onCheckedChange = { canAccessSettings = it }
                    )

                    PermissionCheckboxItem(
                        label = "تعديل العمليات والطلبات",
                        checked = canEdit,
                        onCheckedChange = { canEdit = it }
                    )

                    PermissionCheckboxItem(
                        label = "حذف العمليات والطلبات",
                        checked = canDelete,
                        onCheckedChange = { canDelete = it }
                    )

                    PermissionCheckboxItem(
                        label = "تغيير حالة الجاهزية (جاهز)",
                        checked = canChangeReadyStatus,
                        onCheckedChange = { canChangeReadyStatus = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val permissions = if (isAdmin) {
                        UserPermissions.allEnabled(userWithPerms.user.id)
                    } else {
                        UserPermissions(
                            userId = userWithPerms.user.id,
                            canAccessReports = canAccessReports,
                            canAccessReportsRecent = canAccessReportsRecent,
                            canAccessReportsStatement = canAccessReportsStatement,
                            canAccessReportsCustomSearch = canAccessReportsCustomSearch,
                            canAccessReportsDaily = canAccessReportsDaily,
                            canAccessReportsMonthly = canAccessReportsMonthly,
                            canAccessReportsYearly = canAccessReportsYearly,
                            canAccessSettings = canAccessSettings,
                            canEdit = canEdit,
                            canDelete = canDelete,
                            canChangeReadyStatus = canChangeReadyStatus
                        )
                    }
                    onConfirm(isAdmin, permissions)
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

package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BluePrimary
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.CategoriesViewModel
import com.example.ui.viewmodel.DevicesViewModel
import com.example.ui.viewmodel.MessagingViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.UsersViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    categoriesViewModel: CategoriesViewModel,
    authViewModel: AuthViewModel,
    usersViewModel: UsersViewModel? = null,
    devicesViewModel: DevicesViewModel? = null,
    messagingViewModel: MessagingViewModel? = null,
    settingsViewModel: SettingsViewModel? = null,
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val currentPermissions by authViewModel.currentPermissions.collectAsState()
    val canAccessSettings = currentUser?.isAdmin == true || currentPermissions?.canAccessSettings == true
    val appSettings by (settingsViewModel?.appSettings ?: remember {
        kotlinx.coroutines.flow.MutableStateFlow(com.example.data.model.AppSettings())
    }).collectAsState()
    val messagingSettings by (messagingViewModel?.settings ?: remember {
        kotlinx.coroutines.flow.MutableStateFlow(com.example.data.model.MessagingSettings())
    }).collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var editedShopPhone by remember(messagingSettings.shopPhoneNumber) {
        mutableStateOf(messagingSettings.shopPhoneNumber)
    }

    var showMessagingSettingsScreen by remember { mutableStateOf(false) }
    var showCategoriesManagement by remember { mutableStateOf(false) }
    var showUserManagement by remember { mutableStateOf(false) }
    var showLinkedDevicesScreen by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Navigation sub-screens:
    if (showMessagingSettingsScreen && canAccessSettings && messagingViewModel != null) {
        MessagingSettingsScreen(
            messagingViewModel = messagingViewModel,
            onBack = { showMessagingSettingsScreen = false },
            modifier = modifier
        )
    } else if (showLinkedDevicesScreen && devicesViewModel != null) {
        LinkedDevicesScreen(
            devicesViewModel = devicesViewModel,
            onBack = { showLinkedDevicesScreen = false },
            modifier = modifier
        )
    } else if (showUserManagement && currentUser?.isAdmin == true && usersViewModel != null) {
        UserManagementScreen(
            usersViewModel = usersViewModel,
            onBack = { showUserManagement = false },
            modifier = modifier
        )
    } else if (showCategoriesManagement) {
        CategoriesManagementScreen(
            categoriesViewModel = categoriesViewModel,
            onBack = { showCategoriesManagement = false },
            modifier = modifier
        )
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC))
                    .padding(paddingValues)
            ) {
                // App Bar Header
                Surface(
                    color = BluePrimary,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "الإعدادات",
                            color = Color(0xFF000000),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Current User Info Card
                    currentUser?.let { user ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFDCFCE7),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color(0xFF15803D),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = "المستخدم الحالي: ${user.username}",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = Color(0xFF14532D)
                                        )
                                    )
                                    Text(
                                        text = if (user.isAdmin) "حساب مدير نظام (كامل الصلاحيات)" else "حساب مستخدم عادي",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            color = Color(0xFF166534)
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 1. User Management (ADMIN ONLY)
                    if (currentUser?.isAdmin == true) {
                        SettingsOptionBar(
                            title = "إدارة المستخدمين",
                            subtitle = "إضافة وتعديل المستخدمين وتحديد الصلاحيات وإيقاف الحسابات",
                            icon = Icons.Default.AdminPanelSettings,
                            iconTint = BluePrimary,
                            iconBg = Color(0xFFE0F2FE),
                            testTag = "user_management_setting_card",
                            onClick = { showUserManagement = true }
                        )
                    }

                    // 2. Category Management
                    SettingsOptionBar(
                        title = "إدارة الأنواع",
                        subtitle = "عرض وتعديل وإضافة أنواع التفصيل الافتراضية والمخصصة",
                        icon = Icons.Default.Category,
                        iconTint = BluePrimary,
                        iconBg = Color(0xFFE0F2FE),
                        testTag = "manage_categories_setting_card",
                        onClick = { showCategoriesManagement = true }
                    )

                    // 2.1 Sort By Frequency Toggle
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                settingsViewModel?.setSortByFrequencyEnabled(!appSettings.sortByFrequencyEnabled)
                            }
                            .testTag("sort_by_frequency_setting_card"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFEFF6FF),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Sort,
                                            contentDescription = null,
                                            tint = BluePrimary,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = "ترتيب العناصر حسب الأكثر اختيارًا",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        ),
                                        color = Color(0xFF000000)
                                    )
                                    Text(
                                        text = if (appSettings.sortByFrequencyEnabled)
                                            "مفعل: ترتيب أنواع التفصيل والقصاصين والخياطين حسب الأكثر اختياراً"
                                        else
                                            "معطل: الترتيب الافتراضي للعناصر",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (appSettings.sortByFrequencyEnabled) BluePrimary else Color(0xFF64748B)
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }

                            Switch(
                                checked = appSettings.sortByFrequencyEnabled,
                                onCheckedChange = { isChecked ->
                                    settingsViewModel?.setSortByFrequencyEnabled(isChecked)
                                },
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("sort_by_frequency_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = BluePrimary,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFCBD5E1),
                                    uncheckedBorderColor = Color(0xFF94A3B8)
                                )
                            )
                        }
                    }

                    // 2.2 App Title Setting
                    var editedAppTitle by remember(appSettings.appTitle) { mutableStateOf(appSettings.appTitle) }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("app_title_setting_card"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFE0F2FE),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = BluePrimary,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "عنوان التطبيق",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        ),
                                        color = Color(0xFF000000)
                                    )
                                    Text(
                                        text = "تعديل نص الترويسة المعروض في أعلى الشاشة الرئيسية",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = editedAppTitle,
                                    onValueChange = { editedAppTitle = it },
                                    label = { Text("عنوان التطبيق") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("app_title_text_field"),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Button(
                                    onClick = {
                                        settingsViewModel?.setAppTitle(editedAppTitle)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("تم حفظ عنوان التطبيق بنجاح")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("save_app_title_button")
                                ) {
                                    Text("حفظ", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 3. Dedicated Messaging Section: قسم الرسائل (only for users with canAccessSettings)
                    if (canAccessSettings && messagingViewModel != null) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("messaging_section_card")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Header: قسم الرسائل
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color(0xFFEFF6FF), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Chat,
                                            contentDescription = null,
                                            tint = Color(0xFF2563EB),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "قسم الرسائل",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = Color(0xFF0F172A)
                                            )
                                        )
                                        Text(
                                            text = "إعدادات إرسال الرسائل وإشعارات التجهيز للمحل والعملاء",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 12.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        )
                                    }
                                }

                                HorizontalDivider(color = Color(0xFFE2E8F0))

                                // ==========================================
                                // SHOP MESSAGING (رسائل المحل)
                                // ==========================================
                                Text(
                                    text = "رسائل المحل",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E293B)
                                )

                                // رقم المحل + حفظ
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = editedShopPhone,
                                        onValueChange = { editedShopPhone = it },
                                        label = { Text("رقم المحل") },
                                        placeholder = { Text("أدخل رقم هاتف المحل") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("shop_phone_number_input"),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    Button(
                                        onClick = {
                                            messagingViewModel.setShopPhoneNumber(editedShopPhone) {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("تم حفظ رقم المحل بنجاح")
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("save_shop_phone_button")
                                    ) {
                                        Text("حفظ", fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Separate control beside رقم المحل: إيقاف إرسال الرسائل للمحل
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (messagingSettings.stopShopMessaging) Color(0xFFFEF2F2) else Color(0xFFF8FAFC))
                                        .border(
                                            1.dp,
                                            if (messagingSettings.stopShopMessaging) Color(0xFFFECACA) else Color(0xFFE2E8F0),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "إيقاف إرسال الرسائل للمحل",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = if (messagingSettings.stopShopMessaging)
                                                "مفعل: لن يتم إرسال رسائل أو إظهار تأكيد إرسال رسالة للمحل عند اكتمال الطلبات"
                                            else
                                                "معطل: سيتم إظهار تأكيد إرسال رسالة للمحل عند اكتمال كافة طلبات العميل",
                                            fontSize = 12.sp,
                                            color = if (messagingSettings.stopShopMessaging) Color(0xFFDC2626) else Color(0xFF64748B)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = messagingSettings.stopShopMessaging,
                                        onCheckedChange = { stop ->
                                            messagingViewModel.setStopShopMessaging(stop)
                                        },
                                        modifier = Modifier.testTag("stop_shop_messaging_switch"),
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFFDC2626),
                                            uncheckedThumbColor = Color.White,
                                            uncheckedTrackColor = Color(0xFFCBD5E1)
                                        )
                                    )
                                }

                                HorizontalDivider(color = Color(0xFFE2E8F0))

                                // ==========================================
                                // CUSTOMER MESSAGING (رسائل العملاء)
                                // ==========================================
                                Text(
                                    text = "رسائل العملاء",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E293B)
                                )

                                // إيقاف إرسال رسائل للعملاء عند التجهيز
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (messagingSettings.stopCustomerMessagingOnReady) Color(0xFFFEF2F2) else Color(0xFFF8FAFC))
                                        .border(
                                            1.dp,
                                            if (messagingSettings.stopCustomerMessagingOnReady) Color(0xFFFECACA) else Color(0xFFE2E8F0),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "إيقاف إرسال رسائل للعملاء عند التجهيز",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = if (messagingSettings.stopCustomerMessagingOnReady)
                                                "مفعل: لن يتم إرسال رسائل أو إظهار تأكيد إرسال رسالة للعملاء عند التجهيز"
                                            else
                                                "معطل: سيتم إظهار تأكيد إرسال رسالة للعميل عند اكتمال كافة طلباته",
                                            fontSize = 12.sp,
                                            color = if (messagingSettings.stopCustomerMessagingOnReady) Color(0xFFDC2626) else Color(0xFF64748B)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = messagingSettings.stopCustomerMessagingOnReady,
                                        onCheckedChange = { stop ->
                                            messagingViewModel.setStopCustomerMessagingOnReady(stop)
                                        },
                                        modifier = Modifier.testTag("stop_customer_messaging_switch"),
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFFDC2626),
                                            uncheckedThumbColor = Color.White,
                                            uncheckedTrackColor = Color(0xFFCBD5E1)
                                        )
                                    )
                                }

                                HorizontalDivider(color = Color(0xFFE2E8F0))

                                // Button to open detailed MessagingSettingsScreen for templates, SMS vs WhatsApp, etc.
                                OutlinedButton(
                                    onClick = { showMessagingSettingsScreen = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("open_messaging_details_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, BluePrimary),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BluePrimary)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "قوالب الرسائل ونوع الإرسال (SMS / واتساب)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Linked Devices
                    if (devicesViewModel != null) {
                        SettingsOptionBar(
                            title = "الأجهزة المرتبطة",
                            subtitle = "عرض وإدارة الأجهزة المتصلة بالنظام وتاريخ المزامنة",
                            icon = Icons.Default.Devices,
                            iconTint = Color(0xFF0284C7),
                            iconBg = Color(0xFFE0F2FE),
                            testTag = "linked_devices_setting_card",
                            onClick = { showLinkedDevicesScreen = true }
                        )
                    }

                    // 4. Change Password
                    SettingsOptionBar(
                        title = "تغيير كلمة المرور",
                        subtitle = "تغيير كلمة المرور الخاصة بحسابك الحالي",
                        icon = Icons.Default.LockReset,
                        iconTint = Color(0xFFD97706),
                        iconBg = Color(0xFFFEF3C7),
                        testTag = "change_password_setting_card",
                        onClick = { showChangePasswordDialog = true }
                    )

                    // 4. Logout
                    SettingsOptionBar(
                        title = "تسجيل الخروج",
                        subtitle = "الخروج من الحساب الحالي والعودة لشاشة الدخول",
                        icon = Icons.AutoMirrored.Filled.Logout,
                        iconTint = Color(0xFFDC2626),
                        iconBg = Color(0xFFFEE2E2),
                        testTag = "logout_setting_card",
                        onClick = { showLogoutDialog = true }
                    )

                    // System Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = Color(0xFF475569),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "معلومات التطبيق",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp
                                    ),
                                    color = Color(0xFF1E293B)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "ترند للخياطة الرجالية - كشف متابعة العمل | الإصدار 1.0 (محلي)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.sp,
                                    color = Color(0xFF4B5563),
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }

    // Change Password Dialog
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            authViewModel = authViewModel,
            onDismiss = { showChangePasswordDialog = false },
            onSuccess = { message ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(message)
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "تسجيل الخروج",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("هل أنت متأكد من تسجيل الخروج من التطبيق؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.testTag("confirm_logout_button")
                ) {
                    Text("تسجيل الخروج", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun SettingsOptionBar(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = iconBg,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = Color(0xFF000000)
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569)
                        ),
                        maxLines = 1
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

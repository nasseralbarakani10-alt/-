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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MessagingSettings
import com.example.ui.theme.BluePrimary
import com.example.ui.viewmodel.MessagingViewModel
import kotlinx.coroutines.launch

@Composable
fun MessagingSettingsScreen(
    messagingViewModel: MessagingViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSettings by messagingViewModel.settings.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var messageType by remember { mutableStateOf(currentSettings.messageType) }
    var delaySecondsText by remember { mutableStateOf(currentSettings.delaySeconds.toString()) }
    var autoSendEnabled by remember { mutableStateOf(currentSettings.autoSendEnabled) }
    var readyMessageTemplate by remember { mutableStateOf(currentSettings.readyMessageTemplate) }
    var shopPhoneNumber by remember { mutableStateOf(currentSettings.shopPhoneNumber) }
    var stopShopMessaging by remember { mutableStateOf(currentSettings.stopShopMessaging) }
    var stopCustomerMessagingOnReady by remember { mutableStateOf(currentSettings.stopCustomerMessagingOnReady) }

    // Synchronize local form when settings load from database
    LaunchedEffect(currentSettings) {
        messageType = currentSettings.messageType
        delaySecondsText = currentSettings.delaySeconds.toString()
        autoSendEnabled = currentSettings.autoSendEnabled
        readyMessageTemplate = currentSettings.readyMessageTemplate
        shopPhoneNumber = currentSettings.shopPhoneNumber
        stopShopMessaging = currentSettings.stopShopMessaging
        stopCustomerMessagingOnReady = currentSettings.stopCustomerMessagingOnReady
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                color = BluePrimary,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("messaging_settings_top_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("messaging_settings_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color(0xFF000000),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column {
                        Text(
                            text = "الرسائل",
                            color = Color(0xFF000000),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = "إعدادات قوالب ونوع إرسال رسائل التجهيز للعملاء",
                            color = Color(0xFF000000),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // ==========================================
            // SECTION 0: تحكم إرسال الرسائل ورقم المحل
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("section_shop_customer_messaging_card"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = BluePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "رسائل المحل والعملاء",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                    }

                    // رقم المحل
                    OutlinedTextField(
                        value = shopPhoneNumber,
                        onValueChange = { shopPhoneNumber = it },
                        label = { Text("رقم المحل") },
                        placeholder = { Text("أدخل رقم هاتف المحل لتلقي إشعارات التجهيز") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("messaging_screen_shop_phone_input"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // إيقاف إرسال الرسائل للمحل
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (stopShopMessaging) Color(0xFFFEF2F2) else Color(0xFFF8FAFC))
                            .border(
                                1.dp,
                                if (stopShopMessaging) Color(0xFFFECACA) else Color(0xFFE2E8F0),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "إيقاف إرسال الرسائل للمحل",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = if (stopShopMessaging)
                                    "لن يتم إرسال رسائل أو إظهار تأكيد إرسال رسالة للمحل"
                                else
                                    "إظهار تأكيد إرسال رسالة للمحل عند اكتمال طلبات العميل",
                                fontSize = 11.sp,
                                color = if (stopShopMessaging) Color(0xFFDC2626) else Color(0xFF64748B)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = stopShopMessaging,
                            onCheckedChange = { stopShopMessaging = it },
                            modifier = Modifier.testTag("messaging_screen_stop_shop_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFDC2626)
                            )
                        )
                    }

                    // إيقاف إرسال رسائل للعملاء عند التجهيز
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (stopCustomerMessagingOnReady) Color(0xFFFEF2F2) else Color(0xFFF8FAFC))
                            .border(
                                1.dp,
                                if (stopCustomerMessagingOnReady) Color(0xFFFECACA) else Color(0xFFE2E8F0),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "إيقاف إرسال رسائل للعملاء عند التجهيز",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = if (stopCustomerMessagingOnReady)
                                    "لن يتم إرسال رسائل أو إظهار تأكيد إرسال رسالة للعميل"
                                else
                                    "إظهار تأكيد إرسال رسالة للعميل عند اكتمال طلباته",
                                fontSize = 11.sp,
                                color = if (stopCustomerMessagingOnReady) Color(0xFFDC2626) else Color(0xFF64748B)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = stopCustomerMessagingOnReady,
                            onCheckedChange = { stopCustomerMessagingOnReady = it },
                            modifier = Modifier.testTag("messaging_screen_stop_customer_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFDC2626)
                            )
                        )
                    }
                }
            }

            // ==========================================
            // SECTION 1: نوع الرسائل
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("section_message_type_card"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = null,
                                    tint = BluePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "نوع الرسائل",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "- وسيلة إرسال الإشعارات",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 3 Options side-by-side or compact rows: SMS, واتساب أعمال, واتساب
                    val options = listOf(
                        Triple(MessagingSettings.MESSAGE_TYPE_SMS, "SMS", Icons.Default.Sms),
                        Triple(MessagingSettings.MESSAGE_TYPE_WHATSAPP_BUSINESS, "واتساب أعمال", Icons.Default.Storefront),
                        Triple(MessagingSettings.MESSAGE_TYPE_WHATSAPP, "واتساب", Icons.Default.Chat)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        options.forEach { (typeValue, label, icon) ->
                            val isSelected = messageType == typeValue
                            val tag = when (typeValue) {
                                MessagingSettings.MESSAGE_TYPE_SMS -> "message_type_sms"
                                MessagingSettings.MESSAGE_TYPE_WHATSAPP_BUSINESS -> "message_type_whatsapp_business"
                                else -> "message_type_whatsapp"
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { messageType = typeValue }
                                    .testTag(tag),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) BluePrimary else Color(0xFFE2E8F0)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { messageType = typeValue },
                                        modifier = Modifier.size(20.dp),
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = BluePrimary,
                                            unselectedColor = Color(0xFF94A3B8)
                                        )
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) BluePrimary else Color(0xFF64748B),
                                        modifier = Modifier.size(16.dp)
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Text(
                                        text = label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color(0xFF1E3A8A) else Color(0xFF1E293B),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // SECTION 2: خيارات الإرسال والمدة
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("section_sending_options_card"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    // Row 1: المدة بين إرسال الرسائل
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "المدة بين إرسال الرسائل",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "الفاصل الزمني بالثواني لمنع التكرار اللحظي",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = delaySecondsText,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() } && input.length <= 4) {
                                    delaySecondsText = input
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon = {
                                Text(
                                    text = "ث",
                                    fontSize = 12.sp,
                                    color = Color(0xFF475569),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            },
                            textStyle = TextStyle(
                                fontSize = 13.5.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BluePrimary,
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .width(90.dp)
                                .height(46.dp)
                                .testTag("delay_seconds_input")
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 6.dp),
                        color = Color(0xFFF1F5F9)
                    )

                    // Row 2: إرسال الرسالة تلقائيًا
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { autoSendEnabled = !autoSendEnabled }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "إرسال الرسالة تلقائيًا",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = if (autoSendEnabled)
                                    "مفعل: يتم إرسال الرسالة فور تأكيد جاهزية الطلب"
                                else
                                    "معطل: تحديث حالة الطلب إلى جاهز دون إرسال رسائل",
                                fontSize = 12.sp,
                                color = if (autoSendEnabled) BluePrimary else Color(0xFF64748B)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Switch(
                            checked = autoSendEnabled,
                            onCheckedChange = { autoSendEnabled = it },
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("auto_send_switch"),
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
            }

            // ==========================================
            // SECTION 3: رسالة التجهيز
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("section_ready_template_card"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "رسالة التجهيز",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "- القالب المرسل عند جاهزية الطلب",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = readyMessageTemplate,
                        onValueChange = { readyMessageTemplate = it },
                        minLines = 2,
                        maxLines = 3,
                        textStyle = TextStyle(
                            fontSize = 13.sp,
                            color = Color.Black,
                            lineHeight = 18.sp
                        ),
                        placeholder = {
                            Text(
                                text = MessagingSettings.DEFAULT_TEMPLATE,
                                color = Color(0xFF94A3B8),
                                fontSize = 12.5.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ready_message_template_input")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Live Preview box
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "معاينة الرسالة كما ستصل للعميل:",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            val displayTemplate = readyMessageTemplate.ifBlank {
                                MessagingSettings.DEFAULT_TEMPLATE
                            }
                            Text(
                                text = "عميلنا: أحمد محمد / 1024\n$displayTemplate",
                                fontSize = 12.sp,
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Medium,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // ==========================================
            // SAVE BUTTON
            // ==========================================
            Button(
                onClick = {
                    val parsedDelay = delaySecondsText.toIntOrNull() ?: 15
                    messagingViewModel.saveSettings(
                        messageType = messageType,
                        delaySeconds = parsedDelay,
                        autoSendEnabled = autoSendEnabled,
                        readyMessageTemplate = readyMessageTemplate.ifBlank { MessagingSettings.DEFAULT_TEMPLATE },
                        shopPhoneNumber = shopPhoneNumber,
                        stopShopMessaging = stopShopMessaging,
                        stopCustomerMessagingOnReady = stopCustomerMessagingOnReady,
                        onSuccess = {
                            scope.launch {
                                snackbarHostState.showSnackbar("تم حفظ إعدادات الرسائل بنجاح")
                            }
                        },
                        onError = { err ->
                            scope.launch {
                                snackbarHostState.showSnackbar(err)
                            }
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("save_messaging_settings_button"),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = Color(0xFF000000),
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "حفظ الإعدادات",
                    color = Color(0xFF000000),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MessagingSettings
import com.example.data.model.Order
import com.example.data.model.OrderWithCategory
import com.example.ui.components.AddOrderDialog
import com.example.ui.components.DailyOrderSeparator
import com.example.ui.components.OrderCard
import com.example.ui.components.appTextFieldColors
import com.example.ui.theme.BlueButton
import com.example.ui.theme.BlueDark
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.GreenButton
import com.example.ui.theme.RedButton
import com.example.ui.viewmodel.CategoriesViewModel
import com.example.ui.viewmodel.CuttersViewModel
import com.example.ui.viewmodel.MessagingViewModel
import com.example.ui.viewmodel.OrdersViewModel
import com.example.ui.viewmodel.TailorsViewModel
import com.example.util.MessagingDispatcher
import com.example.util.SmsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class DayGroup(
    val dayKey: String,
    val dayName: String,
    val fullDate: String,
    val orders: List<OrderWithCategory>
)

@Composable
fun MainScreen(
    ordersViewModel: OrdersViewModel,
    categoriesViewModel: CategoriesViewModel,
    cuttersViewModel: CuttersViewModel,
    tailorsViewModel: TailorsViewModel,
    messagingViewModel: MessagingViewModel? = null,
    modifier: Modifier = Modifier
) {
    val ordersWithCategory by ordersViewModel.ordersWithCategory.collectAsStateWithLifecycle()
    val orderCount by ordersViewModel.orderCount.collectAsStateWithLifecycle()
    val selectedIds by ordersViewModel.selectedOrderIds.collectAsStateWithLifecycle()
    val searchQuery by ordersViewModel.searchQuery.collectAsStateWithLifecycle()
    val categories by categoriesViewModel.allCategories.collectAsStateWithLifecycle()
    val cutters by cuttersViewModel.activeCutters.collectAsStateWithLifecycle()
    val tailors by tailorsViewModel.activeTailors.collectAsStateWithLifecycle()
    val messagingSettings by (messagingViewModel?.settings ?: remember {
        MutableStateFlow(MessagingSettings())
    }).collectAsStateWithLifecycle()

    val context = LocalContext.current
    var editingOrderDetail by remember { mutableStateOf<OrderWithCategory?>(null) }
    var pendingReadyConfirmationOrder by remember { mutableStateOf<Order?>(null) }
    var pendingSmsOrder by remember { mutableStateOf<Order?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val targetOrder = pendingSmsOrder
        pendingSmsOrder = null
        if (targetOrder != null) {
            if (isGranted) {
                val template = messagingSettings.readyMessageTemplate.ifBlank {
                    MessagingSettings.DEFAULT_TEMPLATE
                }
                val messageText = "عميلنا: ${targetOrder.customerName} / ${targetOrder.customerNumber}\n$template"
                MessagingDispatcher.enqueueSms(
                    context = context,
                    phoneNumber = targetOrder.phoneNumber,
                    messageText = messageText,
                    delaySeconds = messagingSettings.delaySeconds
                ) { _, msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("لم يتم منح إذن إرسال الرسائل القصيرة (SMS)")
                }
            }
            ordersViewModel.setOrderReady(
                order = targetOrder,
                ready = true,
                onError = { err -> scope.launch { snackbarHostState.showSnackbar(err) } }
            )
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var showSearchRow by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val todayFormatted = remember {
        val formatter = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar"))
        formatter.format(Date())
    }

    // If viewing/editing customer details
    if (editingOrderDetail != null) {
        CustomerDetailScreen(
            orderWithCategory = editingOrderDetail!!,
            categories = categories,
            cutters = cutters,
            tailors = tailors,
            onBack = { editingOrderDetail = null },
            onSave = { updatedOrder ->
                ordersViewModel.updateOrder(
                    order = updatedOrder,
                    onSuccess = {
                        editingOrderDetail = null
                        scope.launch {
                            snackbarHostState.showSnackbar("تم حفظ التعديلات بنجاح")
                        }
                    },
                    onError = { error ->
                        scope.launch {
                            snackbarHostState.showSnackbar(error)
                        }
                    }
                )
            },
            modifier = modifier
        )
        return
    }

    // Group orders by date portion of createdAt (day boundaries in local device timezone)
    val groupedOrders = remember(ordersWithCategory) {
        val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayNameFormat = SimpleDateFormat("EEEE", Locale("ar"))
        val fullDateFormat = SimpleDateFormat("d MMMM yyyy", Locale("ar"))

        ordersWithCategory
            .groupBy { dayKeyFormat.format(Date(it.order.createdAt)) }
            .map { (dayKey, items) ->
                val firstDate = Date(items.first().order.createdAt)
                DayGroup(
                    dayKey = dayKey,
                    dayName = dayNameFormat.format(firstDate),
                    fullDate = fullDateFormat.format(firstDate),
                    orders = items.sortedByDescending { it.order.createdAt }
                )
            }
            .sortedByDescending { it.dayKey }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
        ) {
            // TOP APP BAR: Sky Blue background (#29B6F6), bold black text
            Surface(
                color = BluePrimary,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "كشف متابعة العمل لمحلات ترند للخياطة الرجالية",
                        color = Color(0xFF000000),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = todayFormatted,
                        color = Color(0xFF000000),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // THREE LARGE RECTANGULAR BUTTONS SIDE BY SIDE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "بحث" Button: sky blue background, bold black text
                Button(
                    onClick = { showSearchRow = !showSearchRow },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueButton),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("search_action_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = Color(0xFF000000),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "بحث",
                            color = Color(0xFF000000),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // "حذف" Button: red background, white text
                Button(
                    onClick = {
                        if (selectedIds.isNotEmpty()) {
                            showDeleteConfirmation = true
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("يرجى تحديد عمليات لحذفها أولاً")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedButton),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("delete_action_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (selectedIds.isNotEmpty()) "حذف (${selectedIds.size})" else "حذف",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // "إضافة عميل" Button: green background, white text
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenButton),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("add_customer_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "إضافة عميل",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "إضافة عميل",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // SEARCH BAR (Visible when "بحث" is toggled)
            AnimatedVisibility(visible = showSearchRow) {
                Surface(
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 2.dp
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { ordersViewModel.setSearchQuery(it) },
                        placeholder = {
                            Text(
                                text = "بحث باسم العميل، رقم الهاتف، أو الصنف...",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 15.sp,
                                    color = Color(0xFF757575),
                                    fontWeight = FontWeight.Normal
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF1565C0)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { ordersViewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "مسح",
                                        tint = Color(0xFF212121)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 16.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Normal
                        ),
                        colors = appTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_text_input")
                    )
                }
            }

            // TOTAL ORDER COUNT BAR (Live from database count query)
            Surface(
                color = Color(0xFFF1F5F9),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = BlueDark,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "إجمالي العمليات المسجلة:",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            color = Color(0xFF212121)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BluePrimary,
                        modifier = Modifier.padding(start = 6.dp)
                    ) {
                        Text(
                            text = "$orderCount",
                            color = Color(0xFF000000),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                                .testTag("total_orders_count_badge")
                        )
                    }
                }
            }

            // SCROLLABLE ORDERS LIST WITH DAILY SEPARATORS
            if (ordersWithCategory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFFE2E8F0),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "لا توجد نتائج مطابقة للبحث" else "لا توجد عمليات مسجلة حتى الآن",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = Color(0xFF212121),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "جرّب البحث بكلمة أخرى" else "اضغط على زر \"إضافة عميل\" بالأعلى لبدء تسجيل أول طلب",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp
                            ),
                            color = Color(0xFF616161),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("orders_list"),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp)
                ) {
                    groupedOrders.forEach { dayGroup ->
                        // DAILY SEPARATOR ROW
                        item(key = "divider_${dayGroup.dayKey}") {
                            DailyOrderSeparator(
                                dayName = dayGroup.dayName,
                                fullDate = dayGroup.fullDate,
                                orderCount = dayGroup.orders.size
                            )
                        }

                        // ORDERS FOR THAT DAY
                        items(
                            items = dayGroup.orders,
                            key = { it.order.id }
                        ) { item ->
                            OrderCard(
                                orderWithCategory = item,
                                isSelected = selectedIds.contains(item.order.id),
                                onToggleSelect = { ordersViewModel.toggleSelection(item.order.id) },
                                onToggleLaundry = {
                                    ordersViewModel.toggleLaundry(
                                        order = item.order,
                                        onError = { error ->
                                            scope.launch {
                                                snackbarHostState.showSnackbar(error)
                                            }
                                        }
                                    )
                                },
                                onToggleButtonIroning = {
                                    ordersViewModel.toggleButtonIroning(
                                        order = item.order,
                                        onError = { error ->
                                            scope.launch {
                                                snackbarHostState.showSnackbar(error)
                                            }
                                        }
                                    )
                                },
                                onToggleReady = {
                                    if (!item.order.ready) {
                                        pendingReadyConfirmationOrder = item.order
                                    } else {
                                        ordersViewModel.setOrderReady(
                                            order = item.order,
                                            ready = false,
                                            onError = { error ->
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(error)
                                                }
                                            }
                                        )
                                    }
                                },
                                onOpenDetail = {
                                    editingOrderDetail = item
                                },
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // SNACKBAR HOST
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }

    // ADD ORDER DIALOG
    if (showAddDialog) {
        AddOrderDialog(
            categories = categories,
            cutters = cutters,
            tailors = tailors,
            onDismiss = { showAddDialog = false },
            onSave = { customerName, customerNumber, phoneNumber, categoryId, fabricType, cutterId, tailorId, buttonIroning, laundry ->
                ordersViewModel.addOrder(
                    customerName = customerName,
                    customerNumber = customerNumber,
                    phoneNumber = phoneNumber,
                    categoryId = categoryId,
                    fabricType = fabricType,
                    cutterId = cutterId,
                    tailorId = tailorId,
                    buttonIroning = buttonIroning,
                    laundry = laundry
                )
                showAddDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("تم إضافة العميل بنجاح")
                }
            }
        )
    }

    // DELETE CONFIRMATION DIALOG
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Text(
                    text = "تأكيد الحذف",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = RedButton
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من حذف ${selectedIds.size} من العمليات المحددة؟ لا يمكن التراجع عن هذا الإجراء.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        color = Color(0xFF212121)
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val count = selectedIds.size
                        ordersViewModel.deleteSelectedOrders(
                            onSuccess = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("تم حذف $count من العمليات المحددة")
                                }
                            },
                            onError = { error ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(error)
                                }
                            }
                        )
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedButton),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("حذف", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("إلغاء", color = Color(0xFF424242), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }
        )
    }

    // READY CONFIRMATION DIALOG (WITH MESSAGING OPTION)
    pendingReadyConfirmationOrder?.let { targetOrder ->
        AlertDialog(
            onDismissRequest = { pendingReadyConfirmationOrder = null },
            title = {
                Text(
                    text = "تأكيد جاهزية الطلب",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                val appLabel = when (messagingSettings.messageType) {
                    MessagingSettings.MESSAGE_TYPE_WHATSAPP_BUSINESS -> "واتساب أعمال"
                    MessagingSettings.MESSAGE_TYPE_WHATSAPP -> "واتساب"
                    else -> "SMS"
                }
                Text(
                    text = "هل تريد إرسال رسالة للعميل عبر $appLabel بأن الطلب جاهز للتسليم؟",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        color = Color(0xFF1E293B)
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val current = targetOrder
                        pendingReadyConfirmationOrder = null
                        val template = messagingSettings.readyMessageTemplate.ifBlank {
                            MessagingSettings.DEFAULT_TEMPLATE
                        }
                        val messageText = "عميلنا: ${current.customerName} / ${current.customerNumber}\n$template"

                        when (messagingSettings.messageType) {
                            MessagingSettings.MESSAGE_TYPE_SMS -> {
                                if (messagingSettings.autoSendEnabled) {
                                    val hasSmsPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.SEND_SMS
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasSmsPermission) {
                                        MessagingDispatcher.enqueueSms(
                                            context = context,
                                            phoneNumber = current.phoneNumber,
                                            messageText = messageText,
                                            delaySeconds = messagingSettings.delaySeconds
                                        ) { _, msg ->
                                            scope.launch { snackbarHostState.showSnackbar(msg) }
                                        }
                                        ordersViewModel.setOrderReady(
                                            order = current,
                                            ready = true,
                                            onError = { err -> scope.launch { snackbarHostState.showSnackbar(err) } }
                                        )
                                    } else {
                                        pendingSmsOrder = current
                                        smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                                    }
                                } else {
                                    ordersViewModel.setOrderReady(
                                        order = current,
                                        ready = true,
                                        onError = { err -> scope.launch { snackbarHostState.showSnackbar(err) } }
                                    )
                                }
                            }
                            MessagingSettings.MESSAGE_TYPE_WHATSAPP_BUSINESS -> {
                                MessagingDispatcher.openWhatsApp(
                                    context = context,
                                    phoneNumber = current.phoneNumber,
                                    messageText = messageText,
                                    isBusiness = true
                                ) { _, msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                                ordersViewModel.setOrderReady(
                                    order = current,
                                    ready = true,
                                    onError = { err -> scope.launch { snackbarHostState.showSnackbar(err) } }
                                )
                            }
                            else -> { // MessagingSettings.MESSAGE_TYPE_WHATSAPP
                                MessagingDispatcher.openWhatsApp(
                                    context = context,
                                    phoneNumber = current.phoneNumber,
                                    messageText = messageText,
                                    isBusiness = false
                                ) { _, msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                                ordersViewModel.setOrderReady(
                                    order = current,
                                    ready = true,
                                    onError = { err -> scope.launch { snackbarHostState.showSnackbar(err) } }
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenButton),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("confirm_ready_yes_sms_btn")
                ) {
                    Text("نعم", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val current = targetOrder
                            pendingReadyConfirmationOrder = null
                            ordersViewModel.setOrderReady(
                                order = current,
                                ready = true,
                                onError = { err -> scope.launch { snackbarHostState.showSnackbar(err) } }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("confirm_ready_no_sms_btn")
                    ) {
                        Text("لا", color = Color(0xFF000000), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    TextButton(
                        onClick = { pendingReadyConfirmationOrder = null },
                        modifier = Modifier.testTag("confirm_ready_cancel_btn")
                    ) {
                        Text("إلغاء", color = Color(0xFF000000), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        )
    }
}

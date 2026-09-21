package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Category
import com.example.data.model.Cutter
import com.example.data.model.DraftOrderData
import com.example.data.model.Tailor
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.GreenButton
import com.example.ui.viewmodel.OrdersViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect

private data class SavedOrderItem(
    val orderId: Long,
    val sequenceNumber: Int,
    val draft: DraftOrderData
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerScreen(
    categories: List<Category>,
    cutters: List<Cutter>,
    tailors: List<Tailor>,
    ordersViewModel: OrdersViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Top-level Customer fields
    var customerName by remember { mutableStateOf("") }
    var customerNumber by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    var savedCustomerId by remember { mutableStateOf<Long?>(null) }
    val savedOrders = remember { mutableStateListOf<SavedOrderItem>() }

    // State for the "طلب {N}" popup
    var showAddOrderPopup by remember { mutableStateOf(false) }

    var validationError by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Auto-detect existing client when customerNumber or phoneNumber is typed
    LaunchedEffect(customerNumber, phoneNumber) {
        val num = customerNumber.trim()
        val phone = phoneNumber.trim()
        if (savedCustomerId == null && (num.isNotBlank() || phone.isNotBlank())) {
            val existing = ordersViewModel.findCustomerByNumberOrPhone(num, phone)
            if (existing != null) {
                savedCustomerId = existing.id
                if (customerName.isBlank()) customerName = existing.name
                if (customerNumber.isBlank()) customerNumber = existing.customerNumber
                if (phoneNumber.isBlank()) phoneNumber = existing.phoneNumber
                val existingOrders = ordersViewModel.getOrdersForCustomerDirect(existing.id)
                savedOrders.clear()
                existingOrders.forEach { ord ->
                    savedOrders.add(
                        SavedOrderItem(
                            orderId = ord.id,
                            sequenceNumber = ord.sequenceNumber,
                            draft = DraftOrderData(
                                categoryId = ord.categoryId,
                                fabricType = ord.fabricType,
                                cutterId = ord.cutterId,
                                tailorId = ord.tailorId,
                                cutterPrice = ord.cutterPrice,
                                tailorPrice = ord.tailorPrice,
                                buttonIroning = ord.buttonIroning,
                                laundry = ord.laundry,
                                categoryName = categories.find { it.id == ord.categoryId }?.name ?: "",
                                cutterName = cutters.find { it.id == ord.cutterId }?.name ?: "الافتراضي",
                                tailorName = tailors.find { it.id == ord.tailorId }?.name ?: "الافتراضي"
                            )
                        )
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "إضافة عميل جديد",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BluePrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Customer Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "بيانات العميل",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = BluePrimary
                    )

                    // 1. اسم العميل
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = {
                            customerName = it
                            validationError = null
                        },
                        label = { Text("اسم العميل *") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = BluePrimary
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_name_input"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // 2. رقم العميل
                    OutlinedTextField(
                        value = customerNumber,
                        onValueChange = {
                            customerNumber = it
                            validationError = null
                        },
                        label = { Text("رقم العميل *") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = BluePrimary
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_number_input"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // 3. رقم الهاتف
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it
                            validationError = null
                        },
                        label = { Text("رقم الهاتف *") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = BluePrimary
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_phone_input"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Directly below the phone number display: عدد الطلبات: X
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE0F2FE),
                        border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "عدد الطلبات: ${savedOrders.size}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0369A1),
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("add_customer_orders_count")
                        )
                    }
                }
            }

            // Section 2: "إضافة طلب" Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val trimmedName = customerName.trim()
                        val trimmedNumber = customerNumber.trim()
                        val trimmedPhone = phoneNumber.trim()

                        if (trimmedName.isBlank()) {
                            validationError = "يرجى إدخال اسم العميل أولاً"
                            return@Button
                        }
                        if (trimmedNumber.isBlank()) {
                            validationError = "يرجى إدخال رقم العميل أولاً"
                            return@Button
                        }
                        if (trimmedPhone.isBlank()) {
                            validationError = "يرجى إدخال رقم الهاتف أولاً"
                            return@Button
                        }
                        validationError = null
                        showAddOrderPopup = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_order_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "إضافة طلب",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                if (savedOrders.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFDCFCE7),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "تم الحفظ بنجاح ✓",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Section 3: List of Added Orders (Directly below client information)
            if (savedOrders.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "لم يتم إضافة أي طلب بعد",
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "أدخل بيانات العميل واضغط على \"إضافة طلب\" لحفظ العميل والطلب معاً",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    savedOrders.forEachIndexed { index, item ->
                        DraftOrderCard(
                            sequenceNumber = item.sequenceNumber,
                            draft = item.draft,
                            onDelete = {
                                val toDeleteId = item.orderId
                                ordersViewModel.deleteSingleOrderDirect(
                                    orderId = toDeleteId,
                                    onSuccess = {
                                        savedOrders.removeAt(index)
                                        val renumbered = savedOrders.mapIndexed { idx, itm ->
                                            itm.copy(sequenceNumber = idx + 1)
                                        }
                                        savedOrders.clear()
                                        savedOrders.addAll(renumbered)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("تم حذف الطلب بنجاح")
                                        }
                                    },
                                    onError = { err ->
                                        scope.launch { snackbarHostState.showSnackbar(err) }
                                    }
                                )
                            }
                        )
                    }
                }
            }

            // Validation Error Banner
            if (validationError != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444))
                ) {
                    Text(
                        text = validationError ?: "",
                        color = Color(0xFFDC2626),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation back button once saved (no separate "حفظ العميل" button required!)
            if (savedOrders.isNotEmpty()) {
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenButton),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_all_customer_orders_button")
                ) {
                    Text(
                        text = "العودة للرئيسية (${savedOrders.size} طلبات محفوظة)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // Popup: إضافة طلب {N}
    if (showAddOrderPopup) {
        AddOrderPopup(
            orderNumber = savedOrders.size + 1,
            categories = categories,
            cutters = cutters,
            tailors = tailors,
            onDismiss = { showAddOrderPopup = false },
            onSave = { fabricType, selectedCategory, selectedCutter, selectedTailor ->
                scope.launch {
                    val categoryId = selectedCategory.id
                    val cutterId = selectedCutter?.id
                    val tailorId = selectedTailor?.id

                    // Silently lookup saved prices
                    val cutterPrice = if (cutterId != null) {
                        ordersViewModel.getCutterPriceDirect(cutterId, categoryId)
                    } else 0.0

                    val tailorPrice = if (tailorId != null) {
                        ordersViewModel.getTailorPriceDirect(tailorId, categoryId)
                    } else 0.0

                    val draft = DraftOrderData(
                        categoryId = categoryId,
                        fabricType = fabricType,
                        cutterId = cutterId,
                        tailorId = tailorId,
                        cutterPrice = cutterPrice,
                        tailorPrice = tailorPrice,
                        buttonIroning = false,
                        laundry = false,
                        categoryName = selectedCategory.name,
                        cutterName = selectedCutter?.name ?: "الافتراضي",
                        tailorName = selectedTailor?.name ?: "الافتراضي"
                    )

                    isSaving = true
                    ordersViewModel.saveCustomerAndSingleOrder(
                        customerName = customerName.trim(),
                        customerNumber = customerNumber.trim(),
                        phoneNumber = phoneNumber.trim(),
                        existingCustomerId = savedCustomerId,
                        draft = draft,
                        onSuccess = { customerId, orderId ->
                            isSaving = false
                            savedCustomerId = customerId
                            savedOrders.add(
                                SavedOrderItem(
                                    orderId = orderId,
                                    sequenceNumber = savedOrders.size + 1,
                                    draft = draft
                                )
                            )
                            showAddOrderPopup = false
                            scope.launch {
                                snackbarHostState.showSnackbar("تم حفظ العميل والطلب بنجاح")
                            }
                        },
                        onError = { err ->
                            isSaving = false
                            validationError = err
                            scope.launch {
                                snackbarHostState.showSnackbar(err)
                            }
                        }
                    )
                }
            }
        )
    }
}

@Composable
private fun DraftOrderCard(
    sequenceNumber: Int,
    draft: DraftOrderData,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Row 1: "طلب {N}" badge, Fabric Type, Category, and Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = BluePrimary
                    ) {
                        Text(
                            text = "طلب $sequenceNumber",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    if (draft.fabricType.isNotBlank()) {
                        Text(
                            text = draft.fabricType,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Text(
                            text = draft.categoryName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "حذف الطلب",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Row 2: Cutter and Tailor info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "القصاص: ${draft.cutterName}",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )
                Text(
                    text = "الخياط: ${draft.tailorName}",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )
            }

            // Row 3: Visual Placeholders as specified:
            // "with زرار وكي / المغسلة / قيد العمل badges beneath it (these badges are just visual placeholders at this stage, not yet interactive, since nothing is saved to Room until the whole customer is saved)"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlaceholderBadge(text = "زرار وكي")
                PlaceholderBadge(text = "المغسلة")
                PlaceholderBadge(text = "قيد العمل", isStatus = true)
            }
        }
    }
}

@Composable
private fun PlaceholderBadge(
    text: String,
    isStatus: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = if (isStatus) Color(0xFFFEF3C7) else Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, if (isStatus) Color(0xFFFDE68A) else Color(0xFFE2E8F0))
    ) {
        Text(
            text = text,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Medium,
            color = if (isStatus) Color(0xFF92400E) else Color(0xFF64748B),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddOrderPopup(
    orderNumber: Int,
    categories: List<Category>,
    cutters: List<Cutter>,
    tailors: List<Tailor>,
    onDismiss: () -> Unit,
    onSave: (
        fabricType: String,
        selectedCategory: Category,
        selectedCutter: Cutter?,
        selectedTailor: Tailor?
    ) -> Unit
) {
    var fabricType by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull()) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var selectedCutter by remember { mutableStateOf<Cutter?>(null) }
    var cutterDropdownExpanded by remember { mutableStateOf(false) }

    var selectedTailor by remember { mutableStateOf<Tailor?>(null) }
    var tailorDropdownExpanded by remember { mutableStateOf(false) }

    var popupError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with Title "طلب {N}" and Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "طلب $orderNumber",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = BluePrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color(0xFF64748B)
                        )
                    }
                }

                // 1. الصنف (Text field)
                OutlinedTextField(
                    value = fabricType,
                    onValueChange = {
                        fabricType = it
                        popupError = null
                    },
                    label = { Text("الصنف *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Style, contentDescription = null, tint = BluePrimary)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("popup_fabric_input"),
                    shape = RoundedCornerShape(8.dp)
                )

                // 2. نوع التفصيل (Dropdown of categories)
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedCategory?.name ?: "اختر نوع التفصيل",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("نوع التفصيل *") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Category, contentDescription = null, tint = BluePrimary)
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    categoryDropdownExpanded = !categoryDropdownExpanded
                                }
                                .testTag("popup_category_dropdown")
                        )
                    }

                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    selectedCategory = category
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // 3. القصاص (Dropdown of active cutters)
                val activeCutters = remember(cutters) { cutters.filter { it.isActive } }
                ExposedDropdownMenuBox(
                    expanded = cutterDropdownExpanded,
                    onExpandedChange = { cutterDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedCutter?.name ?: "الافتراضي",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("القصاص") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.ContentCut, contentDescription = null, tint = BluePrimary)
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = cutterDropdownExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    cutterDropdownExpanded = !cutterDropdownExpanded
                                }
                                .testTag("popup_cutter_dropdown")
                        )
                    }

                    ExposedDropdownMenu(
                        expanded = cutterDropdownExpanded,
                        onDismissRequest = { cutterDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("الافتراضي", fontWeight = FontWeight.Bold, color = BluePrimary) },
                            onClick = {
                                selectedCutter = null
                                cutterDropdownExpanded = false
                            }
                        )
                        activeCutters.forEach { cutter ->
                            DropdownMenuItem(
                                text = { Text(cutter.name) },
                                onClick = {
                                    selectedCutter = cutter
                                    cutterDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // 4. الخياط (Dropdown of active tailors)
                val activeTailors = remember(tailors) { tailors.filter { it.isActive } }
                ExposedDropdownMenuBox(
                    expanded = tailorDropdownExpanded,
                    onExpandedChange = { tailorDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedTailor?.name ?: "الافتراضي",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الخياط") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Engineering, contentDescription = null, tint = BluePrimary)
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = tailorDropdownExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    tailorDropdownExpanded = !tailorDropdownExpanded
                                }
                                .testTag("popup_tailor_dropdown")
                        )
                    }

                    ExposedDropdownMenu(
                        expanded = tailorDropdownExpanded,
                        onDismissRequest = { tailorDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("الافتراضي", fontWeight = FontWeight.Bold, color = BluePrimary) },
                            onClick = {
                                selectedTailor = null
                                tailorDropdownExpanded = false
                            }
                        )
                        activeTailors.forEach { tailor ->
                            DropdownMenuItem(
                                text = { Text(tailor.name) },
                                onClick = {
                                    selectedTailor = tailor
                                    tailorDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Error Message if any
                if (popupError != null) {
                    Text(
                        text = popupError ?: "",
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Action Buttons: "عدم الحفظ" and "حفظ"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("popup_cancel_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("عدم الحفظ", color = Color(0xFF64748B))
                    }

                    Button(
                        onClick = {
                            val cat = selectedCategory
                            if (cat == null) {
                                popupError = "يرجى اختيار نوع التفصيل"
                                return@Button
                            }
                            if (fabricType.trim().isBlank()) {
                                popupError = "يرجى إدخال الصنف"
                                return@Button
                            }
                            onSave(fabricType.trim(), cat, selectedCutter, selectedTailor)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("popup_save_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("حفظ الطلب", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

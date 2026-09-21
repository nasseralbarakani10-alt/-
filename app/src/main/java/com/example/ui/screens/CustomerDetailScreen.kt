package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.Customer
import com.example.data.model.Cutter
import com.example.data.model.Order
import com.example.data.model.OrderWithCategory
import com.example.data.model.Tailor
import com.example.ui.components.appTextFieldColors
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.GreenButton
import com.example.ui.viewmodel.OrdersViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CustomerDetailScreen(
    orderWithCategory: OrderWithCategory,
    categories: List<Category>,
    cutters: List<Cutter>,
    tailors: List<Tailor>,
    ordersViewModel: OrdersViewModel? = null,
    onBack: () -> Unit,
    onSave: (Order, Customer?) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val currentOrder = orderWithCategory.order
    val customerId = currentOrder.customerId

    // Form fields editable
    var customerName by remember { mutableStateOf(orderWithCategory.customerName) }
    var customerNumber by remember { mutableStateOf(orderWithCategory.customerNumber) }
    var phoneNumber by remember { mutableStateOf(orderWithCategory.phoneNumber) }
    var isMessagingAllowedForCustomer by remember(customerId) {
        mutableStateOf(ordersViewModel?.isCustomerMessagingAllowed(customerId) ?: true)
    }
    var fabricType by remember { mutableStateOf(currentOrder.fabricType) }

    var selectedCategoryId by remember {
        mutableStateOf(
            if (categories.any { it.id == currentOrder.categoryId }) currentOrder.categoryId
            else categories.firstOrNull()?.id ?: currentOrder.categoryId
        )
    }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var selectedCutterId by remember { mutableStateOf(currentOrder.cutterId) }
    var cutterDropdownExpanded by remember { mutableStateOf(false) }

    var selectedTailorId by remember { mutableStateOf(currentOrder.tailorId) }
    var tailorDropdownExpanded by remember { mutableStateOf(false) }

    var cutterPriceStr by remember { mutableStateOf(if (currentOrder.cutterPrice > 0) currentOrder.cutterPrice.toString() else "") }
    var tailorPriceStr by remember { mutableStateOf(if (currentOrder.tailorPrice > 0) currentOrder.tailorPrice.toString() else "") }

    var buttonIroning by remember { mutableStateOf(currentOrder.buttonIroning) }
    var laundry by remember { mutableStateOf(currentOrder.laundry) }
    var ready by remember { mutableStateOf(currentOrder.ready) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val dateFormatter = remember { SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")) }
    val formattedDate = remember(currentOrder.createdAt) {
        if (currentOrder.createdAt > 0) dateFormatter.format(Date(currentOrder.createdAt)) else "—"
    }

    Scaffold(
        topBar = {
            Surface(
                color = BluePrimary,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("customer_detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color(0xFF000000)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "تفاصيل العميل والطلب",
                            color = Color(0xFF000000),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = "تعديل وحفظ بيانات العميل",
                            color = Color(0xFF000000),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFDC2626),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            val trimmedName = customerName.trim()
                            val trimmedNumber = customerNumber.trim()
                            if (trimmedName.isBlank()) {
                                errorMessage = "يرجى إدخال اسم العميل"
                                return@Button
                            }
                            if (trimmedNumber.isBlank()) {
                                errorMessage = "يرجى إدخال رقم العميل"
                                return@Button
                            }

                            val cPrice = cutterPriceStr.toDoubleOrNull() ?: 0.0
                            val tPrice = tailorPriceStr.toDoubleOrNull() ?: 0.0

                            val updatedCustomer = orderWithCategory.customer?.copy(
                                name = trimmedName,
                                customerNumber = trimmedNumber,
                                phoneNumber = phoneNumber.trim()
                            )
                            val updatedOrder = currentOrder.copy(
                                categoryId = selectedCategoryId,
                                fabricType = fabricType.trim(),
                                cutterId = selectedCutterId,
                                tailorId = selectedTailorId,
                                cutterPrice = cPrice,
                                tailorPrice = tPrice,
                                buttonIroning = buttonIroning,
                                laundry = laundry,
                                ready = ready,
                                updatedAt = System.currentTimeMillis()
                            )
                            errorMessage = null
                            ordersViewModel?.setCustomerMessagingAllowed(customerId, isMessagingAllowedForCustomer)
                            onSave(updatedOrder, updatedCustomer)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenButton),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_customer_detail_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "حفظ التعديلات",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Customer Info
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "بيانات العميل",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A)
                            )
                        )
                    }

                    // 1. اسم العميل
                    Text(
                        text = "اسم العميل *",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = {
                            customerName = it
                            errorMessage = null
                        },
                        placeholder = { Text("أدخل اسم العميل", color = Color(0xFF64748B)) },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 15.sp, color = Color.Black),
                        colors = appTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("detail_customer_name_input")
                    )

                    // 2. رقم العميل
                    Text(
                        text = "رقم العميل *",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                    OutlinedTextField(
                        value = customerNumber,
                        onValueChange = {
                            customerNumber = it
                            errorMessage = null
                        },
                        placeholder = { Text("أدخل رقم العميل", color = Color(0xFF64748B)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(fontSize = 15.sp, color = Color.Black),
                        colors = appTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("detail_customer_number_input")
                    )

                    // 3. رقم الهاتف
                    Text(
                        text = "رقم الهاتف",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        placeholder = { Text("أدخل رقم الهاتف", color = Color(0xFF64748B)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        textStyle = TextStyle(fontSize = 15.sp, color = Color.Black),
                        colors = appTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("detail_phone_number_input")
                    )

                    // 4. إعداد رسائل العميل (Customer Messaging Preference)
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isMessagingAllowedForCustomer) Color(0xFFEFF6FF) else Color(0xFFFEF2F2))
                            .border(
                                1.dp,
                                if (isMessagingAllowedForCustomer) Color(0xFFBFDBFE) else Color(0xFFFECACA),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "إرسال رسائل التجهيز لهذا العميل",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = if (isMessagingAllowedForCustomer)
                                    "مفعل: سيتم إرسال إشعار التجهيز لهذا العميل عند اكتمال كافة طلباته"
                                else
                                    "معطل: إيقاف إرسال رسائل التجهيز لهذا العميل بشكل مخصص",
                                fontSize = 11.5.sp,
                                color = if (isMessagingAllowedForCustomer) Color(0xFF2563EB) else Color(0xFFDC2626)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = isMessagingAllowedForCustomer,
                            onCheckedChange = { allowed ->
                                isMessagingAllowedForCustomer = allowed
                                ordersViewModel?.setCustomerMessagingAllowed(customerId, allowed)
                            },
                            modifier = Modifier.testTag("customer_messaging_allowed_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF2563EB),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFDC2626)
                            )
                        )
                    }
                }
            }

            // Section 2: Garment & Category Details
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Style,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تفاصيل الثوب ونوع التفصيل",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A)
                            )
                        )
                    }

                    // نوع التفصيل (Category Dropdown)
                    Text(
                        text = "نوع التفصيل *",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val currentCategoryName = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "اختر نوع التفصيل"
                        OutlinedButton(
                            onClick = { categoryDropdownExpanded = true },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("detail_category_dropdown_button")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentCategoryName,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color(0xFF1E293B)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = cat.name,
                                            fontWeight = if (cat.id == selectedCategoryId) FontWeight.Bold else FontWeight.Normal,
                                            color = Color(0xFF0F172A)
                                        )
                                    },
                                    onClick = {
                                        selectedCategoryId = cat.id
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // الصنف (Fabric Type)
                    Text(
                        text = "الصنف / نوع القماش",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                    OutlinedTextField(
                        value = fabricType,
                        onValueChange = { fabricType = it },
                        placeholder = { Text("مثال: ياباني، سلك، كوري...", color = Color(0xFF64748B)) },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 15.sp, color = Color.Black),
                        colors = appTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("detail_fabric_type_input")
                    )
                }
            }

            // Section 3: Workers & Prices
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Engineering,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "القصاص والخياط والأسعار",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A)
                            )
                        )
                    }

                    // القصاص Dropdown
                    Text(
                        text = "القصاص",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val currentCutterName = cutters.firstOrNull { it.id == selectedCutterId }?.name ?: "الافتراضي / بدون قصاص"
                        OutlinedButton(
                            onClick = { cutterDropdownExpanded = true },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("detail_cutter_dropdown_button")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentCutterName,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color(0xFF1E293B)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = cutterDropdownExpanded,
                            onDismissRequest = { cutterDropdownExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = { Text("الافتراضي / بدون قصاص", color = Color(0xFF0F172A)) },
                                onClick = {
                                    selectedCutterId = null
                                    cutterDropdownExpanded = false
                                }
                            )
                            cutters.forEach { c ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = c.name,
                                            fontWeight = if (c.id == selectedCutterId) FontWeight.Bold else FontWeight.Normal,
                                            color = Color(0xFF0F172A)
                                        )
                                    },
                                    onClick = {
                                        selectedCutterId = c.id
                                        cutterDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // سعر القصاص
                    Text(
                        text = "سعر القصاص",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                    OutlinedTextField(
                        value = cutterPriceStr,
                        onValueChange = { cutterPriceStr = it },
                        placeholder = { Text("0.0", color = Color(0xFF64748B)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = TextStyle(fontSize = 15.sp, color = Color.Black),
                        colors = appTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("detail_cutter_price_input")
                    )

                    // الخياط Dropdown
                    Text(
                        text = "الخياط",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val currentTailorName = tailors.firstOrNull { it.id == selectedTailorId }?.name ?: "الافتراضي / بدون خياط"
                        OutlinedButton(
                            onClick = { tailorDropdownExpanded = true },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("detail_tailor_dropdown_button")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentTailorName,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color(0xFF1E293B)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = tailorDropdownExpanded,
                            onDismissRequest = { tailorDropdownExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = { Text("الافتراضي / بدون خياط", color = Color(0xFF0F172A)) },
                                onClick = {
                                    selectedTailorId = null
                                    tailorDropdownExpanded = false
                                }
                            )
                            tailors.forEach { t ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = t.name,
                                            fontWeight = if (t.id == selectedTailorId) FontWeight.Bold else FontWeight.Normal,
                                            color = Color(0xFF0F172A)
                                        )
                                    },
                                    onClick = {
                                        selectedTailorId = t.id
                                        tailorDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // سعر الخياط
                    Text(
                        text = "سعر الخياط",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                    OutlinedTextField(
                        value = tailorPriceStr,
                        onValueChange = { tailorPriceStr = it },
                        placeholder = { Text("0.0", color = Color(0xFF64748B)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = TextStyle(fontSize = 15.sp, color = Color.Black),
                        colors = appTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("detail_tailor_price_input")
                    )
                }
            }

            // Section 4: Statuses & Options
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "خيارات إضافية وحالة العمل",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A)
                        )
                    )

                    // 1. زرار وكي
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { buttonIroning = !buttonIroning }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "زرار وكي",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E293B)
                        )
                        Checkbox(
                            checked = buttonIroning,
                            onCheckedChange = { buttonIroning = it },
                            colors = CheckboxDefaults.colors(checkedColor = GreenButton)
                        )
                    }

                    // 2. المغسلة
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { laundry = !laundry }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "المغسلة",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E293B)
                        )
                        Checkbox(
                            checked = laundry,
                            onCheckedChange = { laundry = it },
                            colors = CheckboxDefaults.colors(checkedColor = GreenButton)
                        )
                    }

                    // 3. حالة الجاهزية (جاهز للتسليم)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { ready = !ready }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "حالة الطلب: ${if (ready) "جاهز للتسليم (جاهز✅)" else "قيد العمل"}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (ready) Color(0xFF15803D) else Color(0xFF475569)
                            )
                            Text(
                                text = if (ready) "تم الانتهاء وجاهز للتسليم" else "الطلب قيد التنفيذ والخياطة",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Switch(
                            checked = ready,
                            onCheckedChange = { ready = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF16A34A),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier.testTag("detail_ready_switch")
                        )
                    }
                }
            }

            // Section 5: Order Metadata
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "معلومات النظام",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                    }
                    Text(
                        text = "رقم العملية في النظام: #${currentOrder.id}",
                        fontSize = 12.5.sp,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = "تاريخ ووقت التسجيل: $formattedDate",
                        fontSize = 12.5.sp,
                        color = Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

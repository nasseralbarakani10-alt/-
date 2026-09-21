package com.example.ui.screens.reports

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Category
import com.example.data.model.OrderWithCategory
import com.example.ui.theme.BlueDark
import com.example.ui.theme.BlueLight
import com.example.ui.theme.BluePrimary
import com.example.ui.viewmodel.CategoriesViewModel
import com.example.ui.viewmodel.OrdersViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class WorkerOption(
    val id: Long,
    val name: String,
    val isActive: Boolean,
    val phoneNumber: String = ""
)

data class WorkerCategorySummary(
    val categoryName: String,
    val count: Int,
    val totalDue: Double
)

@Composable
fun WorkerReportScreen(
    title: String,
    workerLabel: String,
    workers: List<WorkerOption>,
    isCutter: Boolean,
    ordersViewModel: OrdersViewModel,
    categoriesViewModel: CategoriesViewModel,
    onFetchSavedExpense: suspend (workerId: Long, start: Long, end: Long) -> Double,
    onSaveExpense: (workerId: Long, start: Long, end: Long, amount: Double) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)

    val context = LocalContext.current
    val decimalFormat = remember { DecimalFormat("#,##0.##") }
    val reportDateFormatter = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }
    val categories by categoriesViewModel.allCategories.collectAsStateWithLifecycle()

    val initialStartCalendar = remember {
        Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
    }
    val initialEndCalendar = remember { Calendar.getInstance() }

    var selectedStartCalendar by remember { mutableStateOf(initialStartCalendar) }
    var selectedEndCalendar by remember { mutableStateOf(initialEndCalendar) }

    val isDateRangeValid = remember(selectedStartCalendar, selectedEndCalendar) {
        getStartOfDay(selectedEndCalendar) >= getStartOfDay(selectedStartCalendar)
    }

    var selectedWorkerId by remember { mutableStateOf<Long?>(null) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }

    // Active query state
    var queryStartMillis by remember { mutableStateOf(getStartOfDay(initialStartCalendar)) }
    var queryEndMillis by remember { mutableStateOf(getEndOfDay(initialEndCalendar)) }
    var queryWorkerId by remember { mutableStateOf<Long?>(null) }
    var queryCategoryId by remember { mutableStateOf<Long?>(null) }
    var hasExecutedReport by remember { mutableStateOf(false) }

    // Inline share error message
    var shareErrorMessage by remember { mutableStateOf<String?>(null) }

    // Manual expense input
    var expenseInputText by remember { mutableStateOf("") }

    // Load saved expense when queried parameters change
    LaunchedEffect(queryWorkerId, queryStartMillis, queryEndMillis) {
        val wId = queryWorkerId
        if (wId != null) {
            val savedAmount = onFetchSavedExpense(wId, queryStartMillis, queryEndMillis)
            expenseInputText = if (savedAmount > 0.0) {
                if (savedAmount % 1.0 == 0.0) savedAmount.toLong().toString() else savedAmount.toString()
            } else {
                ""
            }
        }
    }

    val orders: List<OrderWithCategory> by remember(queryStartMillis, queryEndMillis, queryWorkerId, queryCategoryId) {
        ordersViewModel.getFilteredOrders(
            startTime = queryStartMillis,
            endTime = queryEndMillis,
            cutterId = if (isCutter) queryWorkerId else null,
            tailorId = if (!isCutter) queryWorkerId else null,
            categoryId = queryCategoryId
        )
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    // Calculations based on actual order snapshot prices
    val categorySummaries = remember(orders, isCutter) {
        orders.groupBy { it.category?.name ?: "غير محدد" }
            .map { (catName, orderList) ->
                val count = orderList.size
                val totalDue = orderList.sumOf { item ->
                    if (isCutter) item.order.cutterPrice else item.order.tailorPrice
                }
                WorkerCategorySummary(
                    categoryName = catName,
                    count = count,
                    totalDue = totalDue
                )
            }
            .sortedByDescending { it.count }
    }

    val grandTotalDue = remember(orders, isCutter) {
        orders.sumOf { item ->
            if (isCutter) item.order.cutterPrice else item.order.tailorPrice
        }
    }

    val currentExpenseAmount = remember(expenseInputText) {
        expenseInputText.trim().toDoubleOrNull() ?: 0.0
    }

    val netBalance = remember(grandTotalDue, currentExpenseAmount) {
        grandTotalDue - currentExpenseAmount
    }

    var showWorkerMenu by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        ReportHeader(
            title = title,
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Filters Card
            item {
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
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Worker Dropdown (Cutter / Tailor)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text(
                                    text = workerLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFF94A3B8)),
                                    color = Color.White,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showWorkerMenu = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val selectedWorker = workers.find { it.id == selectedWorkerId }
                                        val textToShow = if (selectedWorker != null) {
                                            if (selectedWorker.isActive) selectedWorker.name else "${selectedWorker.name} (متوقف)"
                                        } else {
                                            "اختر $workerLabel *"
                                        }
                                        Text(
                                            text = textToShow,
                                            fontSize = 14.sp,
                                            color = if (selectedWorker != null) Color(0xFF0F172A) else Color(0xFF94A3B8),
                                            fontWeight = if (selectedWorker != null) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = Color(0xFF64748B)
                                        )
                                    }
                                }
                            }

                            DropdownMenu(
                                expanded = showWorkerMenu,
                                onDismissRequest = { showWorkerMenu = false }
                            ) {
                                workers.forEach { worker ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = worker.name,
                                                    fontWeight = if (worker.id == selectedWorkerId) FontWeight.Bold else FontWeight.Normal
                                                )
                                                if (!worker.isActive) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color(0xFFFEE2E2)
                                                    ) {
                                                        Text(
                                                            text = "متوقف",
                                                            color = Color(0xFFDC2626),
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedWorkerId = worker.id
                                            hasExecutedReport = false
                                            shareErrorMessage = null
                                            showWorkerMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Date Pickers
                        ReportDateField(
                            label = "تاريخ البداية:",
                            calendar = selectedStartCalendar,
                            onDateChanged = {
                                selectedStartCalendar = it
                                hasExecutedReport = false
                                shareErrorMessage = null
                            }
                        )

                        ReportDateField(
                            label = "تاريخ النهاية:",
                            calendar = selectedEndCalendar,
                            onDateChanged = {
                                selectedEndCalendar = it
                                hasExecutedReport = false
                                shareErrorMessage = null
                            }
                        )

                        // Optional Category Filter
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text(
                                    text = "اختيار نوع التفصيل (اختياري):",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFF94A3B8)),
                                    color = Color.White,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showCategoryMenu = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = categories.find { it.id == selectedCategoryId }?.name ?: "الكل (جميع أنواع التفصيل)",
                                            fontSize = 14.sp,
                                            color = Color(0xFF0F172A),
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = Color(0xFF64748B)
                                        )
                                    }
                                }
                            }

                            DropdownMenu(
                                expanded = showCategoryMenu,
                                onDismissRequest = { showCategoryMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("الكل (جميع الأنواع)") },
                                    onClick = {
                                        selectedCategoryId = null
                                        hasExecutedReport = false
                                        shareErrorMessage = null
                                        showCategoryMenu = false
                                    }
                                )
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.name) },
                                        onClick = {
                                            selectedCategoryId = cat.id
                                            hasExecutedReport = false
                                            shareErrorMessage = null
                                            showCategoryMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        if (!isDateRangeValid) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF2F2),
                                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "تاريخ النهاية لا يمكن أن يكون قبل تاريخ البداية",
                                        color = Color(0xFFB91C1C),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Red Continue Button
                        ContinueButton(
                            onClick = {
                                if (isDateRangeValid && selectedWorkerId != null) {
                                    queryStartMillis = getStartOfDay(selectedStartCalendar)
                                    queryEndMillis = getEndOfDay(selectedEndCalendar)
                                    queryWorkerId = selectedWorkerId
                                    queryCategoryId = selectedCategoryId
                                    hasExecutedReport = true
                                    shareErrorMessage = null
                                }
                            },
                            enabled = isDateRangeValid && selectedWorkerId != null
                        )
                    }
                }
            }

            // Results Section
            val isReportGeneratedAndCurrent = hasExecutedReport &&
                queryWorkerId != null &&
                selectedWorkerId == queryWorkerId &&
                getStartOfDay(selectedStartCalendar) == queryStartMillis &&
                getEndOfDay(selectedEndCalendar) == queryEndMillis &&
                selectedCategoryId == queryCategoryId

            if (!isReportGeneratedAndCurrent) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "يرجى اختيار $workerLabel والفترة والضغط على زر استمرار",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp,
                                    color = Color(0xFF475569)
                                )
                            )
                        }
                    }
                }
            } else {
                // Section 1: Breakdown per Category & Grand Total Due
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ملخص مستحقات العمليات",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                )
                                Text(
                                    text = "العدد الإجمالي: ${orders.size}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = BluePrimary,
                                        fontSize = 14.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(10.dp))

                            if (categorySummaries.isEmpty()) {
                                Text(
                                    text = "لا توجد عمليات مسجلة لهذا العامل في هذه الفترة",
                                    color = Color(0xFF64748B),
                                    fontSize = 13.5.sp
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    categorySummaries.forEach { item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = BlueLight
                                                ) {
                                                    Text(
                                                        text = item.categoryName,
                                                        color = BlueDark,
                                                        fontSize = 12.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "(${item.count} ثوب)",
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF475569)
                                                )
                                            }

                                            Text(
                                                text = "${decimalFormat.format(item.totalDue)} ر.س",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Grand Total Due
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "إجمالي المستحق:",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                )
                                Text(
                                    text = "${decimalFormat.format(grandTotalDue)} ر.س",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp,
                                        color = BluePrimary
                                    )
                                )
                            }
                        }
                    }
                }

                // Section 2: Expenses & Net Due
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "تسوية الحساب والمصروفات",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF0F172A)
                                )
                            )

                            // Manual numeric input for expenses
                            OutlinedTextField(
                                value = expenseInputText,
                                onValueChange = { newText ->
                                    // Accept digits and a single decimal point
                                    if (newText.isEmpty() || newText.matches(Regex("^\\d*\\.?\\d*$"))) {
                                        expenseInputText = newText
                                        val amt = newText.toDoubleOrNull() ?: 0.0
                                        queryWorkerId?.let { wId ->
                                            onSaveExpense(wId, queryStartMillis, queryEndMillis, amt)
                                        }
                                    }
                                },
                                label = { Text("إجمالي المصروفات (سلف / مسحوبات)") },
                                placeholder = { Text("0.0") },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("worker_expenses_input")
                            )

                            // Net Balance Calculation Card
                            val isCreditor = netBalance >= 0.0
                            val balanceBadgeBg = if (isCreditor) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                            val balanceBadgeColor = if (isCreditor) Color(0xFF15803D) else Color(0xFFB91C1C)
                            val balanceStatusText = if (isCreditor) "له (مستحق للعامل)" else "عليه (مستحق على العامل)"

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = balanceBadgeBg,
                                border = BorderStroke(1.dp, balanceBadgeColor.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "صافي الحساب:",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = balanceBadgeColor
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = balanceStatusText,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = balanceBadgeColor.copy(alpha = 0.9f)
                                        )
                                    }

                                    Text(
                                        text = "${decimalFormat.format(kotlin.math.abs(netBalance))} ر.س",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = balanceBadgeColor
                                    )
                                }
                            }

                            // Share Report Button below Net Balance result
                            Button(
                                onClick = {
                                    val currentWorker = workers.find { it.id == queryWorkerId }
                                    if (currentWorker == null || currentWorker.phoneNumber.trim().isEmpty()) {
                                        shareErrorMessage = "لا يوجد رقم هاتف محفوظ لهذا الشخص"
                                    } else {
                                        shareErrorMessage = null
                                        val phone = currentWorker.phoneNumber.trim()
                                        val summaryText = buildWorkerReportSummaryText(
                                            workerLabel = workerLabel,
                                            workerName = currentWorker.name,
                                            startDateStr = reportDateFormatter.format(Date(queryStartMillis)),
                                            endDateStr = reportDateFormatter.format(Date(queryEndMillis)),
                                            totalOrdersCount = orders.size,
                                            categorySummaries = categorySummaries,
                                            grandTotalDue = grandTotalDue,
                                            expensesAmount = currentExpenseAmount,
                                            netBalance = netBalance,
                                            decimalFormat = decimalFormat
                                        )
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, summaryText)
                                            putExtra(Intent.EXTRA_SUBJECT, "تقرير حساب $workerLabel - ${currentWorker.name}")
                                            putExtra("address", phone)
                                            putExtra(Intent.EXTRA_PHONE_NUMBER, phone)
                                        }
                                        try {
                                            val chooser = Intent.createChooser(sendIntent, "إرسال التقرير")
                                            context.startActivity(chooser)
                                        } catch (e: Exception) {
                                            shareErrorMessage = "تعذر فتح نافذة المشاركة"
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("share_report_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "إرسال التقرير",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            if (shareErrorMessage != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFEF2F2),
                                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("share_error_message")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ErrorOutline,
                                            contentDescription = null,
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = shareErrorMessage ?: "",
                                            color = Color(0xFFB91C1C),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 3: Orders List
                if (orders.isNotEmpty()) {
                    item {
                        Text(
                            text = "تفاصيل العمليات (${orders.size}):",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1E293B)
                            ),
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                    }

                    items(orders, key = { it.order.id }) { item ->
                        WorkerReportOrderRow(
                            orderWithCategory = item,
                            isCutter = isCutter
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkerReportOrderRow(
    orderWithCategory: OrderWithCategory,
    isCutter: Boolean,
    modifier: Modifier = Modifier
) {
    val order = orderWithCategory.order
    val customerName = orderWithCategory.customerName
    val customerNumber = orderWithCategory.customerNumber
    val phoneNumber = orderWithCategory.phoneNumber
    val categoryName = orderWithCategory.category?.name ?: "غير محدد"
    val price = if (isCutter) order.cutterPrice else order.tailorPrice
    val decimalFormat = remember { DecimalFormat("#,##0.##") }

    val timeFormat = remember { SimpleDateFormat("HH:mm - yyyy/MM/dd", Locale("ar")) }
    val formattedTime = remember(order.createdAt) { timeFormat.format(Date(order.createdAt)) }

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
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            // Line 1: Customer Name, Category, Price snapshot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = customerName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color(0xFF000000)
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(#$customerNumber)",
                        fontSize = 11.5.sp,
                        color = Color(0xFF475569)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = BlueLight,
                    border = BorderStroke(0.5.dp, Color(0xFF90CAF9))
                ) {
                    Text(
                        text = categoryName,
                        color = BlueDark,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Line 2: Phone, Fabric, Price snapshot, and Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (phoneNumber.isNotBlank()) {
                        Text(
                            text = "هاتف: $phoneNumber",
                            fontSize = 11.sp,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (order.fabricType.isNotBlank()) {
                        Text(
                            text = "القماش: ${order.fabricType}",
                            fontSize = 11.sp,
                            color = Color(0xFF1E293B)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(0.5.dp, Color(0xFF86EFAC))
                ) {
                    Text(
                        text = "المستحق: ${decimalFormat.format(price)} ر.س",
                        color = Color(0xFF15803D),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Line 3: Time and status flags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedTime,
                    fontSize = 10.5.sp,
                    color = Color(0xFF64748B)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (order.buttonIroning) {
                        ReportMiniBadge(text = "كوي", color = Color(0xFFB45309), bgColor = Color(0xFFFFFBEB))
                    }
                    if (order.laundry) {
                        ReportMiniBadge(text = "غسيل", color = Color(0xFF0369A1), bgColor = Color(0xFFF0F9FF))
                    }
                    if (order.ready) {
                        ReportMiniBadge(text = "جاهز", color = Color(0xFF15803D), bgColor = Color(0xFFF0FDF4))
                    } else {
                        ReportMiniBadge(text = "قيد التنفيذ", color = Color(0xFFB91C1C), bgColor = Color(0xFFFEF2F2))
                    }
                }
            }
        }
    }
}

private fun buildWorkerReportSummaryText(
    workerLabel: String,
    workerName: String,
    startDateStr: String,
    endDateStr: String,
    totalOrdersCount: Int,
    categorySummaries: List<WorkerCategorySummary>,
    grandTotalDue: Double,
    expensesAmount: Double,
    netBalance: Double,
    decimalFormat: DecimalFormat
): String {
    val balanceStatus = if (netBalance >= 0.0) "له" else "عليه"
    val netFormatted = decimalFormat.format(kotlin.math.abs(netBalance))
    val grandDueFormatted = decimalFormat.format(grandTotalDue)
    val expensesFormatted = decimalFormat.format(expensesAmount)

    val sb = StringBuilder()
    sb.append("تقرير حساب $workerLabel: $workerName\n")
    sb.append("الفترة: من $startDateStr إلى $endDateStr\n")
    sb.append("إجمالي العمليات: $totalOrdersCount\n")
    sb.append("\nتفاصيل العمليات حسب نوع التفصيل:\n")
    if (categorySummaries.isEmpty()) {
        sb.append("- لا توجد عمليات مسجلة\n")
    } else {
        categorySummaries.forEach { cat ->
            sb.append("- ${cat.categoryName}: ${cat.count} ثوب - ${decimalFormat.format(cat.totalDue)} ر.س\n")
        }
    }
    sb.append("\nإجمالي المستحق له: $grandDueFormatted ر.س\n")
    sb.append("إجمالي المصروفات: $expensesFormatted ر.س\n")
    sb.append("الصافي ($balanceStatus): $netFormatted ر.س")

    return sb.toString()
}


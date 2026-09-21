package com.example.ui.screens.reports

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.OrderWithCategory
import com.example.ui.viewmodel.CategoriesViewModel
import com.example.ui.viewmodel.CuttersViewModel
import com.example.ui.viewmodel.OrdersViewModel
import com.example.ui.viewmodel.TailorsViewModel
import java.util.Calendar

@Composable
fun CustomSearchReportScreen(
    ordersViewModel: OrdersViewModel,
    categoriesViewModel: CategoriesViewModel,
    cuttersViewModel: CuttersViewModel,
    tailorsViewModel: TailorsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)

    val categories by categoriesViewModel.allCategories.collectAsStateWithLifecycle()
    val cutters by cuttersViewModel.activeCutters.collectAsStateWithLifecycle()
    val tailors by tailorsViewModel.activeTailors.collectAsStateWithLifecycle()

    val initialStartCalendar = remember {
        Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
    }
    val initialEndCalendar = remember { Calendar.getInstance() }

    var selectedStartCalendar by remember { mutableStateOf(initialStartCalendar) }
    var selectedEndCalendar by remember { mutableStateOf(initialEndCalendar) }

    val isDateRangeValid = remember(selectedStartCalendar, selectedEndCalendar) {
        getStartOfDay(selectedEndCalendar) >= getStartOfDay(selectedStartCalendar)
    }

    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedCutterId by remember { mutableStateOf<Long?>(null) }
    var selectedTailorId by remember { mutableStateOf<Long?>(null) }

    var queryStartMillis by remember { mutableStateOf(getStartOfDay(initialStartCalendar)) }
    var queryEndMillis by remember { mutableStateOf(getEndOfDay(initialEndCalendar)) }
    var queryCategoryId by remember { mutableStateOf<Long?>(null) }
    var queryCutterId by remember { mutableStateOf<Long?>(null) }
    var queryTailorId by remember { mutableStateOf<Long?>(null) }

    val orders: List<OrderWithCategory> by remember(queryStartMillis, queryEndMillis, queryCategoryId, queryCutterId, queryTailorId) {
        ordersViewModel.getFilteredOrders(
            startTime = queryStartMillis,
            endTime = queryEndMillis,
            cutterId = queryCutterId,
            tailorId = queryTailorId,
            categoryId = queryCategoryId
        )
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    var showCategoryMenu by remember { mutableStateOf(false) }
    var showCutterMenu by remember { mutableStateOf(false) }
    var showTailorMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        ReportHeader(
            title = "بحث مخصص في التقارير",
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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
                        ReportDateField(
                            label = "تاريخ البداية:",
                            calendar = selectedStartCalendar,
                            onDateChanged = { selectedStartCalendar = it }
                        )

                        ReportDateField(
                            label = "تاريخ النهاية:",
                            calendar = selectedEndCalendar,
                            onDateChanged = { selectedEndCalendar = it }
                        )

                        // Category Filter
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text(
                                    text = "نوع التفصيل:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B),
                                    modifier = Modifier.padding(bottom = 2.dp)
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
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = categories.find { it.id == selectedCategoryId }?.name ?: "الكل (جميع أنواع التفصيل)",
                                            fontSize = 13.sp,
                                            color = Color(0xFF0F172A),
                                            fontWeight = FontWeight.Medium
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
                                        showCategoryMenu = false
                                    }
                                )
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.name) },
                                        onClick = {
                                            selectedCategoryId = cat.id
                                            showCategoryMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Workers Filter (Cutter & Tailor)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Cutter
                            Box(modifier = Modifier.weight(1f)) {
                                Column {
                                    Text(
                                        text = "القصاص:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B),
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color(0xFF94A3B8)),
                                        color = Color.White,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showCutterMenu = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = cutters.find { it.id == selectedCutterId }?.name ?: "الكل",
                                                fontSize = 13.sp,
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
                                    expanded = showCutterMenu,
                                    onDismissRequest = { showCutterMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("الكل") },
                                        onClick = {
                                            selectedCutterId = null
                                            showCutterMenu = false
                                        }
                                    )
                                    cutters.forEach { c ->
                                        DropdownMenuItem(
                                            text = { Text(c.name) },
                                            onClick = {
                                                selectedCutterId = c.id
                                                showCutterMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Tailor
                            Box(modifier = Modifier.weight(1f)) {
                                Column {
                                    Text(
                                        text = "الخياط:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B),
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color(0xFF94A3B8)),
                                        color = Color.White,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showTailorMenu = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = tailors.find { it.id == selectedTailorId }?.name ?: "الكل",
                                                fontSize = 13.sp,
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
                                    expanded = showTailorMenu,
                                    onDismissRequest = { showTailorMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("الكل") },
                                        onClick = {
                                            selectedTailorId = null
                                            showTailorMenu = false
                                        }
                                    )
                                    tailors.forEach { t ->
                                        DropdownMenuItem(
                                            text = { Text(t.name) },
                                            onClick = {
                                                selectedTailorId = t.id
                                                showTailorMenu = false
                                            }
                                        )
                                    }
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

                        ContinueButton(
                            onClick = {
                                if (isDateRangeValid) {
                                    queryStartMillis = getStartOfDay(selectedStartCalendar)
                                    queryEndMillis = getEndOfDay(selectedEndCalendar)
                                    queryCategoryId = selectedCategoryId
                                    queryCutterId = selectedCutterId
                                    queryTailorId = selectedTailorId
                                }
                            },
                            enabled = isDateRangeValid
                        )
                    }
                }
            }

            item {
                ReportSummarySection(
                    totalCount = orders.size,
                    orders = orders
                )
            }

            if (orders.isEmpty()) {
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
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "لا توجد نتائج مطابقة لشروط البحث المحددة",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF334155)
                                )
                            )
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = "نتائج البحث (${orders.size}):",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                items(orders, key = { it.order.id }) { item ->
                    ReportOrderRowItem(orderWithCategory = item)
                }
            }
        }
    }
}

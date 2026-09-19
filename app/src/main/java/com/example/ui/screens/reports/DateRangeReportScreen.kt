package com.example.ui.screens.reports

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.OrderWithCategory
import com.example.ui.viewmodel.OrdersViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DateRangeReportScreen(
    title: String,
    isYearly: Boolean,
    ordersViewModel: OrdersViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)

    // Default start date: 1st of year if yearly, 1st of month if monthly
    val initialStartCalendar = remember(isYearly) {
        Calendar.getInstance().apply {
            if (isYearly) {
                set(Calendar.MONTH, Calendar.JANUARY)
                set(Calendar.DAY_OF_MONTH, 1)
            } else {
                set(Calendar.DAY_OF_MONTH, 1)
            }
        }
    }

    // Default end date: today
    val initialEndCalendar = remember {
        Calendar.getInstance()
    }

    var selectedStartCalendar by remember { mutableStateOf(initialStartCalendar) }
    var selectedEndCalendar by remember { mutableStateOf(initialEndCalendar) }

    // Validation: end date must not be before start date
    val isDateRangeValid = remember(selectedStartCalendar, selectedEndCalendar) {
        getStartOfDay(selectedEndCalendar) >= getStartOfDay(selectedStartCalendar)
    }

    // Actively queried time window in Room
    var queryStartMillis by remember { mutableStateOf(getStartOfDay(initialStartCalendar)) }
    var queryEndMillis by remember { mutableStateOf(getEndOfDay(initialEndCalendar)) }

    // Live Room query results for the selected date range, newest first
    val orders: List<OrderWithCategory> by remember(queryStartMillis, queryEndMillis) {
        ordersViewModel.getOrdersBetween(queryStartMillis, queryEndMillis)
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Blue Header with Back button
        ReportHeader(
            title = title,
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Filter Controls Card: Start Date, End Date, and prominent Red Continue Button
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
                        // تاريخ البداية
                        ReportDateField(
                            label = "تاريخ البداية:",
                            calendar = selectedStartCalendar,
                            onDateChanged = { newCal ->
                                selectedStartCalendar = newCal
                            }
                        )

                        // تاريخ النهاية
                        ReportDateField(
                            label = "تاريخ النهاية:",
                            calendar = selectedEndCalendar,
                            onDateChanged = { newCal ->
                                selectedEndCalendar = newCal
                            }
                        )

                        // Validation Error Alert
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

                        // Prominent Red "استمرار" Button above bottom nav
                        ContinueButton(
                            onClick = {
                                if (isDateRangeValid) {
                                    queryStartMillis = getStartOfDay(selectedStartCalendar)
                                    queryEndMillis = getEndOfDay(selectedEndCalendar)
                                }
                            },
                            enabled = isDateRangeValid
                        )
                    }
                }
            }

            // Results Summary: Live total order count & breakdown by category (> 0)
            item {
                ReportSummarySection(
                    totalCount = orders.size,
                    orders = orders
                )
            }

            // Empty State or Orders List
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
                                imageVector = Icons.Default.EventBusy,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "لا توجد عمليات مسجلة في هذه الفترة",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF334155)
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "من ${dateFormat.format(selectedStartCalendar.time)} إلى ${dateFormat.format(selectedEndCalendar.time)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
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

                items(orders, key = { it.order.id }) { orderWithCat ->
                    ReportOrderRowItem(orderWithCategory = orderWithCat)
                }
            }
        }
    }
}

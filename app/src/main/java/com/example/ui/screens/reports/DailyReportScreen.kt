package com.example.ui.screens.reports

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
fun DailyReportScreen(
    ordersViewModel: OrdersViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)

    // Current selected date in picker (defaults to today)
    var selectedCalendar by remember { mutableStateOf(Calendar.getInstance()) }

    // Date range actively queried in Room (updated when "استمرار" is tapped, and on initial load)
    var queryStartMillis by remember { mutableStateOf(getStartOfDay(selectedCalendar)) }
    var queryEndMillis by remember { mutableStateOf(getEndOfDay(selectedCalendar)) }

    // Live Room query results for the selected day, newest first
    val orders: List<OrderWithCategory> by remember(queryStartMillis, queryEndMillis) {
        ordersViewModel.getOrdersBetween(queryStartMillis, queryEndMillis)
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    val dayDisplayFormat = remember { SimpleDateFormat("yyyy/MM/dd - EEEE", Locale("ar")) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Blue Header with Back button
        ReportHeader(
            title = "كشف حساب يومي",
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Control Card: Date Field & Prominent Red Continue Button
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ReportDateField(
                            label = "اختيار اليوم:",
                            calendar = selectedCalendar,
                            onDateChanged = { newCal ->
                                selectedCalendar = newCal
                            }
                        )

                        ContinueButton(
                            onClick = {
                                queryStartMillis = getStartOfDay(selectedCalendar)
                                queryEndMillis = getEndOfDay(selectedCalendar)
                            }
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

            // Empty state or Orders List
            if (orders.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
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
                                text = "لا توجد عمليات مسجلة في هذا اليوم",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF334155)
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dayDisplayFormat.format(selectedCalendar.time),
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

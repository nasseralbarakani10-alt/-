package com.example.ui.screens.reports

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderWithCategory
import com.example.ui.theme.BlueDark
import com.example.ui.theme.BlueLight
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.RedButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun getStartOfDay(calendar: Calendar): Long {
    val cal = calendar.clone() as Calendar
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

fun getEndOfDay(calendar: Calendar): Long {
    val cal = calendar.clone() as Calendar
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    return cal.timeInMillis
}

@Composable
fun ReportHeader(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = BluePrimary,
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("btn_report_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "الرجوع لقائمة التقارير",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )
        }
    }
}

@Composable
fun ReportDateField(
    label: String,
    calendar: Calendar,
    onDateChanged: (Calendar) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd (EEEE)", Locale("ar")) }
    val formattedDate = remember(calendar.timeInMillis) { dateFormat.format(calendar.time) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFF94A3B8)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Calendar button opens native DatePickerDialog
                IconButton(
                    onClick = {
                        val dpd = DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val newCal = (calendar.clone() as Calendar).apply {
                                    set(Calendar.YEAR, y)
                                    set(Calendar.MONTH, m)
                                    set(Calendar.DAY_OF_MONTH, d)
                                }
                                onDateChanged(newCal)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                        dpd.show()
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "اختيار التاريخ",
                        tint = BluePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Date Text - also clickable to open DatePicker
                Text(
                    text = formattedDate,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .clickable {
                            val dpd = DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val newCal = (calendar.clone() as Calendar).apply {
                                        set(Calendar.YEAR, y)
                                        set(Calendar.MONTH, m)
                                        set(Calendar.DAY_OF_MONTH, d)
                                    }
                                    onDateChanged(newCal)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            )
                            dpd.show()
                        }
                )

                // Down arrow (-1 day)
                IconButton(
                    onClick = {
                        val newCal = (calendar.clone() as Calendar).apply {
                            add(Calendar.DAY_OF_MONTH, -1)
                        }
                        onDateChanged(newCal)
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "اليوم السابق",
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Up arrow (+1 day)
                IconButton(
                    onClick = {
                        val newCal = (calendar.clone() as Calendar).apply {
                            add(Calendar.DAY_OF_MONTH, 1)
                        }
                        onDateChanged(newCal)
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "اليوم التالي",
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ContinueButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = RedButton,
            contentColor = Color.White,
            disabledContainerColor = RedButton.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("btn_continue")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "استمرار",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportSummarySection(
    totalCount: Int,
    orders: List<OrderWithCategory>,
    modifier: Modifier = Modifier
) {
    val categoryBreakdown = remember(orders) {
        orders.groupBy { it.category?.name ?: "غير محدد" }
            .mapValues { it.value.size }
            .filter { it.value > 0 }
            .toList()
            .sortedByDescending { it.second }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Live Total Order Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "إجمالي العمليات: $totalCount",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A)
                    ),
                    modifier = Modifier.testTag("total_orders_count")
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = BlueLight,
                    border = BorderStroke(1.dp, Color(0xFF90CAF9))
                ) {
                    Text(
                        text = "مباشر",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            if (categoryBreakdown.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "تفصيل حسب النوع:",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569),
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categoryBreakdown.forEach { (catName, count) ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF0FDF4),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$catName: ",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                                Text(
                                    text = "$count",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportOrderRowItem(
    orderWithCategory: OrderWithCategory,
    modifier: Modifier = Modifier
) {
    val order = orderWithCategory.order
    val categoryName = orderWithCategory.category?.name ?: "غير محدد"
    val cutterName = orderWithCategory.cutter?.name ?: "الافتراضي"
    val tailorName = orderWithCategory.tailor?.name ?: "الافتراضي"

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
            // Line 1: Customer Name, Category Tag, Customer Number
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
                        text = order.customerName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color(0xFF000000)
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(#${order.customerNumber})",
                        fontSize = 11.5.sp,
                        color = Color(0xFF475569)
                    )
                }

                // Category badge
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

            Spacer(modifier = Modifier.height(3.dp))

            // Line 2: Phone, Fabric, and Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (order.phoneNumber.isNotBlank()) {
                        Text(
                            text = "هاتف: ${order.phoneNumber}",
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

                Text(
                    text = formattedTime,
                    fontSize = 10.5.sp,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Line 3: Workers & Status flags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "القصاص: $cutterName",
                        fontSize = 11.sp,
                        color = Color(0xFF334155)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "الخياط: $tailorName",
                        fontSize = 11.sp,
                        color = Color(0xFF334155)
                    )
                }

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

@Composable
fun ReportMiniBadge(
    text: String,
    color: Color,
    bgColor: Color
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bgColor,
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
        )
    }
}

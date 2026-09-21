package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderWithCategory

@Composable
fun DailyOrderSeparator(
    dayName: String,
    fullDate: String,
    orders: List<OrderWithCategory>,
    modifier: Modifier = Modifier
) {
    // Calculate live category breakdown for this day only
    val categoryCounts = remember(orders) {
        val counts = linkedMapOf<String, Int>()
        orders.forEach { item ->
            val catName = item.category?.name ?: "غير محدد"
            counts[catName] = (counts[catName] ?: 0) + 1
        }
        counts.filter { it.value > 0 }
    }

    val breakdownText = remember(categoryCounts) {
        if (categoryCounts.isEmpty()) ""
        else categoryCounts.entries.joinToString(separator = "   ") { (cat, count) ->
            "$cat $count"
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_separator_${dayName}_$fullDate"),
        shape = RectangleShape,
        shadowElevation = 0.5.dp,
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF0288D1),
                            Color(0xFF29B6F6),
                            Color(0xFF0288D1)
                        )
                    )
                )
                .padding(horizontal = 8.dp, vertical = 2.5.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Line 1: Compact shortened label "كشف متابعة العمل  {day name}  {date}"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "كشف متابعة العمل  $dayName  $fullDate",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1
                )

                Text(
                    text = "${orders.size} عملية",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }

            // Line 2: Small live breakdown of order counts per category for that specific day only
            if (breakdownText.isNotBlank()) {
                Text(
                    text = breakdownText,
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 9.5.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }
    }
}

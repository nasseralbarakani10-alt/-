package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderWithCategory
import com.example.ui.theme.BlueDark
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.RedButton

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrderCard(
    orderWithCategory: OrderWithCategory,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onToggleLaundry: () -> Unit,
    onToggleButtonIroning: () -> Unit = {},
    onToggleReady: () -> Unit = {},
    onOpenDetail: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val order = orderWithCategory.order
    val categoryName = orderWithCategory.category?.name ?: "غير محدد"
    val cutterName = orderWithCategory.cutter?.name ?: "الافتراضي"
    val tailorName = orderWithCategory.tailor?.name ?: "الافتراضي"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("order_card_${order.id}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE1F5FE) else Color.White
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) BluePrimary else Color(0xFFCFD8DC)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox and "تعديل" area (split side by side: right = checkbox, left = "تعديل")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.testTag("order_checkbox_area_${order.id}")
            ) {
                // Right half: Existing selection checkbox (□) for حذف المحدد
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    modifier = Modifier
                        .size(22.dp)
                        .testTag("order_checkbox_${order.id}"),
                    colors = CheckboxDefaults.colors(
                        checkedColor = BluePrimary,
                        uncheckedColor = Color(0xFF455A64)
                    )
                )

                // Left half: Small red-colored "تعديل" label that opens customer detail/edit screen
                Text(
                    text = "تعديل",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = RedButton,
                    modifier = Modifier
                        .clickable { onOpenDetail() }
                        .padding(horizontal = 2.dp, vertical = 2.dp)
                        .testTag("order_edit_label_${order.id}")
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // ALL customer fields in FlowRow wrapping naturally to fit screen width without horizontal scrolling
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Customer identity group (Name, Number, Phone) - tapping opens customer detail
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onOpenDetail() }
                            .testTag("customer_detail_click_${order.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = BlueDark,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = order.customerName,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF000000)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "[#${order.customerNumber}]",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0277BD)
                        )
                        if (order.phoneNumber.isNotBlank()) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "(${order.phoneNumber})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF000000)
                            )
                        }
                    }

                    Text(
                        text = "•",
                        fontSize = 11.sp,
                        color = Color(0xFF757575),
                        fontWeight = FontWeight.Bold
                    )

                    // الصنف (Fabric Type)
                    Text(
                        text = "الصنف: ${if (order.fabricType.isNotBlank()) order.fabricType else "—"}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF000000)
                    )

                    Text(
                        text = "•",
                        fontSize = 11.sp,
                        color = Color(0xFF757575),
                        fontWeight = FontWeight.Bold
                    )

                    // نوع التفصيل (Category Name)
                    Text(
                        text = "نوع التفصيل: $categoryName",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF000000)
                    )

                    Text(
                        text = "•",
                        fontSize = 11.sp,
                        color = Color(0xFF757575),
                        fontWeight = FontWeight.Bold
                    )

                    // القصاص (Cutter Name)
                    Text(
                        text = "القصاص: $cutterName",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF000000)
                    )

                    Text(
                        text = "•",
                        fontSize = 11.sp,
                        color = Color(0xFF757575),
                        fontWeight = FontWeight.Bold
                    )

                    // الخياط (Tailor Name)
                    Text(
                        text = "الخياط: $tailorName",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF000000)
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // ROW 2: Status badges (زرار وكي، المغسلة، جاهز) in FlowRow to wrap cleanly
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    // 1. زرار وكي badge (interactive toggle)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (order.buttonIroning) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                        border = BorderStroke(
                            1.dp,
                            if (order.buttonIroning) Color(0xFF86EFAC) else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier
                            .clickable { onToggleButtonIroning() }
                            .testTag("order_button_ironing_badge_${order.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (order.buttonIroning) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF15803D),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                            }
                            Text(
                                text = "زرار وكي",
                                color = if (order.buttonIroning) Color(0xFF15803D) else Color(0xFF000000),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 2. المغسلة badge (interactive toggle)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (order.laundry) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                        border = BorderStroke(
                            1.dp,
                            if (order.laundry) Color(0xFF86EFAC) else Color(0xFFFCA5A5)
                        ),
                        modifier = Modifier
                            .clickable { onToggleLaundry() }
                            .testTag("order_laundry_badge_${order.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (order.laundry) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (order.laundry) Color(0xFF15803D) else Color(0xFFB91C1C),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (order.laundry) "المغسلة: نعم" else "المغسلة: لا",
                                color = if (order.laundry) Color(0xFF15803D) else Color(0xFFB91C1C),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 3. جاهز badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (order.ready) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                        border = BorderStroke(
                            1.dp,
                            if (order.ready) Color(0xFF86EFAC) else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier
                            .clickable { onToggleReady() }
                            .testTag("order_ready_badge_${order.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (order.ready) "جاهز✅" else "قيد العمل",
                                color = if (order.ready) Color(0xFF15803D) else Color(0xFF000000),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

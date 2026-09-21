package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.example.data.model.Customer
import com.example.data.model.Order
import com.example.data.model.OrderWithCategory
import com.example.ui.theme.BlueDark
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.RedButton

/**
 * Single Client Card displaying:
 * 1. Client information (Name, Number, Phone) ONCE.
 * 2. Directly below the phone number: "عدد الطلبات: X" (where X is the total number of saved orders belonging to that client).
 * 3. Directly below the client information: all orders belonging to that client,
 *    each appearing as a separate order row/section inside this same client card without repeating client information.
 */
@Composable
fun CustomerCard(
    customer: Customer,
    orders: List<OrderWithCategory>,
    totalOrdersCount: Int,
    selectedIds: Set<Long>,
    onToggleSelect: (Long) -> Unit,
    onToggleLaundry: (Order) -> Unit,
    onToggleButtonIroning: (Order) -> Unit,
    onToggleReady: (OrderWithCategory) -> Unit,
    onOpenOrderDetail: (OrderWithCategory) -> Unit,
    onOpenCustomerDetail: (Customer) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val anyOrderSelected = orders.any { selectedIds.contains(it.order.id) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("customer_card_${customer.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (anyOrderSelected) Color(0xFFF8FAFC) else Color.White
        ),
        border = BorderStroke(
            width = if (anyOrderSelected) 1.5.dp else 1.dp,
            color = if (anyOrderSelected) BluePrimary else Color(0xFFCBD5E1)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // ==========================================
            // CLIENT INFORMATION SECTION (SHOWN ONLY ONCE)
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F5F9))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Line 1: Name, [#Number], (Phone)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenCustomerDetail(customer) }
                        .testTag("customer_header_${customer.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = BlueDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = customer.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "[#${customer.customerNumber}]",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7)
                    )
                    if (customer.phoneNumber.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${customer.phoneNumber})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Line 2: DIRECTLY BELOW THE PHONE NUMBER: عدد الطلبات: X
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 22.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE0F2FE),
                        border = BorderStroke(1.dp, Color(0xFFBAE6FD))
                    ) {
                        Text(
                            text = "عدد الطلبات: $totalOrdersCount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0369A1),
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                .testTag("customer_orders_count_${customer.id}")
                        )
                    }
                }
            }

            HorizontalDivider(
                color = Color(0xFFCBD5E1),
                thickness = 1.dp
            )

            // ==========================================
            // ORDERS SECTION (DIRECTLY BELOW CLIENT INFO)
            // ==========================================
            orders.forEachIndexed { index, orderWithCategory ->
                if (index > 0) {
                    HorizontalDivider(
                        color = Color(0xFFE2E8F0),
                        thickness = 1.dp
                    )
                }

                ClientOrderRow(
                    orderWithCategory = orderWithCategory,
                    sequenceNumber = orderWithCategory.order.sequenceNumber,
                    showSequenceBadge = orders.size > 1,
                    isSelected = selectedIds.contains(orderWithCategory.order.id),
                    onToggleSelect = { onToggleSelect(orderWithCategory.order.id) },
                    onToggleLaundry = { onToggleLaundry(orderWithCategory.order) },
                    onToggleButtonIroning = { onToggleButtonIroning(orderWithCategory.order) },
                    onToggleReady = { onToggleReady(orderWithCategory) },
                    onOpenDetail = { onOpenOrderDetail(orderWithCategory) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ClientOrderRow(
    orderWithCategory: OrderWithCategory,
    sequenceNumber: Int,
    showSequenceBadge: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onToggleLaundry: () -> Unit,
    onToggleButtonIroning: () -> Unit,
    onToggleReady: () -> Unit,
    onOpenDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val order = orderWithCategory.order
    val categoryName = orderWithCategory.category?.name ?: "غير محدد"
    val cutterName = orderWithCategory.cutter?.name ?: "الافتراضي"
    val tailorName = orderWithCategory.tailor?.name ?: "الافتراضي"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isSelected) Color(0xFFE1F5FE) else Color.White)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("order_card_${order.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selection checkbox and "تعديل" button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.testTag("order_checkbox_area_${order.id}")
        ) {
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
            // ORDER INFORMATION (without repeating client name, number, or phone)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // If client has multiple orders, show small sequence label "طلب 1", "طلب 2"
                if (showSequenceBadge) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFE2E8F0)
                    ) {
                        Text(
                            text = "طلب $sequenceNumber",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                }

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

            // STATUS BADGES (زرار وكي، المغسلة، جاهز)
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

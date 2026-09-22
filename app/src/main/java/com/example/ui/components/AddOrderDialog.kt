package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Category
import com.example.data.model.Cutter
import com.example.data.model.Tailor
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.GreenButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrderDialog(
    categories: List<Category>,
    cutters: List<Cutter>,
    tailors: List<Tailor>,
    onDismiss: () -> Unit,
    onSave: (
        customerName: String,
        customerNumber: String,
        phoneNumber: String,
        categoryId: Long,
        fabricType: String,
        cutterId: Long?,
        tailorId: Long?,
        buttonIroning: Boolean,
        laundry: Boolean
    ) -> Unit
) {
    // 1. اسم العميل
    var customerName by remember { mutableStateOf("") }
    // 2. رقم العميل
    var customerNumber by remember { mutableStateOf("") }
    // 3. رقم الهاتف
    var phoneNumber by remember { mutableStateOf("") }
    // 4. النوع
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull()) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    // 5. الصنف
    var fabricType by remember { mutableStateOf("") }
    // 6. القصاص (null = "الافتراضي")
    var selectedCutter by remember { mutableStateOf<Cutter?>(null) }
    var cutterDropdownExpanded by remember { mutableStateOf(false) }
    // 7. الخياط (null = "الافتراضي")
    var selectedTailor by remember { mutableStateOf<Tailor?>(null) }
    var tailorDropdownExpanded by remember { mutableStateOf(false) }
    // 8. زرار وكي & المغسلة
    var buttonIroning by remember { mutableStateOf(false) }
    var laundry by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "إضافة عميل جديد",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary,
                            fontSize = 20.sp
                        )
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_add_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color(0xFF212121),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error message banner
                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFEBEE),
                        border = BorderStroke(1.dp, Color(0xFFEF9A9A)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFC62828),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // 1. اسم العميل (text)
                AppTextField(
                    value = customerName,
                    onValueChange = {
                        customerName = it
                        errorMessage = null
                    },
                    label = "اسم العميل",
                    placeholder = "مثال: أحمد محمد السعيد",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF1565C0)
                        )
                    },
                    modifier = Modifier.testTag("input_customer_name")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 2. رقم العميل (text)
                AppTextField(
                    value = customerNumber,
                    onValueChange = {
                        customerNumber = it
                        errorMessage = null
                    },
                    label = "رقم العميل",
                    placeholder = "مثال: 1045",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = Color(0xFF1565C0)
                        )
                    },
                    modifier = Modifier.testTag("input_customer_number")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3. رقم الهاتف (text, phone keyboard type)
                AppTextField(
                    value = phoneNumber,
                    onValueChange = {
                        phoneNumber = it
                        errorMessage = null
                    },
                    label = "رقم الهاتف",
                    placeholder = "مثال: 0501234567",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = Color(0xFF1565C0)
                        )
                    },
                    modifier = Modifier.testTag("input_phone_number")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 4. نوع التفصيل — ExposedDropdownMenuBox
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text(
                                text = "نوع التفصيل",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        },
                        placeholder = {
                            Text(
                                text = "اختر نوع التفصيل (مثل: قطري، سعودي...)",
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    color = Color(0xFF757575),
                                    fontWeight = FontWeight.Normal
                                )
                            )
                        },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.SemiBold
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = Color(0xFF1565C0)
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded)
                        },
                        colors = appTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                            .testTag("category_dropdown_field")
                    )

                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${category.sortOrder}. ${category.name}",
                                        fontSize = 16.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                onClick = {
                                    selectedCategory = category
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. الصنف — free text field (fabric type description, separate from النوع)
                AppTextField(
                    value = fabricType,
                    onValueChange = {
                        fabricType = it
                        errorMessage = null
                    },
                    label = "الصنف",
                    placeholder = "مثال: ياباني فاخر، كوري، سميرا ميس...",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Style,
                            contentDescription = null,
                            tint = Color(0xFF1565C0)
                        )
                    },
                    modifier = Modifier.testTag("input_fabric_type")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 6. القصاص — ExposedDropdownMenuBox showing only Cutters where isActive = true with "الافتراضي" at top
                ExposedDropdownMenuBox(
                    expanded = cutterDropdownExpanded,
                    onExpandedChange = { cutterDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCutter?.name ?: "الافتراضي",
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text(
                                text = "القصاص",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.SemiBold
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.ContentCut,
                                contentDescription = null,
                                tint = Color(0xFF1565C0)
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = cutterDropdownExpanded)
                        },
                        colors = appTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                            .testTag("cutter_dropdown_field")
                    )

                    ExposedDropdownMenu(
                        expanded = cutterDropdownExpanded,
                        onDismissRequest = { cutterDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "الافتراضي",
                                    fontSize = 16.sp,
                                    color = Color(0xFF1565C0),
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            onClick = {
                                selectedCutter = null
                                cutterDropdownExpanded = false
                            }
                        )
                        cutters.forEach { cutter ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = cutter.name,
                                        fontSize = 16.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                onClick = {
                                    selectedCutter = cutter
                                    cutterDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 7. الخياط — ExposedDropdownMenuBox with "الافتراضي" at top
                ExposedDropdownMenuBox(
                    expanded = tailorDropdownExpanded,
                    onExpandedChange = { tailorDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedTailor?.name ?: "الافتراضي",
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text(
                                text = "الخياط",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.SemiBold
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Engineering,
                                contentDescription = null,
                                tint = Color(0xFF1565C0)
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = tailorDropdownExpanded)
                        },
                        colors = appTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                            .testTag("tailor_dropdown_field")
                    )

                    ExposedDropdownMenu(
                        expanded = tailorDropdownExpanded,
                        onDismissRequest = { tailorDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "الافتراضي",
                                    fontSize = 16.sp,
                                    color = Color(0xFF1565C0),
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            onClick = {
                                selectedTailor = null
                                tailorDropdownExpanded = false
                            }
                        )
                        tailors.forEach { tailor ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = tailor.name,
                                        fontSize = 16.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                onClick = {
                                    selectedTailor = tailor
                                    tailorDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 8. Two checkboxes: "زرار وكي" and "المغسلة" with clear dark visible labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Checkbox 1: "زرار وكي"
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (buttonIroning) Color(0xFFE8F5E9) else Color(0xFFFAFAFA),
                        border = BorderStroke(
                            1.5.dp,
                            if (buttonIroning) Color(0xFF2E7D32) else Color(0xFF9E9E9E)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { buttonIroning = !buttonIroning }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Checkbox(
                                checked = buttonIroning,
                                onCheckedChange = { buttonIroning = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF2E7D32),
                                    uncheckedColor = Color(0xFF424242)
                                ),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "زرار وكي",
                                color = Color(0xFF000000),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // Checkbox 2: "المغسلة"
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (laundry) Color(0xFFE3F2FD) else Color(0xFFFAFAFA),
                        border = BorderStroke(
                            1.5.dp,
                            if (laundry) Color(0xFF1565C0) else Color(0xFF9E9E9E)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { laundry = !laundry }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Checkbox(
                                checked = laundry,
                                onCheckedChange = { laundry = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF1565C0),
                                    uncheckedColor = Color(0xFF424242)
                                ),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "المغسلة",
                                color = Color(0xFF000000),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, Color(0xFF757575)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("cancel_button")
                    ) {
                        Text(
                            text = "إلغاء",
                            color = Color(0xFF424242),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            val cat = selectedCategory ?: categories.firstOrNull()
                            if (customerName.isBlank()) {
                                errorMessage = "يرجى إدخال اسم العميل"
                            } else if (customerNumber.isBlank()) {
                                errorMessage = "يرجى إدخال رقم العميل"
                            } else if (phoneNumber.isBlank()) {
                                errorMessage = "يرجى إدخال رقم الهاتف"
                            } else if (cat == null) {
                                errorMessage = "يرجى اختيار نوع التفصيل"
                            } else if (fabricType.isBlank()) {
                                errorMessage = "يرجى إدخال الصنف"
                            } else {
                                onSave(
                                    customerName.trim(),
                                    customerNumber.trim(),
                                    phoneNumber.trim(),
                                    cat.id,
                                    fabricType.trim(),
                                    selectedCutter?.id, // null if "الافتراضي"
                                    selectedTailor?.id, // null if "الافتراضي"
                                    buttonIroning,
                                    laundry
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenButton),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("save_order_button")
                    ) {
                        Text(
                            text = "حفظ",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

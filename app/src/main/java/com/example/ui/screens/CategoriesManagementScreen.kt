package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Category
import com.example.ui.components.appTextFieldColors
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.GreenButton
import com.example.ui.theme.RedButton
import com.example.ui.viewmodel.CategoriesViewModel
import com.example.ui.viewmodel.CategoryOpResult
import kotlinx.coroutines.launch

@Composable
fun CategoriesManagementScreen(
    categoriesViewModel: CategoriesViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories by categoriesViewModel.allCategories.collectAsStateWithLifecycle()
    val sortedCategories = remember(categories) {
        categories.sortedWith(compareBy<Category> { it.sortOrder }.thenBy { it.id })
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }

    // Deletion states
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }
    var cannotDeleteReason by remember { mutableStateOf<String?>(null) }
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
        ) {
            // App Bar
            Surface(
                color = BluePrimary,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("categories_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "إدارة الأنواع",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        )
                    )
                }
            }

            // Sub-header with Add Button
            Surface(
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "قائمة أنواع التفصيل",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "إجمالي: ${sortedCategories.size} نوع",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenButton),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("add_category_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إضافة نوع",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Categories List
            if (sortedCategories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "لا توجد أنواع مسجلة",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569)
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                        .testTag("categories_list"),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = sortedCategories, key = { it.id }) { category ->
                        val isDefault = category.isDefault || category.sortOrder in 1..15
                        CategoryRowItem(
                            category = category,
                            isDefault = isDefault,
                            onEdit = { editingCategory = category },
                            onDelete = {
                                scope.launch {
                                    val (canDelete, reason) = categoriesViewModel.canDeleteCategory(category)
                                    if (!canDelete) {
                                        cannotDeleteReason = reason
                                    } else {
                                        categoryToDelete = category
                                        showConfirmDeleteDialog = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }

    // ADD CATEGORY DIALOG
    if (showAddDialog) {
        CategoryInputDialog(
            title = "إضافة نوع تفصيل جديد",
            initialName = "",
            confirmButtonText = "حفظ",
            onDismiss = { showAddDialog = false },
            onSave = { name, onError ->
                categoriesViewModel.addCategory(name) { result ->
                    when (result) {
                        is CategoryOpResult.Success -> {
                            showAddDialog = false
                            scope.launch { snackbarHostState.showSnackbar(result.message) }
                        }
                        is CategoryOpResult.Error -> {
                            onError(result.message)
                        }
                    }
                }
            }
        )
    }

    // EDIT CATEGORY DIALOG
    if (editingCategory != null) {
        val cat = editingCategory!!
        CategoryInputDialog(
            title = "تعديل اسم النوع",
            initialName = cat.name,
            confirmButtonText = "حفظ التعديل",
            onDismiss = { editingCategory = null },
            onSave = { name, onError ->
                categoriesViewModel.updateCategory(cat, name) { result ->
                    when (result) {
                        is CategoryOpResult.Success -> {
                            editingCategory = null
                            scope.launch { snackbarHostState.showSnackbar(result.message) }
                        }
                        is CategoryOpResult.Error -> {
                            onError(result.message)
                        }
                    }
                }
            }
        )
    }

    // CANNOT DELETE DIALOG (Default category or has orders)
    if (cannotDeleteReason != null) {
        AlertDialog(
            onDismissRequest = { cannotDeleteReason = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFFC62828),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "تعذر الحذف",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFFC62828),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = cannotDeleteReason!!,
                    fontSize = 15.sp,
                    color = Color(0xFF212121),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = { cannotDeleteReason = null },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("حسناً", color = Color.White)
                }
            }
        )
    }

    // CONFIRM DELETE DIALOG (Only for non-default, 0 orders)
    if (showConfirmDeleteDialog && categoryToDelete != null) {
        val cat = categoryToDelete!!
        AlertDialog(
            onDismissRequest = {
                showConfirmDeleteDialog = false
                categoryToDelete = null
            },
            title = {
                Text(
                    text = "تأكيد حذف النوع",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = RedButton
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من رغبتك في حذف النوع \"${cat.name}\"؟",
                    fontSize = 15.sp,
                    color = Color(0xFF212121)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDeleteDialog = false
                        categoryToDelete = null
                        categoriesViewModel.deleteCategory(cat) { result ->
                            scope.launch {
                                when (result) {
                                    is CategoryOpResult.Success -> snackbarHostState.showSnackbar(result.message)
                                    is CategoryOpResult.Error -> snackbarHostState.showSnackbar(result.message)
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedButton),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("حذف", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmDeleteDialog = false
                        categoryToDelete = null
                    }
                ) {
                    Text("إلغاء", color = Color(0xFF616161))
                }
            }
        )
    }
}

@Composable
private fun CategoryRowItem(
    category: Category,
    isDefault: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("category_row_${category.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Sort order badge (#1, #2...)
                Surface(
                    shape = CircleShape,
                    color = if (isDefault) Color(0xFFE2E8F0) else Color(0xFFE0F2FE),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = category.sortOrder.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isDefault) Color(0xFF475569) else BluePrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Default vs Custom Badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isDefault) Color(0xFFF1F5F9) else Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, if (isDefault) Color(0xFFCBD5E1) else Color(0xFFFDE68A))
                ) {
                    Text(
                        text = if (isDefault) "افتراضي" else "مخصص",
                        color = if (isDefault) Color(0xFF475569) else Color(0xFF92400E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Actions: Edit + Delete
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("edit_category_${category.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "تعديل",
                        tint = Color(0xFF1565C0),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_category_${category.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف",
                        tint = if (isDefault) Color(0xFFB0BEC5) else RedButton,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryInputDialog(
    title: String,
    initialName: String,
    confirmButtonText: String,
    onDismiss: () -> Unit,
    onSave: (name: String, onError: (String) -> Unit) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary,
                            fontSize = 19.sp
                        )
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_category_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color(0xFF212121),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("اسم النوع") },
                    placeholder = { Text("مثال: دشداشة، ثوب كويتي...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = BluePrimary
                        )
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    ),
                    colors = appTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_name_input")
                )

                Spacer(modifier = Modifier.height(24.dp))

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
                            .height(48.dp)
                            .testTag("cancel_category_button")
                    ) {
                        Text(
                            text = "إلغاء",
                            color = Color(0xFF424242),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            val trimmed = name.trim()
                            if (trimmed.isBlank()) {
                                errorMessage = "يرجى إدخال اسم النوع"
                            } else {
                                onSave(trimmed) { err ->
                                    errorMessage = err
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenButton),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("save_category_button")
                    ) {
                        Text(
                            text = confirmButtonText,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

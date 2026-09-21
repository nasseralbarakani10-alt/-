package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Cutter
import com.example.data.model.Tailor
import com.example.ui.components.WorkerFormDialog
import com.example.ui.components.WorkerPricesDialog
import com.example.ui.screens.reports.WorkerOption
import com.example.ui.screens.reports.WorkerReportScreen
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.GreenButton
import com.example.ui.theme.RedButton
import com.example.ui.viewmodel.CategoriesViewModel
import com.example.ui.viewmodel.CuttersViewModel
import com.example.ui.viewmodel.OrdersViewModel
import com.example.ui.viewmodel.TailorsViewModel
import kotlinx.coroutines.launch

@Composable
fun WorkersScreen(
    cuttersViewModel: CuttersViewModel,
    tailorsViewModel: TailorsViewModel,
    categoriesViewModel: CategoriesViewModel,
    ordersViewModel: OrdersViewModel? = null,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val cutters by cuttersViewModel.allCutters.collectAsStateWithLifecycle()
    val tailors by tailorsViewModel.allTailors.collectAsStateWithLifecycle()
    val categories by categoriesViewModel.allCategories.collectAsStateWithLifecycle()

    var activeWorkerReportType by remember { mutableStateOf<Int?>(null) } // 0: Cutters Report, 1: Tailors Report

    if (ordersViewModel != null && activeWorkerReportType != null) {
        if (activeWorkerReportType == 0) {
            WorkerReportScreen(
                title = "تقارير القصاصين",
                workerLabel = "القصاص",
                workers = cutters.map { WorkerOption(it.id, it.name, it.isActive, it.phoneNumber) },
                isCutter = true,
                ordersViewModel = ordersViewModel,
                categoriesViewModel = categoriesViewModel,
                onFetchSavedExpense = { wId, start, end ->
                    cuttersViewModel.getExpenseDirect(wId, start, end)
                },
                onSaveExpense = { wId, start, end, amt ->
                    cuttersViewModel.saveExpense(wId, start, end, amt)
                },
                onBackClick = { activeWorkerReportType = null }
            )
            return
        } else if (activeWorkerReportType == 1) {
            WorkerReportScreen(
                title = "تقارير الخياطين",
                workerLabel = "الخياط",
                workers = tailors.map { WorkerOption(it.id, it.name, it.isActive, it.phoneNumber) },
                isCutter = false,
                ordersViewModel = ordersViewModel,
                categoriesViewModel = categoriesViewModel,
                onFetchSavedExpense = { wId, start, end ->
                    tailorsViewModel.getExpenseDirect(wId, start, end)
                },
                onSaveExpense = { wId, start, end, amt ->
                    tailorsViewModel.saveExpense(wId, start, end, amt)
                },
                onBackClick = { activeWorkerReportType = null }
            )
            return
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Dialog state for adding/editing
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCutter by remember { mutableStateOf<Cutter?>(null) }
    var editingTailor by remember { mutableStateOf<Tailor?>(null) }

    // Dialog states for price management
    var pricingCutter by remember { mutableStateOf<Pair<Cutter, Map<Long, Double>>?>(null) }
    var pricingTailor by remember { mutableStateOf<Pair<Tailor, Map<Long, Double>>?>(null) }

    // Dialog states for two-step deletion
    var pendingDeleteCutter by remember { mutableStateOf<Cutter?>(null) }
    var pendingDeleteTailor by remember { mutableStateOf<Tailor?>(null) }
    var pendingDeleteHasOrders by remember { mutableStateOf(false) }
    var showFirstConfirmDialog by remember { mutableStateOf(false) }
    var showSecondConfirmDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
        ) {
            // App Bar Header
            Surface(
                color = BluePrimary,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "بيانات القصاصين والخياطين",
                        color = Color(0xFF000000),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Tab Row: Switch between القصاصين and الخياطين
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = Color(0xFF000000),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = BluePrimary,
                        height = 3.dp
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCut,
                                contentDescription = null,
                                tint = Color(0xFF000000),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "القصاصين (${cutters.size})",
                                fontSize = 14.sp,
                                color = Color(0xFF000000),
                                fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.SemiBold
                            )
                        }
                    },
                    modifier = Modifier.testTag("cutters_tab")
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Engineering,
                                contentDescription = null,
                                tint = Color(0xFF000000),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "الخياطين (${tailors.size})",
                                fontSize = 14.sp,
                                color = Color(0xFF000000),
                                fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.SemiBold
                            )
                        }
                    },
                    modifier = Modifier.testTag("tailors_tab")
                )
            }

            // Top Action Bar with prominent green Add Button
            Surface(
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedTabIndex == 0) "قائمة القصاصين" else "قائمة الخياطين",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color(0xFF000000)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (ordersViewModel != null) {
                            OutlinedButton(
                                onClick = { activeWorkerReportType = selectedTabIndex },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, BluePrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier.testTag(if (selectedTabIndex == 0) "cutter_reports_btn" else "tailor_reports_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Assessment,
                                        contentDescription = null,
                                        tint = Color(0xFF000000),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (selectedTabIndex == 0) "التقرير المالي" else "التقرير المالي",
                                        color = Color(0xFF000000),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenButton),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag(if (selectedTabIndex == 0) "add_cutter_button" else "add_tailor_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (selectedTabIndex == 0) "إضافة قصاص" else "إضافة خياط",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            }
                        }
                    }
                }
            }

            // Content for Cutters / Tailors
            if (selectedTabIndex == 0) {
                // CUTTERS LIST
                if (cutters.isEmpty()) {
                    WorkerEmptyState(
                        title = "لا يوجد قصاصين مسجلين",
                        subtitle = "اضغط على زر \"إضافة قصاص\" لبدء إضافة بيانات القصاصين",
                        icon = Icons.Default.ContentCut
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp)
                            .testTag("cutters_list"),
                        contentPadding = PaddingValues(top = 10.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(items = cutters, key = { it.id }) { cutter ->
                            WorkerCard(
                                name = cutter.name,
                                phoneNumber = cutter.phoneNumber,
                                isStopped = !cutter.isActive,
                                stopLabel = "إيقاف القصاص",
                                onToggleStop = { cuttersViewModel.setCutterStopped(cutter, it) },
                                onEdit = { editingCutter = cutter },
                                onDelete = {
                                    scope.launch {
                                        val hasOrders = cuttersViewModel.hasAssociatedOrders(cutter.id)
                                        pendingDeleteCutter = cutter
                                        pendingDeleteHasOrders = hasOrders
                                        showFirstConfirmDialog = true
                                    }
                                },
                                onManagePrices = {
                                    scope.launch {
                                        val prices = cuttersViewModel.getPricesListForCutter(cutter.id)
                                        val map = prices.associate { it.categoryId to it.price }
                                        pricingCutter = Pair(cutter, map)
                                    }
                                },
                                testTagPrefix = "cutter_${cutter.id}"
                            )
                        }
                    }
                }
            } else {
                // TAILORS LIST
                if (tailors.isEmpty()) {
                    WorkerEmptyState(
                        title = "لا يوجد خياطين مسجلين",
                        subtitle = "اضغط على زر \"إضافة خياط\" لبدء إضافة بيانات الخياطين",
                        icon = Icons.Default.Engineering
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp)
                            .testTag("tailors_list"),
                        contentPadding = PaddingValues(top = 10.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(items = tailors, key = { it.id }) { tailor ->
                            WorkerCard(
                                name = tailor.name,
                                phoneNumber = tailor.phoneNumber,
                                isStopped = !tailor.isActive,
                                stopLabel = "إيقاف الخياط",
                                onToggleStop = { tailorsViewModel.setTailorStopped(tailor, it) },
                                onEdit = { editingTailor = tailor },
                                onDelete = {
                                    scope.launch {
                                        val hasOrders = tailorsViewModel.hasAssociatedOrders(tailor.id)
                                        pendingDeleteTailor = tailor
                                        pendingDeleteHasOrders = hasOrders
                                        showFirstConfirmDialog = true
                                    }
                                },
                                onManagePrices = {
                                    scope.launch {
                                        val prices = tailorsViewModel.getPricesListForTailor(tailor.id)
                                        val map = prices.associate { it.categoryId to it.price }
                                        pricingTailor = Pair(tailor, map)
                                    }
                                },
                                testTagPrefix = "tailor_${tailor.id}"
                            )
                        }
                    }
                }
            }
        }

        // SNACKBAR HOST
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }

    // ADD DIALOG
    if (showAddDialog) {
        val isCutter = selectedTabIndex == 0
        WorkerFormDialog(
            isCutter = isCutter,
            initialName = "",
            initialPhone = "",
            isEditing = false,
            onDismiss = { showAddDialog = false },
            onSave = { name, phone ->
                if (isCutter) {
                    cuttersViewModel.addCutter(name, phone) {
                        showAddDialog = false
                        scope.launch { snackbarHostState.showSnackbar("تم إضافة القصاص بنجاح") }
                    }
                } else {
                    tailorsViewModel.addTailor(name, phone) {
                        showAddDialog = false
                        scope.launch { snackbarHostState.showSnackbar("تم إضافة الخياط بنجاح") }
                    }
                }
            }
        )
    }

    // EDIT CUTTER DIALOG
    if (editingCutter != null) {
        val cutter = editingCutter!!
        WorkerFormDialog(
            isCutter = true,
            initialName = cutter.name,
            initialPhone = cutter.phoneNumber,
            isEditing = true,
            onDismiss = { editingCutter = null },
            onSave = { name, phone ->
                cuttersViewModel.updateCutter(cutter.copy(name = name, phoneNumber = phone)) {
                    editingCutter = null
                    scope.launch { snackbarHostState.showSnackbar("تم تعديل بيانات القصاص بنجاح") }
                }
            }
        )
    }

    // EDIT TAILOR DIALOG
    if (editingTailor != null) {
        val tailor = editingTailor!!
        WorkerFormDialog(
            isCutter = false,
            initialName = tailor.name,
            initialPhone = tailor.phoneNumber,
            isEditing = true,
            onDismiss = { editingTailor = null },
            onSave = { name, phone ->
                tailorsViewModel.updateTailor(tailor.copy(name = name, phoneNumber = phone)) {
                    editingTailor = null
                    scope.launch { snackbarHostState.showSnackbar("تم تعديل بيانات الخياط بنجاح") }
                }
            }
        )
    }

    // DELETE FIRST CONFIRMATION DIALOG
    if (showFirstConfirmDialog) {
        val isCutter = pendingDeleteCutter != null
        val workerName = pendingDeleteCutter?.name ?: pendingDeleteTailor?.name ?: ""
        val roleName = if (isCutter) "هذا القصاص" else "هذا الخياط"

        AlertDialog(
            onDismissRequest = {
                showFirstConfirmDialog = false
                pendingDeleteCutter = null
                pendingDeleteTailor = null
            },
            title = {
                Text(
                    text = "تأكيد الحذف",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = RedButton
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من حذف $roleName ($workerName)؟",
                    fontSize = 16.sp,
                    color = Color(0xFF212121)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFirstConfirmDialog = false
                        if (pendingDeleteHasOrders) {
                            // Proceed to SECOND confirmation because orders reference this worker
                            showSecondConfirmDialog = true
                        } else {
                            // No orders: delete immediately
                            if (isCutter) {
                                pendingDeleteCutter?.let { c ->
                                    cuttersViewModel.deleteCutter(c, hasOrders = false) {
                                        scope.launch { snackbarHostState.showSnackbar("تم حذف القصاص بنجاح") }
                                    }
                                }
                            } else {
                                pendingDeleteTailor?.let { t ->
                                    tailorsViewModel.deleteTailor(t, hasOrders = false) {
                                        scope.launch { snackbarHostState.showSnackbar("تم حذف الخياط بنجاح") }
                                    }
                                }
                            }
                            pendingDeleteCutter = null
                            pendingDeleteTailor = null
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
                        showFirstConfirmDialog = false
                        pendingDeleteCutter = null
                        pendingDeleteTailor = null
                    }
                ) {
                    Text("إلغاء", color = Color(0xFF616161))
                }
            }
        )
    }

    // DELETE SECOND CONFIRMATION DIALOG (When orders DO reference this Cutter/Tailor)
    if (showSecondConfirmDialog) {
        val isCutter = pendingDeleteCutter != null
        val roleName = if (isCutter) "هذا القصاص" else "هذا الخياط"

        AlertDialog(
            onDismissRequest = {
                showSecondConfirmDialog = false
                pendingDeleteCutter = null
                pendingDeleteTailor = null
            },
            title = {
                Text(
                    text = "تأكيد حذف نهائي",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = RedButton
                )
            },
            text = {
                Text(
                    text = "هل تريد بالفعل حذف $roleName رغم وجود معاملات سابقة مرتبطة به؟",
                    fontSize = 16.sp,
                    color = Color(0xFF212121)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSecondConfirmDialog = false
                        if (isCutter) {
                            pendingDeleteCutter?.let { c ->
                                cuttersViewModel.deleteCutter(c, hasOrders = true) {
                                    scope.launch { snackbarHostState.showSnackbar("تم إيقاف وحذف القصاص من القوائم النشطة بنجاح") }
                                }
                            }
                        } else {
                            pendingDeleteTailor?.let { t ->
                                tailorsViewModel.deleteTailor(t, hasOrders = true) {
                                    scope.launch { snackbarHostState.showSnackbar("تم إيقاف وحذف الخياط من القوائم النشطة بنجاح") }
                                }
                            }
                        }
                        pendingDeleteCutter = null
                        pendingDeleteTailor = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedButton),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("حذف نهائي", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSecondConfirmDialog = false
                        pendingDeleteCutter = null
                        pendingDeleteTailor = null
                    }
                ) {
                    Text("إلغاء", color = Color(0xFF616161))
                }
            }
        )
    }

    // PRICING CUTTER DIALOG
    if (pricingCutter != null) {
        val (cutter, savedPrices) = pricingCutter!!
        WorkerPricesDialog(
            workerName = cutter.name,
            workerId = cutter.id,
            isCutter = true,
            categories = categories,
            savedPrices = savedPrices,
            onDismiss = { pricingCutter = null },
            onSave = { updatedPrices ->
                cuttersViewModel.savePricesForCutter(cutter.id, updatedPrices) {
                    pricingCutter = null
                    scope.launch { snackbarHostState.showSnackbar("تم حفظ أسعار القصاص ${cutter.name} بنجاح") }
                }
            }
        )
    }

    // PRICING TAILOR DIALOG
    if (pricingTailor != null) {
        val (tailor, savedPrices) = pricingTailor!!
        WorkerPricesDialog(
            workerName = tailor.name,
            workerId = tailor.id,
            isCutter = false,
            categories = categories,
            savedPrices = savedPrices,
            onDismiss = { pricingTailor = null },
            onSave = { updatedPrices ->
                tailorsViewModel.savePricesForTailor(tailor.id, updatedPrices) {
                    pricingTailor = null
                    scope.launch { snackbarHostState.showSnackbar("تم حفظ أسعار الخياط ${tailor.name} بنجاح") }
                }
            }
        )
    }
}

/**
 * Worker Card Component:
 * - Name
 * - Phone number
 * - Small checkbox with label "إيقاف القصاص" (or "إيقاف الخياط") above it (checked = isActive false, unchecked = isActive true)
 * - Prices button/icon
 * - Edit button/icon
 * - Delete button/icon
 */
@Composable
private fun WorkerCard(
    name: String,
    phoneNumber: String,
    isStopped: Boolean,
    stopLabel: String,
    onToggleStop: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onManagePrices: () -> Unit,
    testTagPrefix: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onManagePrices() }
            .testTag("worker_card_$testTagPrefix"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isStopped) Color(0xFFF1F5F9) else Color.White
        ),
        border = BorderStroke(
            1.dp,
            if (isStopped) Color(0xFFCBD5E1) else Color(0xFFE2E8F0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Worker details (Name, Phone, Status badge if stopped)
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isStopped) Color(0xFF757575) else BluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = if (isStopped) Color(0xFF616161) else Color(0xFF000000)
                    )
                    if (isStopped) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFFEBEE),
                            border = BorderStroke(1.dp, Color(0xFFEF9A9A))
                        ) {
                            Text(
                                text = "موقوف",
                                color = Color(0xFFC62828),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = Color(0xFF0288D1),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (phoneNumber.isNotBlank()) phoneNumber else "بدون رقم هاتف",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF000000)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Stop Checkbox + Prices + Edit + Delete Icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Checkbox with label above it: "إيقاف القصاص" / "إيقاف الخياط"
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = stopLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isStopped) Color(0xFFC62828) else Color(0xFF000000)
                    )
                    Checkbox(
                        checked = isStopped,
                        onCheckedChange = { onToggleStop(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFC62828),
                            uncheckedColor = Color(0xFF616161)
                        ),
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("stop_checkbox_$testTagPrefix")
                    )
                }

                // Prices button
                IconButton(
                    onClick = onManagePrices,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("prices_button_$testTagPrefix")
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = "الأسعار",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Edit button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("edit_button_$testTagPrefix")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "تعديل",
                        tint = Color(0xFF1565C0),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_button_$testTagPrefix")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف",
                        tint = RedButton,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkerEmptyState(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp, bottom = 40.dp, start = 24.dp, end = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFFE2E8F0),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = Color(0xFF212121),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp
                ),
                color = Color(0xFF616161),
                textAlign = TextAlign.Center
            )
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.reports.AccountStatementReportScreen
import com.example.ui.screens.reports.CustomSearchReportScreen
import com.example.ui.screens.reports.DailyReportScreen
import com.example.ui.screens.reports.DateRangeReportScreen
import com.example.ui.screens.reports.RecentOrdersReportScreen
import com.example.ui.screens.reports.WorkerOption
import com.example.ui.screens.reports.WorkerReportScreen
import com.example.ui.theme.BluePrimary
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.CategoriesViewModel
import com.example.ui.viewmodel.CuttersViewModel
import com.example.ui.viewmodel.OrdersViewModel
import com.example.ui.viewmodel.TailorsViewModel

enum class ActiveReportScreen {
    RECENT,
    ACCOUNT_STATEMENT,
    CUSTOM_SEARCH,
    DAILY,
    MONTHLY,
    YEARLY,
    CUTTER_REPORTS,
    TAILOR_REPORTS
}

@Composable
fun ReportsScreen(
    authViewModel: AuthViewModel,
    ordersViewModel: OrdersViewModel,
    categoriesViewModel: CategoriesViewModel,
    cuttersViewModel: CuttersViewModel,
    tailorsViewModel: TailorsViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val permissions by authViewModel.currentPermissions.collectAsState()

    val isAdmin = currentUser?.isAdmin == true
    val canAccessReports = isAdmin || (permissions?.canAccessReports == true)

    var activeReport by remember { mutableStateOf<ActiveReportScreen?>(null) }

    val allCutters by cuttersViewModel.allCutters.collectAsState()
    val allTailors by tailorsViewModel.allTailors.collectAsState()

    when (activeReport) {
        ActiveReportScreen.CUTTER_REPORTS -> {
            WorkerReportScreen(
                title = "تقارير القصاصين",
                workerLabel = "القصاص",
                workers = allCutters.map { WorkerOption(it.id, it.name, it.isActive) },
                isCutter = true,
                ordersViewModel = ordersViewModel,
                categoriesViewModel = categoriesViewModel,
                onFetchSavedExpense = { wId, start, end ->
                    cuttersViewModel.getExpenseDirect(wId, start, end)
                },
                onSaveExpense = { wId, start, end, amt ->
                    cuttersViewModel.saveExpense(wId, start, end, amt)
                },
                onBackClick = { activeReport = null }
            )
            return
        }
        ActiveReportScreen.TAILOR_REPORTS -> {
            WorkerReportScreen(
                title = "تقارير الخياطين",
                workerLabel = "الخياط",
                workers = allTailors.map { WorkerOption(it.id, it.name, it.isActive) },
                isCutter = false,
                ordersViewModel = ordersViewModel,
                categoriesViewModel = categoriesViewModel,
                onFetchSavedExpense = { wId, start, end ->
                    tailorsViewModel.getExpenseDirect(wId, start, end)
                },
                onSaveExpense = { wId, start, end, amt ->
                    tailorsViewModel.saveExpense(wId, start, end, amt)
                },
                onBackClick = { activeReport = null }
            )
            return
        }
        ActiveReportScreen.DAILY -> {
            DailyReportScreen(
                ordersViewModel = ordersViewModel,
                onBackClick = { activeReport = null }
            )
            return
        }
        ActiveReportScreen.MONTHLY -> {
            DateRangeReportScreen(
                title = "كشف حساب شهري",
                isYearly = false,
                ordersViewModel = ordersViewModel,
                onBackClick = { activeReport = null }
            )
            return
        }
        ActiveReportScreen.YEARLY -> {
            DateRangeReportScreen(
                title = "كشف حساب سنوي",
                isYearly = true,
                ordersViewModel = ordersViewModel,
                onBackClick = { activeReport = null }
            )
            return
        }
        ActiveReportScreen.RECENT -> {
            RecentOrdersReportScreen(
                ordersViewModel = ordersViewModel,
                onBackClick = { activeReport = null }
            )
            return
        }
        ActiveReportScreen.ACCOUNT_STATEMENT -> {
            AccountStatementReportScreen(
                ordersViewModel = ordersViewModel,
                cuttersViewModel = cuttersViewModel,
                tailorsViewModel = tailorsViewModel,
                onBackClick = { activeReport = null }
            )
            return
        }
        ActiveReportScreen.CUSTOM_SEARCH -> {
            CustomSearchReportScreen(
                ordersViewModel = ordersViewModel,
                categoriesViewModel = categoriesViewModel,
                cuttersViewModel = cuttersViewModel,
                tailorsViewModel = tailorsViewModel,
                onBackClick = { activeReport = null }
            )
            return
        }
        null -> {
            // Main Reports List
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // App Bar Header
        Surface(
            color = BluePrimary,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "التقارير والإحصائيات",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }
        }

        if (!canAccessReports) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFEE2E2),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "لا تملك صلاحية الوصول للتقارير",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "يرجى مراجعة مدير النظام لمنحك صلاحية الوصول إلى التقارير والإحصائيات.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B)),
                        textAlign = TextAlign.Center
                    )
                }
            }
            return
        }

        // Sub-reports list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isAdmin || permissions?.canAccessReportsRecent == true) {
                item {
                    ReportCardItem(
                        title = "آخر العمليات",
                        subtitle = "عرض أحدث العمليات والطلبات المدخلة بالتفصيل",
                        icon = Icons.Default.History,
                        iconTint = Color(0xFF2563EB),
                        iconBg = Color(0xFFEFF6FF),
                        testTag = "report_card_recent",
                        onClick = { activeReport = ActiveReportScreen.RECENT }
                    )
                }
            }

            if (isAdmin || permissions?.canAccessReportsStatement == true) {
                item {
                    ReportCardItem(
                        title = "كشف حساب",
                        subtitle = "كشف حساب مفصل للخياطين والقصاصين وحسابات الإنتاج",
                        icon = Icons.Default.AccountBalance,
                        iconTint = Color(0xFF0D9488),
                        iconBg = Color(0xFFF0FDFA),
                        testTag = "report_card_statement",
                        onClick = { activeReport = ActiveReportScreen.ACCOUNT_STATEMENT }
                    )
                }
            }

            if (isAdmin || permissions?.canAccessReportsCustomSearch == true) {
                item {
                    ReportCardItem(
                        title = "بحث مخصص في التقارير",
                        subtitle = "تصفية حسب التاريخ، الخياط، القصاص، أو نوع التفصيل",
                        icon = Icons.Default.Search,
                        iconTint = Color(0xFFD97706),
                        iconBg = Color(0xFFFFFBEB),
                        testTag = "report_card_custom_search",
                        onClick = { activeReport = ActiveReportScreen.CUSTOM_SEARCH }
                    )
                }
            }

            if (isAdmin || permissions?.canAccessReportsDaily == true) {
                item {
                    ReportCardItem(
                        title = "كشف حساب يومي",
                        subtitle = "إحصائيات وكميات التفصيل المنجزة لليوم المحدد",
                        icon = Icons.Default.Today,
                        iconTint = Color(0xFF16A34A),
                        iconBg = Color(0xFFF0FDF4),
                        testTag = "report_card_daily",
                        onClick = { activeReport = ActiveReportScreen.DAILY }
                    )
                }
            }

            if (isAdmin || permissions?.canAccessReportsMonthly == true) {
                item {
                    ReportCardItem(
                        title = "كشف حساب شهري",
                        subtitle = "ملخص العمل والإنتاجية خلال الفترة الشهرية",
                        icon = Icons.Default.CalendarMonth,
                        iconTint = Color(0xFF9333EA),
                        iconBg = Color(0xFFFAF5FF),
                        testTag = "report_card_monthly",
                        onClick = { activeReport = ActiveReportScreen.MONTHLY }
                    )
                }
            }

            if (isAdmin || permissions?.canAccessReportsYearly == true) {
                item {
                    ReportCardItem(
                        title = "كشف حساب سنوي",
                        subtitle = "إحصائيات شاملة ومقارنة سنوية لمعدلات الإنتاج",
                        icon = Icons.Default.DateRange,
                        iconTint = Color(0xFFDC2626),
                        iconBg = Color(0xFFFEF2F2),
                        testTag = "report_card_yearly",
                        onClick = { activeReport = ActiveReportScreen.YEARLY }
                    )
                }
            }

            // Financial Reports for Cutters & Tailors
            item {
                ReportCardItem(
                    title = "تقارير القصاصين",
                    subtitle = "كشف حساب تفصيلي لمستحقات القصاص، المصروفات، وصافي الحساب",
                    icon = Icons.Default.ContentCut,
                    iconTint = Color(0xFF4F46E5),
                    iconBg = Color(0xFFEEF2FF),
                    testTag = "report_card_cutters",
                    onClick = { activeReport = ActiveReportScreen.CUTTER_REPORTS }
                )
            }

            item {
                ReportCardItem(
                    title = "تقارير الخياطين",
                    subtitle = "كشف حساب تفصيلي لمستحقات الخياط، المصروفات، وصافي الحساب",
                    icon = Icons.Default.Engineering,
                    iconTint = Color(0xFF0891B2),
                    iconBg = Color(0xFFECFEFF),
                    testTag = "report_card_tailors",
                    onClick = { activeReport = ActiveReportScreen.TAILOR_REPORTS }
                )
            }
        }
    }
}

@Composable
private fun ReportCardItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = iconBg,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.5.sp,
                            color = Color(0xFF64748B)
                        )
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

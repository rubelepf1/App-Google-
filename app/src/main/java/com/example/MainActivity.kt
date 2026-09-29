package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ServiceItem
import com.example.data.TransactionItem
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.StatsSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

// ---------------- Helper Formatters for Bengali Local Appeal ----------------

fun String.toBengaliDigits(): String {
    val englishDigits = "0123456789"
    val bengaliDigits = "০১২৩৪৫৬৭৮৯"
    return this.map { char ->
        val idx = englishDigits.indexOf(char)
        if (idx != -1) bengaliDigits[idx] else char
    }.joinToString("")
}

fun Double.toBengaliCurrency(): String {
    val formatted = String.format("%.2f", this)
    return "৳ " + formatted.toBengaliDigits()
}

fun Int.toBengaliDigits(): String {
    return this.toString().toBengaliDigits()
}

fun formatBengaliDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.ENGLISH)
    val englishDate = sdf.format(Date(timestamp))
    
    // Simple Bengali month and indicator replacements
    return englishDate
        .replace("January", "জানুয়ারি")
        .replace("February", "ফেব্রুয়ারি")
        .replace("March", "মার্চ")
        .replace("April", "এপ্রিল")
        .replace("May", "মে")
        .replace("June", "জুন")
        .replace("July", "জুলাই")
        .replace("August", "আগস্ট")
        .replace("September", "সেপ্টেম্বর")
        .replace("October", "অক্টোবর")
        .replace("November", "নভেম্বর")
        .replace("December", "ডিসেম্বর")
        .replace("AM", "সকাল")
        .replace("PM", "বিকাল/সন্ধ্যা")
        .toBengaliDigits()
}

// Navigation Screens
enum class ScreenTabs {
    DASHBOARD,
    SERVICES,
    REPORTS
}

@Composable
fun MainAppScreen(viewModel: AppViewModel = viewModel()) {
    var currentTab by remember { mutableStateOf(ScreenTabs.DASHBOARD) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var showAddServiceDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == ScreenTabs.DASHBOARD,
                    onClick = { currentTab = ScreenTabs.DASHBOARD },
                    label = { Text("ড্যাশবোর্ড", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                NavigationBarItem(
                    selected = currentTab == ScreenTabs.SERVICES,
                    onClick = { currentTab = ScreenTabs.SERVICES },
                    label = { Text("সেবামূল্য তালিকা", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.ListAlt, contentDescription = "Services") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                NavigationBarItem(
                    selected = currentTab == ScreenTabs.REPORTS,
                    onClick = { currentTab = ScreenTabs.REPORTS },
                    label = { Text("আর্থিক রিপোর্ট", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Reports") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        },
        floatingActionButton = {
            if (currentTab == ScreenTabs.DASHBOARD) {
                FloatingActionButton(
                    onClick = { showAddTransactionDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_transaction_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "নতুন হিসাব")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("নতুন হিসাব", fontWeight = FontWeight.Bold)
                    }
                }
            } else if (currentTab == ScreenTabs.SERVICES) {
                FloatingActionButton(
                    onClick = { showAddServiceDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_service_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = "নতুন সেবা")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("নতুন সেবা", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ScreenTabs.DASHBOARD -> DashboardTab(viewModel)
                ScreenTabs.SERVICES -> ServicesTab(viewModel)
                ScreenTabs.REPORTS -> ReportsTab(viewModel)
            }

            // --- Add Transaction Dialog ---
            if (showAddTransactionDialog) {
                AddTransactionDialog(
                    viewModel = viewModel,
                    onDismiss = { showAddTransactionDialog = false }
                )
            }

            // --- Add Service Dialog ---
            if (showAddServiceDialog) {
                AddServiceDialog(
                    viewModel = viewModel,
                    onDismiss = { showAddServiceDialog = false }
                )
            }

            // --- Transaction Memo/Receipt View Bottom Sheet/Dialog ---
            val activeMemo by viewModel.activeMemoTransaction.collectAsStateWithLifecycle()
            if (activeMemo != null) {
                ReceiptDialog(
                    transaction = activeMemo!!,
                    onDismiss = { viewModel.activeMemoTransaction.value = null },
                    onDelete = {
                        viewModel.deleteTransaction(activeMemo!!)
                        viewModel.activeMemoTransaction.value = null
                        Toast.makeText(context, "হিসাবটি মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

// =================================== DASHBOARD TAB ===================================

@Composable
fun DashboardTab(viewModel: AppViewModel) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterStatus by viewModel.filterStatus.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Hero Header & Stats
        DashboardHeader(stats = stats)

        // Search & Filter Bar
        SearchAndFilterSection(
            searchQuery = searchQuery,
            onQueryChange = { viewModel.searchQuery.value = it },
            filterStatus = filterStatus,
            onFilterChange = { viewModel.filterStatus.value = it }
        )

        // Transaction List Header
        Text(
            text = "সাম্প্রতিক লেনদেনসমূহ (${transactions.size.toBengaliDigits()})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Transactions List
        if (transactions.isEmpty()) {
            EmptyStateView(
                message = if (searchQuery.isNotEmpty()) "উক্ত নামে বা নম্বরে কোনো লেনদেন পাওয়া যায়নি!" else "এখনো কোনো লেনদেন যুক্ত করা হয়নি। নিচের '+' বোতামে চাপ দিয়ে প্রথম লেনদেন যোগ করুন।"
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(transactions, key = { it.id }) { transaction ->
                    TransactionRowItem(
                        transaction = transaction,
                        onClick = { viewModel.activeMemoTransaction.value = transaction }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB
                }
            }
        }
    }
}

@Composable
fun DashboardHeader(stats: StatsSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "উদ্যোক্তা ড্যাশবোর্ড",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "ইউনিয়ন ডিজিটাল সেন্টার",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                // Elegant white circular emblem
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalAtm,
                        contentDescription = "Emblem",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main stats inside hero header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "আজকের মোট সংগ্রহ",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = stats.todayCollected.toBengaliCurrency(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(
                        text = "আজকের মোট বকেয়া",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = stats.todayDue.toBengaliCurrency(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (stats.todayDue > 0) Color(0xFFFFD1D1) else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "আজকের সম্পন্ন সেবা:",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${stats.todayCount.toBengaliDigits()} টি",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun SearchAndFilterSection(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    filterStatus: String,
    onFilterChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            placeholder = { Text("গ্রাহকের নাম বা মোবাইল নম্বর...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_input"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val filters = listOf(
                "ALL" to "সব হিসাব",
                "PAID" to "পরিশোধিত",
                "DUE" to "বকেয়া",
                "PARTIAL" to "আংশিক"
            )

            filters.forEach { (statusKey, statusLabel) ->
                val isSelected = filterStatus == statusKey
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onFilterChange(statusKey) }
                        .testTag("filter_chip_$statusKey")
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionRowItem(transaction: TransactionItem, onClick: () -> Unit) {
    val isPaid = transaction.dueAmount <= 0.0
    val isPartial = transaction.dueAmount > 0.0 && transaction.paidAmount > 0.0
    val statusColor = when {
        isPaid -> Color(0xFF10B981) // Emerald Green
        isPartial -> Color(0xFFF59E0B) // Amber Yellow
        else -> Color(0xFFEF4444) // Clean Red
    }
    
    val statusText = when {
        isPaid -> "পরিশোধিত"
        isPartial -> "আংশিক"
        else -> "বকেয়া"
    }

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_item_${transaction.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left icon representing status
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(statusColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isPaid -> Icons.Default.CheckCircle
                        isPartial -> Icons.Default.Info
                        else -> Icons.Default.Warning
                    },
                    contentDescription = "Status",
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Transaction Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.citizenName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = transaction.serviceName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = transaction.citizenPhone.toBengaliDigits(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right values
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (transaction.govtFee + transaction.entrepreneurFee).toBengaliCurrency(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (!isPaid) {
                    Text(
                        text = "বকেয়া: " + transaction.dueAmount.toBengaliCurrency(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD93025),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = "No data",
            tint = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

// =================================== SERVICES TAB ===================================

@Composable
fun ServicesTab(viewModel: AppViewModel) {
    val services by viewModel.allServices.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "সেবার মূল্য ও সরকারি ফি তালিকা",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "এখানে ইউনিয়ন পরিষদ থেকে সাধারণ নাগরিকদের দেওয়া সেবাসমূহের সরকারি ফি এবং আপনার সার্ভিস চার্জ তালিকাভুক্ত আছে। লেনদেন যোগ করার সময় এগুলো স্বয়ংক্রিয়ভাবে লোড হবে।",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (services.isEmpty()) {
            EmptyStateView(message = "সেবামূল্য তালিকা লোড হচ্ছে...")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(services, key = { it.id }) { service ->
                    ServiceRowItem(
                        service = service,
                        onDelete = {
                            viewModel.deleteService(service)
                            Toast.makeText(context, "সেবাটি তালিকা থেকে মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // FAB padding
                }
            }
        }
    }
}

@Composable
fun ServiceRowItem(service: ServiceItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                if (!service.isDefault) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Service",
                            tint = Color(0xFFD93025)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ডিফল্ট",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "সরকারি ফি",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = service.govtFee.toBengaliCurrency(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "উদ্যোক্তা চার্জ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = service.entrepreneurFee.toBengaliCurrency(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// =================================== REPORTS TAB ===================================

@Composable
fun ReportsTab(viewModel: AppViewModel) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "আর্থিক বিবরণী ও বিশ্লেষণ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "নিচে ইউনিয়ন ডিজিটাল সেন্টারের সর্বমোট আয়ের পুঙ্খানুপুঙ্খ বিবরণী দেওয়া হলো।",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
        }

        // Stats Cards Grid
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "মোট আর্থিক সারসংক্ষেপ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    ReportDetailRow(
                        label = "মোট সম্পাদিত সেবা",
                        value = "${stats.totalCount.toBengaliDigits()} টি",
                        iconColor = MaterialTheme.colorScheme.primary
                    )
                    ReportDetailRow(
                        label = "মোট সংগৃহীত টাকা",
                        value = stats.totalCollected.toBengaliCurrency(),
                        iconColor = Color(0xFF0F9D58),
                        isBold = true
                    )
                    ReportDetailRow(
                        label = "মোট বকেয়া টাকা",
                        value = stats.totalDue.toBengaliCurrency(),
                        iconColor = Color(0xFFD93025),
                        isBold = true,
                        valueColor = Color(0xFFD93025)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    Spacer(modifier = Modifier.height(8.dp))

                    ReportDetailRow(
                        label = "মোট সরকারি ফি অংশ",
                        value = stats.totalGovtFee.toBengaliCurrency(),
                        iconColor = MaterialTheme.colorScheme.primary
                    )
                    ReportDetailRow(
                        label = "মোট উদ্যোক্তা আয় (সার্ভিস চার্জ)",
                        value = stats.totalEntrepreneurFee.toBengaliCurrency(),
                        iconColor = Color(0xFFE05300),
                        isHighlight = true
                    )
                }
            }
        }

        item {
            // Service guidelines
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Tips",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "পরামর্শ ও নির্দেশনা",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "১. সরকারি ফি সংশ্লিষ্ট সরকারি চালানের মাধ্যমে কোষাগারে জমা নিশ্চিত করুন।\n" +
                                    "২. কোনো বকেয়া থাকলে গ্রাহক তালিকায় লাল চিহ্নে বকেয়া পরিমাণ নির্দেশিত হয়। গ্রাহকের কাজ শেষে দ্রুত বকেয়া সংগ্রহ করুন।\n" +
                                    "৩. ডিজিটাল রশিদ গ্রাহকের মোবাইল নম্বরে এসএমএস বা হোয়াটসঅ্যাপে শেয়ার করার মাধ্যমে সেবা প্রদানে স্বচ্ছতা বৃদ্ধি করুন।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ReportDetailRow(
    label: String,
    value: String,
    iconColor: Color,
    isBold: Boolean = false,
    isHighlight: Boolean = false,
    valueColor: Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(
                if (isHighlight) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .padding(if (isHighlight) 8.dp else 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(iconColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isBold || isHighlight) FontWeight.Bold else FontWeight.Normal
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isBold || isHighlight) FontWeight.Bold else FontWeight.Medium,
            color = valueColor ?: if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

// =================================== DIALOGS ===================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(viewModel: AppViewModel, onDismiss: () -> Unit) {
    val services by viewModel.allServices.collectAsStateWithLifecycle()

    var citizenName by remember { mutableStateOf("") }
    var citizenPhone by remember { mutableStateOf("") }
    var selectedService by remember { mutableStateOf<ServiceItem?>(null) }
    var govtFeeStr by remember { mutableStateOf("") }
    var entrepreneurFeeStr by remember { mutableStateOf("") }
    var paidAmountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }

    // Validation errors
    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var serviceError by remember { mutableStateOf(false) }

    // Calculated values
    val govtFee = govtFeeStr.toDoubleOrNull() ?: 0.0
    val entrepreneurFee = entrepreneurFeeStr.toDoubleOrNull() ?: 0.0
    val totalAmount = govtFee + entrepreneurFee
    val paidAmount = paidAmountStr.toDoubleOrNull() ?: 0.0
    val dueAmount = totalAmount - paidAmount

    // Autofill when service changes
    LaunchedEffect(selectedService) {
        selectedService?.let {
            govtFeeStr = it.govtFee.toString()
            entrepreneurFeeStr = it.entrepreneurFee.toString()
            paidAmountStr = (it.govtFee + it.entrepreneurFee).toString() // Auto default to full paid
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = 40.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "নতুন সেবা হিসাব যুক্ত করুন",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Clear, contentDescription = "Close")
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                        .padding(vertical = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Citizen Name
                    item {
                        OutlinedTextField(
                            value = citizenName,
                            onValueChange = {
                                citizenName = it
                                nameError = false
                            },
                            label = { Text("গ্রাহকের নাম *") },
                            placeholder = { Text("যেমন: মোহা: আব্দুল করিম") },
                            isError = nameError,
                            supportingText = { if (nameError) Text("নাম আবশ্যক", color = Color.Red) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("citizen_name_input"),
                            singleLine = true
                        )
                    }

                    // Citizen Phone
                    item {
                        OutlinedTextField(
                            value = citizenPhone,
                            onValueChange = {
                                citizenPhone = it
                                phoneError = false
                            },
                            label = { Text("গ্রাহকের মোবাইল নম্বর *") },
                            placeholder = { Text("যেমন: 017xxxxxxxx") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            isError = phoneError,
                            supportingText = { if (phoneError) Text("সঠিক মোবাইল নম্বর দিন", color = Color.Red) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("citizen_phone_input"),
                            singleLine = true
                        )
                    }

                    // Service Dropdown
                    item {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedService?.name ?: "সেবা নির্বাচন করুন...",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("প্রদত্ত সেবা *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                isError = serviceError,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .testTag("service_dropdown")
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                services.forEach { service ->
                                    DropdownMenuItem(
                                        text = { Text(service.name) },
                                        onClick = {
                                            selectedService = service
                                            expanded = false
                                            serviceError = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }
                    }

                    // Govt Fee & Entrepreneur Fee side-by-side
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = govtFeeStr,
                                onValueChange = { govtFeeStr = it },
                                label = { Text("সরকারি ফি (৳)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("govt_fee_input"),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = entrepreneurFeeStr,
                                onValueChange = { entrepreneurFeeStr = it },
                                label = { Text("উদ্যোক্তা চার্জ (৳)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("entr_fee_input"),
                                singleLine = true
                            )
                        }
                    }

                    // Total Fee display (Read Only)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "সর্বমোট হিসাবকৃত ফি:",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = totalAmount.toBengaliCurrency(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Paid Amount Input
                    item {
                        OutlinedTextField(
                            value = paidAmountStr,
                            onValueChange = { paidAmountStr = it },
                            label = { Text("পরিশোধিত টাকা (৳) *") },
                            placeholder = { Text("যেমন: 80") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("paid_amount_input"),
                            singleLine = true
                        )
                    }

                    // Due Amount display
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "বকেয়া টাকা:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = dueAmount.toBengaliCurrency(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (dueAmount > 0.0) Color(0xFFD93025) else Color(0xFF0F9D58)
                            )
                        }
                    }

                    // Note/Comments
                    item {
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("মন্তব্য / অতিরিক্ত বিবরণ (ঐচ্ছিক)") },
                            placeholder = { Text("যেমন: কাগজ সরবরাহ করা হয়েছে") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("note_input")
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("বাতিল করুন")
                    }

                    Button(
                        onClick = {
                            var isValid = true
                            if (citizenName.trim().isEmpty()) {
                                nameError = true
                                isValid = false
                            }
                            if (citizenPhone.trim().length < 11) {
                                phoneError = true
                                isValid = false
                            }
                            if (selectedService == null) {
                                serviceError = true
                                isValid = false
                            }

                            if (isValid) {
                                viewModel.addTransaction(
                                    citizenName = citizenName,
                                    citizenPhone = citizenPhone,
                                    serviceName = selectedService!!.name,
                                    govtFee = govtFee,
                                    entrepreneurFee = entrepreneurFee,
                                    paidAmount = paidAmount,
                                    note = note
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("submit_transaction_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddServiceDialog(viewModel: AppViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var govtFeeStr by remember { mutableStateOf("") }
    var entrepreneurFeeStr by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "নতুন সেবা ও ফি যুক্ত করুন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("সেবার নাম *") },
                    placeholder = { Text("যেমন: অনলাইন উত্তরাধিকার সনদ") },
                    isError = nameError,
                    supportingText = { if (nameError) Text("সেবার নাম আবশ্যক", color = Color.Red) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("service_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = govtFeeStr,
                    onValueChange = { govtFeeStr = it },
                    label = { Text("সরকারি ফি (৳)") },
                    placeholder = { Text("যেমন: ৫০") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("service_govt_fee_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = entrepreneurFeeStr,
                    onValueChange = { entrepreneurFeeStr = it },
                    label = { Text("উদ্যোক্তা সার্ভিস চার্জ (৳)") },
                    placeholder = { Text("যেমন: ৩০") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("service_entr_fee_input"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("বাতিল")
                    }

                    Button(
                        onClick = {
                            if (name.trim().isEmpty()) {
                                nameError = true
                            } else {
                                val govtFee = govtFeeStr.toDoubleOrNull() ?: 0.0
                                val entrepreneurFee = entrepreneurFeeStr.toDoubleOrNull() ?: 0.0
                                viewModel.addService(name, govtFee, entrepreneurFee)
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("submit_service_button")
                    ) {
                        Text("যুক্ত করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptDialog(
    transaction: TransactionItem,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val total = transaction.govtFee + transaction.entrepreneurFee
    val isPaid = transaction.dueAmount <= 0.0

    if (showDeleteConfirm) {
        Dialog(onDismissRequest = { showDeleteConfirm = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "লেনদেনটি মুছতে চান?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD93025)
                    )
                    Text(
                        text = "আপনি কি নিশ্চিত যে আপনি ${transaction.citizenName}-এর '${transaction.serviceName}' সেবার হিসাবটি মুছে ফেলতে চান? এটি আর ফিরিয়ে আনা যাবে না।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TextButton(onClick = { showDeleteConfirm = false }, modifier = Modifier.weight(1f)) {
                            Text("না, বাতিল")
                        }
                        Button(
                            onClick = {
                                showDeleteConfirm = false
                                onDelete()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD93025)),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Text("হ্যাঁ, ডিলিট করুন")
                        }
                    }
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 40.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Receipt Header
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ডিজিটাল রসিদ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Clear, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ইউপি ডিজিটাল সেন্টার",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "সেবার মূল্য সংগ্রহ ও রসিদ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Receipt Content
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    item {
                        ReceiptFieldRow(label = "রশিদ নং:", value = "UP-${transaction.id.toBengaliDigits()}")
                        ReceiptFieldRow(label = "তারিখ ও সময়:", value = formatBengaliDate(transaction.timestamp))
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "গ্রাহকের বিবরণ:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ReceiptFieldRow(label = "গ্রাহকের নাম:", value = transaction.citizenName)
                        ReceiptFieldRow(label = "মোবাইল নম্বর:", value = transaction.citizenPhone.toBengaliDigits())
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "সেবার আর্থিক বিবরণ:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ReceiptFieldRow(label = "সেবার নাম:", value = transaction.serviceName)
                        ReceiptFieldRow(label = "সরকারি ফি:", value = transaction.govtFee.toBengaliCurrency())
                        ReceiptFieldRow(label = "উদ্যোক্তা কমিশন:", value = transaction.entrepreneurFee.toBengaliCurrency())
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        ReceiptFieldRow(
                            label = "সর্বমোট বিল:",
                            value = total.toBengaliCurrency(),
                            isBold = true
                        )
                        ReceiptFieldRow(
                            label = "পরিশোধিত টাকা:",
                            value = transaction.paidAmount.toBengaliCurrency(),
                            valueColor = Color(0xFF0F9D58),
                            isBold = true
                        )
                        ReceiptFieldRow(
                            label = "বকেয়া টাকা:",
                            value = transaction.dueAmount.toBengaliCurrency(),
                            valueColor = if (transaction.dueAmount > 0) Color(0xFFD93025) else Color(0xFF0F9D58),
                            isBold = true
                        )

                        if (transaction.note.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "মন্তব্য:",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = transaction.note,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Receipt Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .background(Color(0xFFFCE8E6), CircleShape)
                            .size(48.dp)
                            .testTag("delete_receipt_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Receipt", tint = Color(0xFFD93025))
                    }

                    Button(
                        onClick = {
                            val shareBody = """
                                *** ইউপি ডিজিটাল সেন্টার রশিদ ***
                                রশিদ নং: UP-${transaction.id}
                                গ্রাহক: ${transaction.citizenName}
                                মোবাইল: ${transaction.citizenPhone}
                                তারিখ: ${formatBengaliDate(transaction.timestamp)}
                                সেবার নাম: ${transaction.serviceName}
                                সরকারি ফি: ${transaction.govtFee.toBengaliCurrency()}
                                উদ্যোক্তা ফি: ${transaction.entrepreneurFee.toBengaliCurrency()}
                                ---------------------------------
                                মোট বিল: ${total.toBengaliCurrency()}
                                পরিশোধিত: ${transaction.paidAmount.toBengaliCurrency()}
                                বকেয়া: ${transaction.dueAmount.toBengaliCurrency()}
                                
                                ধন্যবাদ! আপনার সেবাই আমাদের লক্ষ্য।
                            """.trimIndent()

                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "সেবা রশিদ - ইউপি ডিজিটাল সেন্টার")
                                putExtra(Intent.EXTRA_TEXT, shareBody)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "রশিদ শেয়ার করুন"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("share_receipt_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("রশিদ শেয়ার করুন", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptFieldRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    valueColor: Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium
        )
    }
}

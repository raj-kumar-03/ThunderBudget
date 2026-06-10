package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BudgetConfig
import com.example.data.model.Expense
import com.example.data.model.SavingsGoal
import com.example.data.model.RecurringTransaction
import com.example.data.utils.DateUtils
import com.example.ui.components.CategoryBreakdownChart
import com.example.ui.components.WeeklySpendTrendChart
import com.example.ui.viewmodel.BudgetViewModel
import com.example.ui.viewmodel.FinancesSummary
import java.util.*
import kotlin.math.abs
import kotlin.math.max

@Composable
fun Modifier.smoothClickable(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: () -> Unit
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "clickScale"
    )
    return this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.foundation.LocalIndication.current,
            onClick = onClick
        )
}

@Composable
fun SmoothIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String,
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = tween(durationMillis = 80),
        label = "iconButtonScale"
    )
    IconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier.scale(scale)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SmoothFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = tween(durationMillis = 80),
        label = "fabScale"
    )
    FloatingActionButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier.scale(scale),
        containerColor = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(16.dp),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val financesState by viewModel.financesState.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()
    val budgetConfig by viewModel.budgetConfig.collectAsState()
    val savingsGoals by viewModel.savingsGoals.collectAsState()
    val recurringTransactions by viewModel.recurringTransactions.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showConfigDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddRecurringDialog by remember { mutableStateOf(false) }

    val currencySymbol = "₹"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // A premium Neo-brutalist stylized logo container matching the screenshots
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary, // Volt Lime-Yellow
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(1.5.dp, Color.Black, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = "Volt Logo",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        // Brand and title text
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "DAILY FLOW",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.2.sp
                                )
                            }
                            Text(
                                text = "Budget Tracker",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Decorative bell helper matching Screen 1 header with a smooth tactile tap scale
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                )
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        ) {
                            SmoothIconButton(
                                onClick = { },
                                contentDescription = "Notifications",
                                icon = Icons.Default.Notifications,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        // Config settings action button with a smooth tactile tap scale
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                                .testTag("app_settings_button")
                        ) {
                            SmoothIconButton(
                                onClick = { showConfigDialog = true },
                                contentDescription = "Budget Settings",
                                icon = Icons.Default.Settings,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .border(width = 1.dp, color = MaterialTheme.colorScheme.outline)
            ) {
                val navBarColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)
                )

                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(if (activeTab == 0) Icons.Default.Home else Icons.Outlined.Home, contentDescription = "Daily Allowance") },
                    label = { Text("Allowance", fontWeight = FontWeight.Bold) },
                    colors = navBarColors,
                    modifier = Modifier.testTag("nav_tab_allowance")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Outlined.BarChart, contentDescription = "Analytics") },
                    label = { Text("Analytics", fontWeight = FontWeight.Bold) },
                    colors = navBarColors,
                    modifier = Modifier.testTag("nav_tab_analytics")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Outlined.Savings, contentDescription = "Savings Goals") },
                    label = { Text("Savings", fontWeight = FontWeight.Bold) },
                    colors = navBarColors,
                    modifier = Modifier.testTag("nav_tab_savings")
                )
                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    icon = { Icon(if (activeTab == 3) Icons.Default.Loop else Icons.Outlined.Loop, contentDescription = "Recurring") },
                    label = { Text("Recurring", fontWeight = FontWeight.Bold) },
                    colors = navBarColors,
                    modifier = Modifier.testTag("nav_tab_recurring")
                )
            }
        },
        floatingActionButton = {
            if (activeTab == 0) {
                SmoothFloatingActionButton(
                    onClick = { showAddExpenseDialog = true },
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("fab_add_expense"),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Expense")
                }
            } else if (activeTab == 2) {
                SmoothFloatingActionButton(
                    onClick = { showAddGoalDialog = true },
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("fab_add_goal"),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(imageVector = Icons.Default.AddTask, contentDescription = "New Savings Goal")
                }
            } else if (activeTab == 3) {
                SmoothFloatingActionButton(
                    onClick = { showAddRecurringDialog = true },
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("fab_add_recurring"),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "New Recurring Transaction")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (activeTab) {
                0 -> AllowanceTabContent(
                    finances = financesState,
                    expenses = allExpenses,
                    currencySymbol = currencySymbol,
                    onViewGraphs = { activeTab = 1 },
                    onDeleteExpense = { viewModel.deleteExpense(it) }
                )
                1 -> AnalyticsTabContent(
                    finances = financesState,
                    expenses = allExpenses,
                    currencySymbol = currencySymbol
                )
                2 -> SavingsTabContent(
                    goals = savingsGoals,
                    currencySymbol = currencySymbol,
                    onUpdateProgress = { goal, amt -> viewModel.updateSavingsProgress(goal, amt) },
                    onDeductCash = { amt, desc -> viewModel.addExpense(amt, "Savings", desc) },
                    onDeleteGoal = { viewModel.deleteSavingsGoal(it) }
                )
                3 -> RecurringTabContent(
                    recurringList = recurringTransactions,
                    currencySymbol = currencySymbol,
                    onDelete = { viewModel.deleteRecurringTransaction(it) }
                )
            }
        }
    }

    // --- DIALOGS GENERATION ---
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            currencySymbol = currencySymbol,
            onDismiss = { showAddExpenseDialog = false },
            onAddExpense = { amt, cat, desc, date ->
                viewModel.addExpense(amt, cat, desc, date)
                showAddExpenseDialog = false
            }
        )
    }

    if (showConfigDialog) {
        ConfigureBudgetDialog(
            config = budgetConfig,
            currencySymbol = currencySymbol,
            onDismiss = { showConfigDialog = false },
            onSave = { week, month, startBal ->
                viewModel.updateConfig(week, month, startBal)
                showConfigDialog = false
            }
        )
    }

    if (showAddGoalDialog) {
        AddSavingsGoalDialog(
            currencySymbol = currencySymbol,
            onDismiss = { showAddGoalDialog = false },
            onAddGoal = { title, target, current, targetDate ->
                viewModel.addSavingsGoal(title, target, current, targetDate)
                showAddGoalDialog = false
            }
        )
    }

    if (showAddRecurringDialog) {
        AddRecurringDialog(
            currencySymbol = currencySymbol,
            onDismiss = { showAddRecurringDialog = false },
            onAddRecurring = { amt, cat, desc, freq, date ->
                viewModel.addRecurringTransaction(amt, cat, desc, freq, date)
                showAddRecurringDialog = false
            }
        )
    }
}// ==========================================
// ALLOWANCE TAB CONTENT
// ==========================================
@Composable
fun AllowanceTabContent(
    finances: FinancesSummary,
    expenses: List<Expense>,
    currencySymbol: String,
    onViewGraphs: () -> Unit,
    onDeleteExpense: (Expense) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Rolling Daily Budget Core Card - Styled in vibrant Volt Lime-Yellow
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TODAY'S RECOMMENDED LIMIT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black.copy(alpha = 0.7f),
                        letterSpacing = 1.5.sp
                    )
                    // Custom flash/lightning icon badge matching Screen 2
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.Black.copy(0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Flash Icon",
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "$currencySymbol${String.format("%.0f", finances.recommendedRemainingToday)}",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                    Text(
                        text = "/ day limit today",
                        fontSize = 13.sp,
                        color = Color.Black.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rollover badge row
                val baseDailyAllowance = finances.weeklyBudget / 7.0
                val rolloverAmt = finances.recommendedLimitToday - baseDailyAllowance

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (rolloverAmt > 1.0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x33000000), // high-contrast black overlay
                        ) {
                            Text(
                                text = "+ $currencySymbol${String.format("%.0f", rolloverAmt)} ROLLOVER",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = "Stacked from aggregate savings",
                            color = Color.Black.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x22000000),
                        ) {
                            Text(
                                text = "STANDARD DIRECT LIMIT",
                                color = Color.Black.copy(alpha = 0.8f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = "Save today to accumulate rollovers!",
                            color = Color.Black.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Beautiful high contrast black progress line
                val totalLimit = if (finances.recommendedLimitToday > 0) finances.recommendedLimitToday else 1.0
                val remainingPercent = (finances.recommendedRemainingToday / totalLimit).toFloat().coerceIn(0f, 1f)

                LinearProgressIndicator(
                    progress = { remainingPercent },
                    color = Color(0xFF000000), // Solid black progress line over Volt yellow container
                    trackColor = Color.Black.copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$currencySymbol${String.format("%,.0f", finances.remainingWeeklyBudget)} remaining this week",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.8f)
                    )
                    val remainingDays = 8 - finances.todayIndex
                    Text(
                        text = "$remainingDays ${if (remainingDays == 1) "day" else "days"} left",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Stats Grid from HTML mock-up (with Weekly Limit integrated as well for optimal density)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Total Cash Balance
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL BALANCE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Wallet",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currencySymbol${String.format("%,.0f", finances.totalAvailableBalance)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Card 2: Monthly Limit
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MONTHLY BUDGET",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Calendar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currencySymbol${String.format("%,.0f", finances.monthlyBudget)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Spending Activity & Forecast Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Header activity bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Activity & Categories",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "View Graphs",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onViewGraphs() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                if (expenses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ReceiptLong,
                                contentDescription = "Empty Log",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "No expenses logged today. Tap '+' to begin!",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(expenses) { expense ->
                            ExpenseRowCard(
                                expense = expense,
                                currencySymbol = currencySymbol,
                                onDelete = { onDeleteExpense(expense) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Forecast card from mockup
                val weeklyDiff = finances.weekProjectedDifference
                val isSurplus = weeklyDiff >= 0
                val diffText = if (isSurplus) "save" else "overspend by"
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isSurplus) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = "Forecast",
                            tint = if (isSurplus) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "WEEKLY FORECAST",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = buildAnnotatedString {
                            append("At current rate, you will $diffText ")
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSurplus) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                )
                            ) {
                                append("$currencySymbol${String.format("%.0f", abs(weeklyDiff))}")
                            }
                            append(" by Sunday night. Stay under $currencySymbol${String.format("%.0f", finances.recommendedLimitToday)}/day to hit this goal.")
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TotalCashHeaderCard(
    totalAvailableBalance: Double,
    currencySymbol: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Total Available Cash",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$currencySymbol${String.format("%,.2f", totalAvailableBalance)}",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Wallet,
                    contentDescription = "Wallet Icon",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun QuotaItemCard(
    title: String,
    spent: Double,
    total: Double,
    currencySymbol: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progressPercent = if (total > 0f) (spent / total).toFloat().coerceIn(0f, 1f) else 0f

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = if (spent > total) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = "Status Indicator",
                    tint = if (spent > total) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                )
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "$currencySymbol${String.format("%.0f", spent)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "of $currencySymbol${String.format("%.0f", total)}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { progressPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = if (spent > total) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            }
        }
    }
}

@Composable
fun ExpenseRowCard(
    expense: Expense,
    currencySymbol: String,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("expense_card_${expense.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(expense.category),
                        contentDescription = "Category Icon",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = expense.description.ifEmpty { expense.category },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = expense.category,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = DateUtils.formatShortDate(expense.date),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$currencySymbol${String.format("%.0f", expense.amount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("delete_expense_${expense.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Expense",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// ANALYTICS TAB CONTENT
// ==========================================
@Composable
fun AnalyticsTabContent(
    finances: FinancesSummary,
    expenses: List<Expense>,
    currencySymbol: String
) {
    val scrollState = rememberScrollState()

    // Aggregate expenses for weekly trend Mon-Sun
    val weekStart = DateUtils.getStartOfWeekTimestamp()
    val weekEnd = DateUtils.getEndOfWeekTimestamp()
    val weeklyExpensesMap = remember(expenses) {
        val mapped = mutableMapOf<Int, Double>()
        // Initialize days
        for (i in 1..7) mapped[i] = 0.0

        expenses.filter { it.date in weekStart..weekEnd }.forEach { exp ->
            val idx = DateUtils.getDayOfWeekIndex(exp.date)
            mapped[idx] = (mapped[idx] ?: 0.0) + exp.amount
        }
        mapped
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Simple Weekly Trend Graph widget
        WeeklySpendTrendChart(
            weeklyExpenses = weeklyExpensesMap,
            recommendedDailyAllowance = finances.recommendedLimitToday,
            currencySymbol = currencySymbol,
            modifier = Modifier.fillMaxWidth()
        )

        // Donut breakdown for categories
        CategoryBreakdownChart(
            categoryTotals = finances.categoryTotals,
            currencySymbol = currencySymbol,
            modifier = Modifier.fillMaxWidth()
        )

        // Budget forecasts Card
        ForecastsSectionCard(
            finances = finances,
            currencySymbol = currencySymbol,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(60.dp)) // padding for bottom bar
    }
}

@Composable
fun ForecastsSectionCard(
    finances: FinancesSummary,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .smoothClickable { isExpanded = !isExpanded }
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Forecast icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Budget Forecasts & Insights",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle Forecast",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Weekly projection item
                    ForecastItem(
                        label = "Weekly Spending Forecast",
                        projectedTotal = finances.weekProjectedTotal,
                        diff = finances.weekProjectedDifference,
                        budget = finances.weeklyBudget,
                        currencySymbol = currencySymbol
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    // Monthly projection item
                    ForecastItem(
                        label = "Monthly Spending Forecast",
                        projectedTotal = finances.monthProjectedTotal,
                        diff = finances.monthProjectedDifference,
                        budget = finances.monthlyBudget,
                        currencySymbol = currencySymbol
                    )
                }
            }
        }
    }
}

@Composable
fun ForecastItem(
    label: String,
    projectedTotal: Double,
    diff: Double,
    budget: Double,
    currencySymbol: String
) {
    val isSurplus = diff >= 0
    val diffText = if (isSurplus) "Surplus" else "Deficit"
    val badgeColor = if (isSurplus) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = badgeColor.copy(alpha = 0.1f),
            ) {
                Text(
                    text = "$diffText: $currencySymbol${String.format("%.0f", abs(diff))}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = if (projectedTotal > 0.0) {
                    "Currently averaging $currencySymbol${String.format("%.0f", projectedTotal)} total"
                } else {
                    "Not enough data to project"
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
            )

            Text(
                text = "Budget: $currencySymbol${String.format("%.0f", budget)}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==========================================
// SAVINGS & GOALS TAB CONTENT
// ==========================================
@Composable
fun SavingsTabContent(
    goals: List<SavingsGoal>,
    currencySymbol: String,
    onUpdateProgress: (SavingsGoal, Double) -> Unit,
    onDeductCash: (Double, String) -> Unit,
    onDeleteGoal: (SavingsGoal) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Savings Goals",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (goals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = "Empty Savings",
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Build a savings target today!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tap the circular button at the bottom to start.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(goals) { goal ->
                    SavingsGoalItemCard(
                        goal = goal,
                        currencySymbol = currencySymbol,
                        onUpdateProgress = { onUpdateProgress(goal, it) },
                        onDeductCash = onDeductCash,
                        onDelete = { onDeleteGoal(goal) }
                    )
                }
            }
        }
    }
}

@Composable
fun SavingsGoalItemCard(
    goal: SavingsGoal,
    currencySymbol: String,
    onUpdateProgress: (Double) -> Unit,
    onDeductCash: (Double, String) -> Unit,
    onDelete: () -> Unit
) {
    var showChipInAmount by remember { mutableStateOf(false) }
    var chipInValue by remember { mutableStateOf("") }
    var deductFromCash by remember { mutableStateOf(false) }

    val daysLeft = max(0L, (goal.targetDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24))
    val progressPercent = if (goal.targetAmount > 0f) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("savings_goal_${goal.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Due in $daysLeft days (${DateUtils.formatOnlyDate(goal.targetDate)})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showChipInAmount = !showChipInAmount },
                        modifier = Modifier.testTag("savings_add_btn_${goal.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings, 
                            contentDescription = "Contribute Savings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("savings_delete_btn_${goal.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Goal",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Savings Progress Bar - styled in energetic Pulse lavender/purple
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$currencySymbol${String.format("%,.0f", goal.currentAmount)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.secondary // Lavender highlight text
                )
                Text(
                    text = "Target: $currencySymbol${String.format("%,.0f", goal.targetAmount)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { progressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.secondary, // Pulse lavender progress fill
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )

            // Contribution Box toggles
            AnimatedVisibility(
                visible = showChipInAmount,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = chipInValue,
                                onValueChange = { chipInValue = it },
                                placeholder = { Text("Amount") },
                                leadingIcon = { Text(currencySymbol) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("savings_input_amount_${goal.id}"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Button(
                                onClick = {
                                    val amt = chipInValue.toDoubleOrNull() ?: 0.0
                                    if (amt > 0) {
                                        onUpdateProgress(amt)
                                        if (deductFromCash) {
                                            onDeductCash(amt, "Allocated to: ${goal.title}")
                                        }
                                        chipInValue = ""
                                        deductFromCash = false
                                        showChipInAmount = false
                                    }
                                },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("savings_add_submit_${goal.id}"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.Black
                                )
                            ) {
                                Text("Save")
                            }
                        }

                        // Toggle checkbox to deduct from total cash balance
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = deductFromCash,
                                onCheckedChange = { deductFromCash = it },
                                modifier = Modifier.testTag("savings_deduct_checkbox_${goal.id}")
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Deduct from Cash Balance",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// ADD EXPENSE DIALOG
// ==========================================
@Composable
fun AddExpenseDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onAddExpense: (Double, String, String, Long) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val categoriesList = listOf("Food", "Travel", "Rent", "Shopping", "Entertainment", "Utilities", "Other")
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Monthly Expense", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount Spent") },
                    leadingIcon = { Text(currencySymbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_expense_amount")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Note") },
                    placeholder = { Text("e.g. Dinner, Rent invoice") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_expense_desc")
                )

                // Category selector
                Text(
                    text = "Category",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val dropScroll = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .horizontalScroll(dropScroll),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categoriesList.forEach { cat ->
                            val isSelected = category == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { category = cat },
                                label = { Text(cat) },
                                modifier = Modifier.testTag("chip_category_$cat")
                            )
                        }
                    }
                }

                // Date Picker trigger
                Text(
                    text = "Date: ${DateUtils.formatOnlyDate(selectedDate)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.4f), RoundedCornerShape(8.dp))
                        .clickable {
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = selectedDate
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val newCal = Calendar.getInstance()
                                    newCal.set(year, month, dayOfMonth)
                                    selectedDate = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .padding(12.dp)
                        .testTag("dialog_expense_date_btn")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amtVal = amount.toDoubleOrNull() ?: 0.0
                    if (amtVal > 0) {
                        onAddExpense(amtVal, category, description, selectedDate)
                    }
                },
                modifier = Modifier.testTag("dialog_expense_confirm")
            ) {
                Text("Log Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ==========================================
// CONFIGURE BUDGET DIALOG
// ==========================================
@Composable
fun ConfigureBudgetDialog(
    config: BudgetConfig,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (Double, Double, Double) -> Unit
) {
    var weeklyBudget by remember { mutableStateOf(config.weeklyBudget.toString()) }
    var monthlyBudget by remember { mutableStateOf(config.monthlyBudget.toString()) }
    var startingBalance by remember { mutableStateOf(config.startingBalance.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Budget Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = weeklyBudget,
                    onValueChange = { weeklyBudget = it },
                    label = { Text("Weekly Budget limit") },
                    leadingIcon = { Text(currencySymbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_config_week")
                )

                OutlinedTextField(
                    value = monthlyBudget,
                    onValueChange = { monthlyBudget = it },
                    label = { Text("Monthly Budget limit") },
                    leadingIcon = { Text(currencySymbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_config_month")
                )

                OutlinedTextField(
                    value = startingBalance,
                    onValueChange = { startingBalance = it },
                    label = { Text("Available Cash Balance") },
                    leadingIcon = { Text(currencySymbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_config_start")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val week = weeklyBudget.toDoubleOrNull() ?: config.weeklyBudget
                    val month = monthlyBudget.toDoubleOrNull() ?: config.monthlyBudget
                    val start = startingBalance.toDoubleOrNull() ?: config.startingBalance
                    onSave(week, month, start)
                },
                modifier = Modifier.testTag("dialog_config_confirm")
            ) {
                Text("Save Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ==========================================
// ADD SAVINGS GOAL DIALOG
// ==========================================
@Composable
fun AddSavingsGoalDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onAddGoal: (String, Double, Double, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var current by remember { mutableStateOf("") }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 30)) } // default +30 days

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Savings Goal", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title") },
                    placeholder = { Text("e.g. Dream Laptop, Emergency Fund") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_goal_title")
                )

                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Target Amount") },
                    leadingIcon = { Text(currencySymbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_goal_target")
                )

                OutlinedTextField(
                    value = current,
                    onValueChange = { current = it },
                    label = { Text("Current Initial Savings") },
                    placeholder = { Text("0") },
                    leadingIcon = { Text(currencySymbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_goal_current")
                )

                // Calendar Target date trigger
                Text(
                    text = "Target Date: ${DateUtils.formatOnlyDate(selectedDate)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.4f), RoundedCornerShape(8.dp))
                        .clickable {
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = selectedDate
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val newCal = Calendar.getInstance()
                                    newCal.set(year, month, dayOfMonth)
                                    selectedDate = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .padding(12.dp)
                        .testTag("dialog_goal_date_btn")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val targetVal = target.toDoubleOrNull() ?: 0.0
                    val currentVal = current.toDoubleOrNull() ?: 0.0
                    if (title.isNotEmpty() && targetVal > 0) {
                        onAddGoal(title, targetVal, currentVal, selectedDate)
                    }
                },
                modifier = Modifier.testTag("dialog_goal_confirm")
            ) {
                Text("Create Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// Helper to horizontal scroll chips
@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(0.4f)
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        label()
    }
}

// Category icons helper
fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Food" -> Icons.Default.Restaurant
        "Travel" -> Icons.Default.DirectionsCar
        "Rent" -> Icons.Default.Home
        "Shopping" -> Icons.Default.ShoppingBag
        "Entertainment" -> Icons.Default.LocalPlay
        "Utilities" -> Icons.Default.FlashOn
        else -> Icons.Default.Category
    }
}

// ==========================================
// RECURRING TAB CONTENT
// ==========================================
@Composable
fun RecurringTabContent(
    recurringList: List<RecurringTransaction>,
    currencySymbol: String,
    onDelete: (RecurringTransaction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        
        Text(
            text = "Recurring Transactions",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (recurringList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Loop,
                        contentDescription = "Empty Recurring",
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No active recurring bills or income.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(recurringList) { rec ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(rec.category),
                                        contentDescription = rec.category,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = rec.description,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${rec.category} • ${rec.frequency}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "$currencySymbol${String.format("%.0f", rec.amount)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                IconButton(onClick = { onDelete(rec) }) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// ADD RECURRING DIALOG
// ==========================================
@Composable
fun AddRecurringDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onAddRecurring: (Double, String, String, String, Long) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }
    var description by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Weekly") }
    var startDate by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val categoriesList = listOf("Food", "Travel", "Rent", "Shopping", "Entertainment", "Utilities", "Other")
    val freqList = listOf("Daily", "Weekly", "Monthly")
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Recurring Transaction", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    leadingIcon = { Text(currencySymbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("e.g. Netflix, Wifi subscription") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Frequency", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    freqList.forEach { freq ->
                        val isSelected = frequency == freq
                        FilterChip(
                            selected = isSelected,
                            onClick = { frequency = freq },
                            label = { Text(freq) }
                        )
                    }
                }

                Text("Category", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categoriesList.forEach { cat ->
                        val isSelected = category == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amtVal = amount.toDoubleOrNull() ?: 0.0
                    if (amtVal > 0 && description.isNotEmpty()) {
                        onAddRecurring(amtVal, category, description, frequency, startDate)
                    }
                }
            ) {
                Text("Add Transaction")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

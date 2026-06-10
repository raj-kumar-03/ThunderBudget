package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BudgetDatabase
import com.example.data.model.BudgetConfig
import com.example.data.model.Expense
import com.example.data.model.SavingsGoal
import com.example.data.model.RecurringTransaction
import com.example.data.repository.BudgetRepository
import com.example.data.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.max

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BudgetRepository

    init {
        val database = BudgetDatabase.getDatabase(application)
        repository = BudgetRepository(database.budgetDao())
        checkAndApplyRecurringTransactions()
    }

    private fun checkAndApplyRecurringTransactions() {
        viewModelScope.launch {
            val recurringList = repository.getRecurringTransactionsList()
            val now = System.currentTimeMillis()
            for (rec in recurringList) {
                var currentTrigger = if (rec.lastTriggeredDate > 0L) {
                    getNextOccurrence(rec.lastTriggeredDate, rec.frequency)
                } else {
                    rec.startDate
                }

                var tempLastTriggered = rec.lastTriggeredDate
                val newExpenses = mutableListOf<Expense>()

                while (currentTrigger <= now) {
                    val expense = Expense(
                        amount = rec.amount,
                        category = rec.category,
                        description = "${rec.description} (Recurring)",
                        date = currentTrigger
                    )
                    newExpenses.add(expense)

                    tempLastTriggered = currentTrigger
                    currentTrigger = getNextOccurrence(currentTrigger, rec.frequency)
                }

                if (newExpenses.isNotEmpty()) {
                    for (exp in newExpenses) {
                        repository.insertExpense(exp)
                    }
                    repository.insertRecurringTransaction(rec.copy(lastTriggeredDate = tempLastTriggered))
                }
            }
        }
    }

    private fun getNextOccurrence(timestamp: Long, frequency: String): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        when (frequency) {
            "Daily" -> cal.add(Calendar.DAY_OF_YEAR, 1)
            "Weekly" -> cal.add(Calendar.DAY_OF_YEAR, 7)
            "Monthly" -> cal.add(Calendar.MONTH, 1)
            "Yearly" -> cal.add(Calendar.YEAR, 1)
            else -> cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    // Expose flows from repository
    val allExpenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgetConfig: StateFlow<BudgetConfig> = repository.budgetConfig
        .combine(MutableStateFlow(Unit)) { config, _ ->
            config ?: BudgetConfig() // Default config if not inserted yet
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetConfig())

    val savingsGoals: StateFlow<List<SavingsGoal>> = repository.allSavingsGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurringTransactions: StateFlow<List<RecurringTransaction>> = repository.allRecurringTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- CALCULATED BALANCES & BUDGET STATES ---

    // 1. Total Balance = Starting Balance - Total Expenses
    val financesState: StateFlow<FinancesSummary> = combine(
        allExpenses,
        budgetConfig
    ) { expenses, config ->
        val totalExpenses = expenses.sumOf { it.amount }
        val availableBalance = config.startingBalance - totalExpenses

        // Filter week expenses
        val weekStart = DateUtils.getStartOfWeekTimestamp()
        val weekEnd = DateUtils.getEndOfWeekTimestamp()
        val weekExpenses = expenses.filter { it.date in weekStart..weekEnd }
        val weekSpentTotal = weekExpenses.sumOf { it.amount }

        // Filter month expenses
        val monthStart = DateUtils.getStartOfMonthTimestamp()
        val monthEnd = DateUtils.getEndOfMonthTimestamp()
        val monthExpenses = expenses.filter { it.date in monthStart..monthEnd }
        val monthSpentTotal = monthExpenses.sumOf { it.amount }

        // --- ROLLING DAILY ALLOWANCE CALCULATION ---
        val todayIndex = DateUtils.getDayOfWeekIndexToday() // 1 (Mon) to 7 (Sun)
        val remainingDaysInWeek = 8 - todayIndex // e.g. Monday = 7, Sunday = 1

        // S_past: sum of expenses in current week before today
        val pastWeekExpenses = weekExpenses.filter { DateUtils.getDayOfWeekIndex(it.date) < todayIndex }
        val sPast = pastWeekExpenses.sumOf { it.amount }

        // S_today: sum of expenses spent today
        val todayExpenses = weekExpenses.filter { DateUtils.getDayOfWeekIndex(it.date) == todayIndex }
        val sToday = todayExpenses.sumOf { it.amount }

        // Limit today start = max(0, (WeeklyBudget - sPast) / remainingDays)
        val rawLimitToday = (config.weeklyBudget - sPast) / remainingDaysInWeek
        val recommendedLimitToday = max(0.0, rawLimitToday)
        val recommendedRemainingToday = max(0.0, recommendedLimitToday - sToday)

        // --- BUDGET FORECASTS ---
        // Week forecast
        val weekAvgDaily = if (todayIndex > 0) weekSpentTotal / todayIndex else 0.0
        val weekProjected = weekAvgDaily * 7
        val weekProjectedDifference = config.weeklyBudget - weekProjected

        // Month forecast
        val todayDayOfMonth = DateUtils.getDayOfMonthToday()
        val daysInMonth = DateUtils.getDaysInCurrentMonth()
        val monthAvgDaily = if (todayDayOfMonth > 0) monthSpentTotal / todayDayOfMonth else 0.0
        val monthProjected = monthAvgDaily * daysInMonth
        val monthProjectedDifference = config.monthlyBudget - monthProjected

        // Category breakdown
        val categoryTotals = expenses.groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

        FinancesSummary(
            totalAvailableBalance = availableBalance,
            weeklyBudget = config.weeklyBudget,
            monthlyBudget = config.monthlyBudget,
            weeklySpent = weekSpentTotal,
            monthlySpent = monthSpentTotal,
            remainingWeeklyBudget = config.weeklyBudget - weekSpentTotal,
            remainingMonthlyBudget = config.monthlyBudget - monthSpentTotal,
            recommendedLimitToday = recommendedLimitToday,
            recommendedRemainingToday = recommendedRemainingToday,
            todayIndex = todayIndex,
            spentToday = sToday,
            spentPastOfWeek = sPast,
            weekProjectedDifference = weekProjectedDifference,
            weekProjectedTotal = weekProjected,
            monthProjectedDifference = monthProjectedDifference,
            monthProjectedTotal = monthProjected,
            categoryTotals = categoryTotals
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancesSummary())

    // --- OPERATIONS ---

    fun addExpense(amount: Double, category: String, description: String, date: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            val expense = Expense(
                amount = amount,
                category = category,
                description = description,
                date = date
            )
            repository.insertExpense(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun updateConfig(weeklyBudget: Double, monthlyBudget: Double, startingBalance: Double) {
        viewModelScope.launch {
            val updatedConfig = BudgetConfig(
                weeklyBudget = weeklyBudget,
                monthlyBudget = monthlyBudget,
                startingBalance = startingBalance
            )
            repository.updateBudgetConfig(updatedConfig)
        }
    }

    fun addSavingsGoal(title: String, targetAmount: Double, currentAmount: Double, targetDate: Long) {
        viewModelScope.launch {
            val goal = SavingsGoal(
                title = title,
                targetAmount = targetAmount,
                currentAmount = currentAmount,
                targetDate = targetDate
            )
            repository.insertSavingsGoal(goal)
        }
    }

    fun updateSavingsProgress(goal: SavingsGoal, amountToAdd: Double) {
        viewModelScope.launch {
            val updatedGoal = goal.copy(currentAmount = max(0.0, goal.currentAmount + amountToAdd))
            repository.insertSavingsGoal(updatedGoal)
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goal)
        }
    }

    fun addRecurringTransaction(amount: Double, category: String, description: String, frequency: String, startDate: Long) {
        viewModelScope.launch {
            val recurring = RecurringTransaction(
                amount = amount,
                category = category,
                description = description,
                frequency = frequency,
                startDate = startDate,
                lastTriggeredDate = 0L
            )
            repository.insertRecurringTransaction(recurring)
            checkAndApplyRecurringTransactions()
        }
    }

    fun deleteRecurringTransaction(rec: RecurringTransaction) {
        viewModelScope.launch {
            repository.deleteRecurringTransaction(rec)
        }
    }
}

data class FinancesSummary(
    val totalAvailableBalance: Double = 0.0,
    val weeklyBudget: Double = 2000.0,
    val monthlyBudget: Double = 10000.0,
    val weeklySpent: Double = 0.0,
    val monthlySpent: Double = 0.0,
    val remainingWeeklyBudget: Double = 2000.0,
    val remainingMonthlyBudget: Double = 10000.0,
    val recommendedLimitToday: Double = 0.0,
    val recommendedRemainingToday: Double = 0.0,
    val todayIndex: Int = 1,
    val spentToday: Double = 0.0,
    val spentPastOfWeek: Double = 0.0,
    val weekProjectedDifference: Double = 0.0,
    val weekProjectedTotal: Double = 0.0,
    val monthProjectedDifference: Double = 0.0,
    val monthProjectedTotal: Double = 0.0,
    val categoryTotals: Map<String, Double> = emptyMap()
)

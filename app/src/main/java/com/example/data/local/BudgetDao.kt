package com.example.data.local

import androidx.room.*
import com.example.data.model.BudgetConfig
import com.example.data.model.Expense
import com.example.data.model.SavingsGoal
import com.example.data.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    // --- EXPENSES ---
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    // --- BUDGET CONFIG ---
    @Query("SELECT * FROM budget_config WHERE id = 1 LIMIT 1")
    fun getBudgetConfig(): Flow<BudgetConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetConfig(config: BudgetConfig)

    // --- SAVINGS GOALS ---
    @Query("SELECT * FROM savings_goals ORDER BY targetDate ASC")
    fun getAllSavingsGoals(): Flow<List<SavingsGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(goal: SavingsGoal)

    @Delete
    suspend fun deleteSavingsGoal(goal: SavingsGoal)

    // --- RECURRING TRANSACTIONS ---
    @Query("SELECT * FROM recurring_transactions ORDER BY startDate DESC")
    fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>>

    @Query("SELECT * FROM recurring_transactions")
    suspend fun getRecurringTransactionsList(): List<RecurringTransaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringTransaction(recurring: RecurringTransaction)

    @Delete
    suspend fun deleteRecurringTransaction(recurring: RecurringTransaction)
}

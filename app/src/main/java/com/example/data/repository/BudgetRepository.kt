package com.example.data.repository

import com.example.data.local.BudgetDao
import com.example.data.model.BudgetConfig
import com.example.data.model.Expense
import com.example.data.model.SavingsGoal
import com.example.data.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) {

    val allExpenses: Flow<List<Expense>> = budgetDao.getAllExpenses()
    val budgetConfig: Flow<BudgetConfig?> = budgetDao.getBudgetConfig()
    val allSavingsGoals: Flow<List<SavingsGoal>> = budgetDao.getAllSavingsGoals()
    val allRecurringTransactions: Flow<List<RecurringTransaction>> = budgetDao.getAllRecurringTransactions()

    suspend fun getRecurringTransactionsList(): List<RecurringTransaction> {
        return budgetDao.getRecurringTransactionsList()
    }

    suspend fun insertRecurringTransaction(rec: RecurringTransaction) {
        budgetDao.insertRecurringTransaction(rec)
    }

    suspend fun deleteRecurringTransaction(rec: RecurringTransaction) {
        budgetDao.deleteRecurringTransaction(rec)
    }

    suspend fun insertExpense(expense: Expense) {
        budgetDao.insertExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        budgetDao.deleteExpense(expense)
    }

    suspend fun updateBudgetConfig(config: BudgetConfig) {
        budgetDao.insertBudgetConfig(config)
    }

    suspend fun insertSavingsGoal(goal: SavingsGoal) {
        budgetDao.insertSavingsGoal(goal)
    }

    suspend fun deleteSavingsGoal(goal: SavingsGoal) {
        budgetDao.deleteSavingsGoal(goal)
    }
}

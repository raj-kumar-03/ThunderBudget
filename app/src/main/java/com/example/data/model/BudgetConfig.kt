package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_config")
data class BudgetConfig(
    @PrimaryKey val id: Int = 1, // Singleton row
    val weeklyBudget: Double = 2000.0,
    val monthlyBudget: Double = 10000.0,
    val startingBalance: Double = 15000.0
)

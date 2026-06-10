package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_transactions")
data class RecurringTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val category: String,
    val description: String,
    val frequency: String, // "Daily", "Weekly", "Monthly", "Yearly"
    val startDate: Long, // timestamp
    val lastTriggeredDate: Long = 0L // timestamp of the last time an expense was generated
)

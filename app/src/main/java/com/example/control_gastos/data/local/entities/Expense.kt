package com.example.control_gastos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val amount: Double,
    val description: String,
    val date: Long, // Timestamp en milisegundos
    val category: String?,
    val isRecurring: Boolean = false,
    val periodId: Long // Clave foránea a FinancialPeriod
)

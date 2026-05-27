package com.example.control_gastos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "financial_periods")
data class FinancialPeriod(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val startDate: Long, // Timestamp en milisegundos
    val endDate: Long,   // Timestamp en milisegundos
    val type: String,    // "Mensual" o "Quincenal"
    val initialBalance: Double = 0.0,
    var totalSpent: Double = 0.0, // Se actualizará
    var expenseCount: Int = 0     // Se actualizará
)

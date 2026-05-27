package com.example.control_gastos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.control_gastos.data.local.dao.ExpenseDao
import com.example.control_gastos.data.local.dao.FinancialPeriodDao
import com.example.control_gastos.data.local.dao.RecurringExpenseDao
import com.example.control_gastos.data.local.entities.Expense
import com.example.control_gastos.data.local.entities.FinancialPeriod
import com.example.control_gastos.data.local.entities.RecurringExpense

@Database(
    entities = [Expense::class, RecurringExpense::class, FinancialPeriod::class],
    version = 1,
    exportSchema = false
)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao
    abstract fun financialPeriodDao(): FinancialPeriodDao
}

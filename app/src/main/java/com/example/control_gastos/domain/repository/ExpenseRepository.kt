package com.example.control_gastos.domain.repository

import com.example.control_gastos.data.local.dao.ExpenseDao
import com.example.control_gastos.data.local.entities.Expense
import com.example.control_gastos.data.local.entities.FinancialPeriod
import com.example.control_gastos.data.local.entities.RecurringExpense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    suspend fun insertExpense(expense: Expense)
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expenseId: Long)
    fun getExpensesForPeriod(periodId: Long): Flow<List<Expense>>
    fun getTotalSpentForPeriod(periodId: Long): Flow<Double?>
    fun getExpenseCountForPeriod(periodId: Long): Flow<Int?>
    fun getTopCategoriesForPeriod(periodId: Long): Flow<List<ExpenseDao.CategorySpending>>

    suspend fun insertRecurringExpense(recurringExpense: RecurringExpense)
    suspend fun updateRecurringExpense(recurringExpense: RecurringExpense)
    suspend fun deleteRecurringExpense(recurringExpenseId: Long)
    fun getAllRecurringExpenses(): Flow<List<RecurringExpense>>

    suspend fun insertFinancialPeriod(period: FinancialPeriod): Long
    suspend fun updateFinancialPeriod(period: FinancialPeriod)
    fun getAllFinancialPeriods(): Flow<List<FinancialPeriod>>
    fun getCurrentFinancialPeriod(currentTime: Long): Flow<FinancialPeriod?>
    suspend fun getFinancialPeriodById(periodId: Long): FinancialPeriod?
}

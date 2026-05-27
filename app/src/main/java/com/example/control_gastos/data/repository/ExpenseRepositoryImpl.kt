package com.example.control_gastos.data.repository

import com.example.control_gastos.data.local.dao.ExpenseDao
import com.example.control_gastos.data.local.dao.FinancialPeriodDao
import com.example.control_gastos.data.local.dao.RecurringExpenseDao
import com.example.control_gastos.data.local.entities.Expense
import com.example.control_gastos.data.local.entities.FinancialPeriod
import com.example.control_gastos.data.local.entities.RecurringExpense
import com.example.control_gastos.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val recurringExpenseDao: RecurringExpenseDao,
    private val financialPeriodDao: FinancialPeriodDao
) : ExpenseRepository {

    override suspend fun insertExpense(expense: Expense) = expenseDao.insertExpense(expense)
    override suspend fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)
    override suspend fun deleteExpense(expenseId: Long) = expenseDao.deleteExpense(expenseId)
    override fun getExpensesForPeriod(periodId: Long): Flow<List<Expense>> = expenseDao.getExpensesForPeriod(periodId)
    override fun getTotalSpentForPeriod(periodId: Long): Flow<Double?> = expenseDao.getTotalSpentForPeriod(periodId)
    override fun getExpenseCountForPeriod(periodId: Long): Flow<Int?> = expenseDao.getExpenseCountForPeriod(periodId)
    override fun getTopCategoriesForPeriod(periodId: Long): Flow<List<ExpenseDao.CategorySpending>> = expenseDao.getTopCategoriesForPeriod(periodId)

    override suspend fun insertRecurringExpense(recurringExpense: RecurringExpense) = recurringExpenseDao.insertRecurringExpense(recurringExpense)
    override suspend fun updateRecurringExpense(recurringExpense: RecurringExpense) = recurringExpenseDao.updateRecurringExpense(recurringExpense)
    override suspend fun deleteRecurringExpense(recurringExpenseId: Long) = recurringExpenseDao.deleteRecurringExpense(recurringExpenseId)
    override fun getAllRecurringExpenses(): Flow<List<RecurringExpense>> = recurringExpenseDao.getAllRecurringExpenses()

    override suspend fun insertFinancialPeriod(period: FinancialPeriod): Long = financialPeriodDao.insertFinancialPeriod(period)
    override suspend fun updateFinancialPeriod(period: FinancialPeriod) = financialPeriodDao.updateFinancialPeriod(period)
    override fun getAllFinancialPeriods(): Flow<List<FinancialPeriod>> = financialPeriodDao.getAllFinancialPeriods()
    override fun getCurrentFinancialPeriod(currentTime: Long): Flow<FinancialPeriod?> = financialPeriodDao.getCurrentFinancialPeriod(currentTime)
    override suspend fun getFinancialPeriodById(periodId: Long): FinancialPeriod? = financialPeriodDao.getFinancialPeriodById(periodId)
}

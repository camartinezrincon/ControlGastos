package com.example.control_gastos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.control_gastos.data.local.entities.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpense(expenseId: Long)

    @Query("SELECT * FROM expenses WHERE periodId = :periodId ORDER BY date DESC")
    fun getExpensesForPeriod(periodId: Long): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE periodId = :periodId")
    fun getTotalSpentForPeriod(periodId: Long): Flow<Double?>

    @Query("SELECT COUNT(id) FROM expenses WHERE periodId = :periodId")
    fun getExpenseCountForPeriod(periodId: Long): Flow<Int?>

    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE periodId = :periodId GROUP BY category ORDER BY total DESC LIMIT 3")
    fun getTopCategoriesForPeriod(periodId: Long): Flow<List<CategorySpending>>

    // Clase auxiliar para el resultado de getTopCategoriesForPeriod
    data class CategorySpending(
        val category: String?,
        val total: Double
    )
}

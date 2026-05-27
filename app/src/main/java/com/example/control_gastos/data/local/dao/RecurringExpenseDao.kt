package com.example.control_gastos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.control_gastos.data.local.entities.RecurringExpense
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringExpense(recurringExpense: RecurringExpense)

    @Update
    suspend fun updateRecurringExpense(recurringExpense: RecurringExpense)

    @Query("DELETE FROM recurring_expenses WHERE id = :recurringExpenseId")
    suspend fun deleteRecurringExpense(recurringExpenseId: Long)

    @Query("SELECT * FROM recurring_expenses ORDER BY description ASC")
    fun getAllRecurringExpenses(): Flow<List<RecurringExpense>>
}

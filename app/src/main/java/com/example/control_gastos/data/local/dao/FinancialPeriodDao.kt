package com.example.control_gastos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.control_gastos.data.local.entities.FinancialPeriod
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialPeriodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinancialPeriod(period: FinancialPeriod): Long

    @Update
    suspend fun updateFinancialPeriod(period: FinancialPeriod)

    @Query("SELECT * FROM financial_periods ORDER BY startDate DESC")
    fun getAllFinancialPeriods(): Flow<List<FinancialPeriod>>

    @Query("SELECT * FROM financial_periods WHERE endDate >= :currentTime ORDER BY startDate ASC LIMIT 1")
    fun getCurrentFinancialPeriod(currentTime: Long): Flow<FinancialPeriod?>

    @Query("SELECT * FROM financial_periods WHERE id = :periodId")
    suspend fun getFinancialPeriodById(periodId: Long): FinancialPeriod?

    @Query("DELETE FROM financial_periods WHERE id = :periodId")
    suspend fun deleteFinancialPeriod(periodId: Long)
}

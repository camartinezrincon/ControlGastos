package com.example.control_gastos.data.local.dao

import androidx.room.*
import com.example.control_gastos.data.local.entities.FinancialPeriod
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialPeriodDao {
    @Insert
    suspend fun insertFinancialPeriod(period: FinancialPeriod): Long

    @Update
    suspend fun updateFinancialPeriod(period: FinancialPeriod)

    @Query("SELECT * FROM financial_periods ORDER BY startDate DESC")
    fun getAllFinancialPeriods(): Flow<List<FinancialPeriod>>

    @Query("SELECT * FROM financial_periods WHERE :currentTime BETWEEN startDate AND endDate LIMIT 1")
    fun getCurrentFinancialPeriod(currentTime: Long): Flow<FinancialPeriod?>

    @Query("SELECT * FROM financial_periods WHERE id = :periodId")
    suspend fun getFinancialPeriodById(periodId: Long): FinancialPeriod?
}

package com.example.control_gastos.di

import android.content.Context
import androidx.room.Room
import com.example.control_gastos.data.local.ExpenseDatabase
import com.example.control_gastos.data.local.dao.ExpenseDao
import com.example.control_gastos.data.local.dao.FinancialPeriodDao
import com.example.control_gastos.data.local.dao.RecurringExpenseDao
import com.example.control_gastos.data.repository.ExpenseRepositoryImpl
import com.example.control_gastos.domain.repository.ExpenseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideExpenseDatabase(@ApplicationContext context: Context): ExpenseDatabase {
        return Room.databaseBuilder(
            context,
            ExpenseDatabase::class.java,
            "expense_database"
        ).build()
    }

    @Provides
    fun provideExpenseDao(database: ExpenseDatabase): ExpenseDao {
        return database.expenseDao()
    }

    @Provides
    fun provideRecurringExpenseDao(database: ExpenseDatabase): RecurringExpenseDao {
        return database.recurringExpenseDao()
    }

    @Provides
    fun provideFinancialPeriodDao(database: ExpenseDatabase): FinancialPeriodDao {
        return database.financialPeriodDao()
    }

    @Provides
    @Singleton
    fun provideExpenseRepository(
        expenseDao: ExpenseDao,
        recurringExpenseDao: RecurringExpenseDao,
        financialPeriodDao: FinancialPeriodDao
    ): ExpenseRepository {
        return ExpenseRepositoryImpl(expenseDao, recurringExpenseDao, financialPeriodDao)
    }
}

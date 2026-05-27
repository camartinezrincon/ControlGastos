package com.example.control_gastos.domain.usecase

import com.example.control_gastos.data.local.entities.FinancialPeriod
import com.example.control_gastos.data.local.entities.Expense
import com.example.control_gastos.data.local.entities.RecurringExpense
import com.example.control_gastos.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar
import javax.inject.Inject

class CheckAndCreatePeriodUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {

    suspend operator fun invoke(periodType: String, initialBalance: Double): FinancialPeriod {
        val currentTime = System.currentTimeMillis()
        var currentPeriod = repository.getCurrentFinancialPeriod(currentTime).firstOrNull()

        if (currentPeriod == null || currentTime > currentPeriod.endDate) {
            // El período actual ha terminado o no existe, crear uno nuevo
            val newPeriod = createNewPeriod(periodType, initialBalance)
            val newPeriodId = repository.insertFinancialPeriod(newPeriod)

            // Añadir gastos recurrentes al nuevo período
            val recurringExpenses = repository.getAllRecurringExpenses().firstOrNull() ?: emptyList()
            recurringExpenses.forEach { recurring ->
                val expense = Expense(
                    amount = recurring.amount,
                    description = recurring.description,
                    date = newPeriod.startDate, // Se añade al inicio del nuevo período
                    category = recurring.category,
                    isRecurring = true,
                    periodId = newPeriodId
                )
                repository.insertExpense(expense)
            }

            // Actualizar el nuevo período con los gastos recurrentes si los hay
            val updatedTotalSpent = recurringExpenses.sumOf { it.amount }
            val updatedExpenseCount = recurringExpenses.size
            val updatedNewPeriod = newPeriod.copy(
                id = newPeriodId,
                totalSpent = updatedTotalSpent,
                expenseCount = updatedExpenseCount
            )
            repository.updateFinancialPeriod(updatedNewPeriod)
            return updatedNewPeriod
        } else {
            return currentPeriod
        }
    }

    private fun createNewPeriod(periodType: String, initialBalance: Double): FinancialPeriod {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()

        val startDate: Long
        val endDate: Long

        when (periodType) {
            "Mensual" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                startDate = calendar.timeInMillis

                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                endDate = calendar.timeInMillis
            }
            "Quincenal" -> {
                val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                if (dayOfMonth <= 15) {
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    startDate = calendar.timeInMillis

                    calendar.set(Calendar.DAY_OF_MONTH, 15)
                    endDate = calendar.timeInMillis
                } else {
                    calendar.set(Calendar.DAY_OF_MONTH, 16)
                    startDate = calendar.timeInMillis

                    calendar.add(Calendar.MONTH, 1)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.add(Calendar.DAY_OF_MONTH, -1)
                    endDate = calendar.timeInMillis
                }
            }
            else -> throw IllegalArgumentException("Tipo de período no soportado")
        }

        return FinancialPeriod(
            startDate = startDate,
            endDate = endDate,
            type = periodType,
            initialBalance = initialBalance
        )
    }
}

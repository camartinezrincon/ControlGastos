package com.example.control_gastos.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.control_gastos.data.local.dao.FinancialPeriodDao
import com.example.control_gastos.data.local.entities.Expense
import com.example.control_gastos.data.local.entities.FinancialPeriod
import com.example.control_gastos.domain.repository.ExpenseRepository
import com.example.control_gastos.domain.usecase.CheckAndCreatePeriodUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val currentPeriod: FinancialPeriod? = null,
    val expenses: List<Expense> = emptyList(),
    val totalSpent: Double = 0.0,
    val expenseCount: Int = 0,
    val topCategories: List<com.example.control_gastos.data.local.dao.ExpenseDao.CategorySpending> = emptyList(),
    val balanceRemaining: Double = 0.0,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val financialPeriodDao: FinancialPeriodDao,
    private val checkAndCreatePeriodUseCase: CheckAndCreatePeriodUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCurrentPeriodData()
    }

    fun loadCurrentPeriodData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val currentPeriod = checkAndCreatePeriodUseCase("Mensual", 0.0)
                _uiState.value = _uiState.value.copy(currentPeriod = currentPeriod)

                combine(
                    expenseRepository.getExpensesForPeriod(currentPeriod.id),
                    expenseRepository.getTotalSpentForPeriod(currentPeriod.id),
                    expenseRepository.getTopCategoriesForPeriod(currentPeriod.id)
                ) { expenses, totalSpent, topCategories ->
                    val calculatedTotalSpent = totalSpent ?: 0.0
                    _uiState.value.copy(
                        expenses = expenses,
                        totalSpent = calculatedTotalSpent,
                        expenseCount = expenses.size,
                        topCategories = topCategories,
                        balanceRemaining = currentPeriod.initialBalance - calculatedTotalSpent,
                        isLoading = false
                    )
                }.collect { updatedState ->
                    _uiState.value = updatedState
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }

    fun updateInitialBalance(newBalance: Double) {
        viewModelScope.launch {
            _uiState.value.currentPeriod?.let {
                financialPeriodDao.updateFinancialPeriod(it.copy(initialBalance = newBalance))
                loadCurrentPeriodData() // Recargar para actualizar el balance calculado
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense.id)
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.updateExpense(expense)
        }
    }
}

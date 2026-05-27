package com.example.control_gastos.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.control_gastos.data.local.dao.FinancialPeriodDao
import com.example.control_gastos.data.local.entities.FinancialPeriod
import com.example.control_gastos.domain.repository.ExpenseRepository
import com.example.control_gastos.domain.usecase.CheckAndCreatePeriodUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val currentPeriod: FinancialPeriod? = null,
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

    fun loadCurrentPeriodData(type: String = "Mensual", balance: Double = 0.0) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val currentPeriod = checkAndCreatePeriodUseCase(type, balance)
                _uiState.value = _uiState.value.copy(currentPeriod = currentPeriod)

                currentPeriod.let {
                    combine(
                        expenseRepository.getTotalSpentForPeriod(it.id),
                        expenseRepository.getExpenseCountForPeriod(it.id),
                        expenseRepository.getTopCategoriesForPeriod(it.id)
                    ) { totalSpent, expenseCount, topCategories ->
                        val calculatedTotalSpent = totalSpent ?: 0.0
                        val calculatedBalance = it.initialBalance - calculatedTotalSpent
                        _uiState.value.copy(
                            totalSpent = calculatedTotalSpent,
                            expenseCount = expenseCount ?: 0,
                            topCategories = topCategories,
                            balanceRemaining = calculatedBalance,
                            isLoading = false
                        )
                    }.collect { updatedState ->
                        _uiState.value = updatedState
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }

    fun updateInitialBalance(newBalance: Double) {
        viewModelScope.launch {
            _uiState.value.currentPeriod?.let {
                val updatedPeriod = it.copy(initialBalance = newBalance)
                financialPeriodDao.updateFinancialPeriod(updatedPeriod)
                // El flujo de Room actualizará la UI automáticamente
            }
        }
    }
}

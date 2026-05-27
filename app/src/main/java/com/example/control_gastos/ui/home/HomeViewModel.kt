package com.example.control_gastos.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.control_gastos.data.local.dao.ExpenseDao
import com.example.control_gastos.data.local.dao.FinancialPeriodDao
import com.example.control_gastos.data.local.entities.FinancialPeriod
import com.example.control_gastos.domain.repository.ExpenseRepository
import com.example.control_gastos.domain.usecase.CheckAndCreatePeriodUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val currentPeriod: FinancialPeriod? = null,
    val totalSpent: Double = 0.0,
    val expenseCount: Int = 0,
    val topCategories: List<ExpenseDao.CategorySpending> = emptyList(),
    val balanceRemaining: Double = 0.0,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val financialPeriodDao: FinancialPeriodDao, // Inyectamos el DAO directamente para operaciones de período
    private val checkAndCreatePeriodUseCase: CheckAndCreatePeriodUseCase // Inyectar el UseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCurrentPeriodData()
    }

    private fun loadCurrentPeriodData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Asegurarse de que siempre haya un período actual
                // Aquí se podría obtener el tipo de período y el saldo inicial de las preferencias del usuario
                val currentPeriod = checkAndCreatePeriodUseCase("Mensual", 1000.0) // Ejemplo: Mensual, saldo inicial 1000
                _uiState.value = _uiState.value.copy(currentPeriod = currentPeriod)

                currentPeriod?.let {
                    // Combinar los flujos de datos para el período actual
                    combine(
                        expenseRepository.getTotalSpentForPeriod(it.id),
                        expenseRepository.getExpenseCountForPeriod(it.id),
                        expenseRepository.getTopCategoriesForPeriod(it.id)
                    ) { totalSpent, expenseCount, topCategories ->
                        val calculatedTotalSpent = totalSpent ?: 0.0
                        val calculatedExpenseCount = expenseCount ?: 0
                        val calculatedBalance = it.initialBalance - calculatedTotalSpent
                        _uiState.value.copy(
                            totalSpent = calculatedTotalSpent,
                            expenseCount = calculatedExpenseCount,
                            topCategories = topCategories,
                            balanceRemaining = calculatedBalance,
                            isLoading = false
                        )
                    }.collect { updatedState ->
                        _uiState.value = updatedState
                    }
                } ?: run {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "No hay período actual. Por favor, configura uno.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }

    // Función para añadir un gasto (ejemplo)
    fun addExpense(amount: Double, description: String, category: String?) {
        viewModelScope.launch {
            _uiState.value.currentPeriod?.let {
                val newExpense = com.example.control_gastos.data.local.entities.Expense(
                    amount = amount,
                    description = description,
                    date = System.currentTimeMillis(),
                    category = category,
                    periodId = it.id
                )
                expenseRepository.insertExpense(newExpense)

                // Actualizar el total gastado y el conteo en el período actual
                val updatedPeriod = it.copy(
                    totalSpent = it.totalSpent + amount,
                    expenseCount = it.expenseCount + 1
                )
                financialPeriodDao.updateFinancialPeriod(updatedPeriod)
            }
        }
    }

    // Otras funciones para interactuar con el repositorio, como eliminar o editar gastos
}

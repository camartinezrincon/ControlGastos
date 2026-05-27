package com.example.control_gastos.ui.add_expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.control_gastos.data.local.dao.FinancialPeriodDao
import com.example.control_gastos.data.local.entities.Expense
import com.example.control_gastos.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddExpenseUiState(
    val amount: String = "",
    val description: String = "",
    val category: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val expenseAdded: Boolean = false
)

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val financialPeriodDao: FinancialPeriodDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    fun onAmountChange(newAmount: String) {
        _uiState.value = _uiState.value.copy(amount = newAmount)
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.value = _uiState.value.copy(description = newDescription)
    }

    fun onCategoryChange(newCategory: String) {
        _uiState.value = _uiState.value.copy(category = newCategory)
    }

    fun addExpense() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val amount = _uiState.value.amount.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    _uiState.value = _uiState.value.copy(error = "Por favor, introduce un monto válido.", isLoading = false)
                    return@launch
                }

                val currentPeriod = expenseRepository.getCurrentFinancialPeriod(System.currentTimeMillis()).firstOrNull()

                if (currentPeriod == null) {
                    _uiState.value = _uiState.value.copy(error = "No hay período financiero activo. Por favor, configura uno.", isLoading = false)
                    return@launch
                }

                val newExpense = Expense(
                    amount = amount,
                    description = _uiState.value.description,
                    date = System.currentTimeMillis(),
                    category = _uiState.value.category.ifEmpty { null },
                    periodId = currentPeriod.id
                )
                expenseRepository.insertExpense(newExpense)

                // Actualizar el total gastado y el conteo en el período actual
                val updatedPeriod = currentPeriod.copy(
                    totalSpent = currentPeriod.totalSpent + amount,
                    expenseCount = currentPeriod.expenseCount + 1
                )
                financialPeriodDao.updateFinancialPeriod(updatedPeriod)

                _uiState.value = _uiState.value.copy(expenseAdded = true, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.localizedMessage, isLoading = false)
            }
        }
    }

    fun resetExpenseAddedStatus() {
        _uiState.value = _uiState.value.copy(expenseAdded = false)
    }
}

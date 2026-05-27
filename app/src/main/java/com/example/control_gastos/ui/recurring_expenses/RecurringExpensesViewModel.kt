package com.example.control_gastos.ui.recurring_expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.control_gastos.data.local.entities.RecurringExpense
import com.example.control_gastos.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecurringExpensesUiState(
    val recurringExpenses: List<RecurringExpense> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddEditDialog: Boolean = false,
    val expenseToEdit: RecurringExpense? = null,
    val amountInput: String = "",
    val descriptionInput: String = "",
    val categoryInput: String = ""
)

@HiltViewModel
class RecurringExpensesViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecurringExpensesUiState())
    val uiState: StateFlow<RecurringExpensesUiState> = _uiState.asStateFlow()

    init {
        loadRecurringExpenses()
    }

    private fun loadRecurringExpenses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getAllRecurringExpenses().collect {
                _uiState.value = _uiState.value.copy(
                    recurringExpenses = it,
                    isLoading = false
                )
            }
        }
    }

    fun onAmountInputChange(newAmount: String) {
        _uiState.value = _uiState.value.copy(amountInput = newAmount)
    }

    fun onDescriptionInputChange(newDescription: String) {
        _uiState.value = _uiState.value.copy(descriptionInput = newDescription)
    }

    fun onCategoryInputChange(newCategory: String) {
        _uiState.value = _uiState.value.copy(categoryInput = newCategory)
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(
            showAddEditDialog = true,
            expenseToEdit = null,
            amountInput = "",
            descriptionInput = "",
            categoryInput = ""
        )
    }

    fun showEditDialog(expense: RecurringExpense) {
        _uiState.value = _uiState.value.copy(
            showAddEditDialog = true,
            expenseToEdit = expense,
            amountInput = expense.amount.toString(),
            descriptionInput = expense.description,
            categoryInput = expense.category ?: ""
        )
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(showAddEditDialog = false)
    }

    fun saveRecurringExpense() {
        viewModelScope.launch {
            val amount = _uiState.value.amountInput.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                _uiState.value = _uiState.value.copy(error = "Monto inválido.")
                return@launch
            }

            val description = _uiState.value.descriptionInput
            if (description.isBlank()) {
                _uiState.value = _uiState.value.copy(error = "La descripción no puede estar vacía.")
                return@launch
            }

            val category = _uiState.value.categoryInput.ifEmpty { null }

            val expense = _uiState.value.expenseToEdit?.copy(
                amount = amount,
                description = description,
                category = category
            ) ?: RecurringExpense(
                amount = amount,
                description = description,
                category = category
            )

            if (expense.id == 0L) {
                repository.insertRecurringExpense(expense)
            } else {
                repository.updateRecurringExpense(expense)
            }
            _uiState.value = _uiState.value.copy(showAddEditDialog = false, error = null)
        }
    }

    fun deleteRecurringExpense(expenseId: Long) {
        viewModelScope.launch {
            repository.deleteRecurringExpense(expenseId)
        }
    }
}

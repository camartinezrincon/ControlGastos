package com.example.control_gastos.ui.recurring_expenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.control_gastos.ui.home.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringExpensesScreen(
    onBackClick: () -> Unit,
    viewModel: RecurringExpensesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gastos Recurrentes") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Filled.Add, "Añadir Gasto Recurrente")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.error != null) {
                Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
            } else if (uiState.recurringExpenses.isEmpty()) {
                Text("No hay gastos recurrentes configurados.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(uiState.recurringExpenses) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.showEditDialog(it) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = it.description, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(text = it.category ?: "Sin Categoría", style = MaterialTheme.typography.bodySmall)
                                }
                                Text(text = formatCurrency(it.amount), style = MaterialTheme.typography.bodyLarge)
                                IconButton(onClick = { viewModel.deleteRecurringExpense(it.id) }) {
                                    Icon(Icons.Filled.Delete, "Eliminar Gasto Recurrente")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (uiState.showAddEditDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                title = { Text(if (uiState.expenseToEdit == null) "Añadir Gasto Recurrente" else "Editar Gasto Recurrente") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = uiState.amountInput,
                            onValueChange = viewModel::onAmountInputChange,
                            label = { Text("Monto") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.descriptionInput,
                            onValueChange = viewModel::onDescriptionInputChange,
                            label = { Text("Descripción") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.categoryInput,
                            onValueChange = viewModel::onCategoryInputChange,
                            label = { Text("Categoría (Opcional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        uiState.error?.let { error ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = error, color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { viewModel.saveRecurringExpense() }) {
                        Text(if (uiState.expenseToEdit == null) "Guardar" else "Actualizar")
                    }
                },
                dismissButton = {
                    Button(onClick = { viewModel.dismissDialog() }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

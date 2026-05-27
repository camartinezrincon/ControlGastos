package com.example.control_gastos.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.control_gastos.data.local.entities.Expense
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddExpenseClick: () -> Unit,
    onViewRecurringExpensesClick: () -> Unit,
    onViewHistoryClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSettingsDialog by remember { mutableStateOf(false) }
    var balanceInput by remember { mutableStateOf("") }
    
    // Estado para editar gasto
    var selectedExpense by remember { mutableStateOf<Expense?>(null) }
    var editAmount by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Control de Gastos") },
                actions = {
                    IconButton(onClick = { 
                        balanceInput = uiState.currentPeriod?.initialBalance?.toString() ?: ""
                        showSettingsDialog = true 
                    }) { Icon(Icons.Default.Settings, "Ajustes") }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    IconButton(onClick = onViewRecurringExpensesClick) { Icon(Icons.Filled.List, "Recurrentes") }
                    IconButton(onClick = onViewHistoryClick) { Icon(Icons.Filled.DateRange, "Historial") }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExpenseClick) { Icon(Icons.Filled.Add, "Añadir") }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                // Cabecera de Resumen
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Disponible", style = MaterialTheme.typography.titleMedium)
                        Text(formatCurrency(uiState.balanceRemaining), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold,
                            color = if(uiState.balanceRemaining >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Inicial: ${formatCurrency(uiState.currentPeriod?.initialBalance ?: 0.0)}")
                            Text("Gastado: ${formatCurrency(uiState.totalSpent)}")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Gastos del Período", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                // Lista de Gastos
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.expenses) { expense ->
                        ListItem(
                            headlineContent = { Text(expense.description) },
                            supportingContent = { Text("${expense.category ?: "General"} • ${formatDate(expense.date)}") },
                            trailingContent = { Text(formatCurrency(expense.amount), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                            modifier = Modifier.clickable { 
                                selectedExpense = expense
                                editAmount = expense.amount.toString()
                                editDescription = expense.description
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }

        // Diálogo de Configuración de Saldo
        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text("Saldo Inicial") },
                text = {
                    OutlinedTextField(value = balanceInput, onValueChange = { balanceInput = it }, label = { Text("Monto") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.updateInitialBalance(balanceInput.toDoubleOrNull() ?: 0.0)
                        showSettingsDialog = false
                    }) { Text("Guardar") }
                }
            )
        }

        // Diálogo de Editar/Eliminar Gasto
        selectedExpense?.let { expense ->
            AlertDialog(
                onDismissRequest = { selectedExpense = null },
                title = { Text("Editar Gasto") },
                text = {
                    Column {
                        OutlinedTextField(value = editDescription, onValueChange = { editDescription = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = editAmount, onValueChange = { editAmount = it }, label = { Text("Monto") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.updateExpense(expense.copy(description = editDescription, amount = editAmount.toDoubleOrNull() ?: expense.amount))
                        selectedExpense = null
                    }) { Text("Actualizar") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.deleteExpense(expense)
                        selectedExpense = null
                    }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Text("Eliminar")
                    }
                }
            )
        }
    }
}

fun formatCurrency(amount: Double): String = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(amount)
fun formatDate(time: Long): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(time))

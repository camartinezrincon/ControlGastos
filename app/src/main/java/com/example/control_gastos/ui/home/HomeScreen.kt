package com.example.control_gastos.ui.home

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Control de Gastos") },
                actions = {
                    IconButton(onClick = { 
                        balanceInput = uiState.currentPeriod?.initialBalance?.toString() ?: ""
                        showSettingsDialog = true 
                    }) {
                        Icon(Icons.Default.Settings, "Configuración")
                    }
                }
            )
        },
        // ... (resto del Scaffold igual que antes)
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
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (uiState.isLoading) CircularProgressIndicator()
            else {
                uiState.currentPeriod?.let {
                    Text("Período: ${formatDate(it.startDate)} - ${formatDate(it.endDate)}")
                    Text("Saldo Inicial: ${formatCurrency(it.initialBalance)}", style = MaterialTheme.typography.titleLarge)
                    Text("Total Gastado: ${formatCurrency(uiState.totalSpent)}", color = MaterialTheme.colorScheme.error)
                    Text("Disponible: ${formatCurrency(uiState.balanceRemaining)}", 
                        style = MaterialTheme.typography.headlineMedium, 
                        color = if(uiState.balanceRemaining >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Resumen por Categorías", fontWeight = FontWeight.Bold)
                            uiState.topCategories.forEach { Text("${it.category ?: "General"}: ${formatCurrency(it.total)}") }
                        }
                    }
                }
            }
        }

        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text("Configurar Período") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = balanceInput,
                            onValueChange = { balanceInput = it },
                            label = { Text("Saldo Inicial") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val newBalance = balanceInput.toDoubleOrNull() ?: 0.0
                        viewModel.updateInitialBalance(newBalance)
                        showSettingsDialog = false
                    }) { Text("Guardar") }
                }
            )
        }
    }
}

fun formatCurrency(amount: Double): String = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(amount)
fun formatDate(time: Long): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(time))

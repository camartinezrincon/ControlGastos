package com.example.control_gastos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.control_gastos.ui.add_expense.AddExpenseScreen
import com.example.control_gastos.ui.home.HomeScreen
import com.example.control_gastos.ui.recurring_expenses.RecurringExpensesScreen
import com.example.control_gastos.ui.history.HistoryScreen
import com.example.control_gastos.ui.theme.ControlGastosTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ControlGastosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ExpenseAppNavigation()
                }
            }
        }
    }
}

@Composable
fun ExpenseAppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onAddExpenseClick = { navController.navigate("add_expense") },
                onViewRecurringExpensesClick = { navController.navigate("recurring_expenses") },
                onViewHistoryClick = { navController.navigate("history") }
            )
        }
        composable("add_expense") {
            AddExpenseScreen(onBackClick = { navController.popBackStack() })
        }
        composable("recurring_expenses") {
            RecurringExpensesScreen(onBackClick = { navController.popBackStack() })
        }
        composable("history") {
            HistoryScreen(onBackClick = { navController.popBackStack() })
        }
    }
}

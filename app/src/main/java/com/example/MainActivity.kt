package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.data.db.AppDatabase
import com.example.data.repository.TravelRepository
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PaymentScreen
import com.example.ui.screens.PilgrimScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TravelViewModel
import com.example.ui.viewmodel.TravelViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Room Database, Repository, and ViewModel using Simple Constructor Injection
    val database = AppDatabase.getDatabase(applicationContext)
    val repository = TravelRepository(
      scheduleDao = database.departureScheduleDao(),
      pilgrimDao = database.pilgrimDao(),
      paymentDao = database.paymentDao()
    )
    val factory = TravelViewModelFactory(repository)
    val viewModel = ViewModelProvider(this, factory)[TravelViewModel::class.java]

    setContent {
      MyApplicationTheme {
        var selectedTabIndex by remember { mutableIntStateOf(0) }

        Scaffold(
          modifier = Modifier.fillMaxSize(),
          bottomBar = {
            NavigationBar(
              containerColor = MaterialTheme.colorScheme.surface,
              tonalElevation = 8.dp,
              modifier = Modifier.testTag("main_bottom_nav_bar")
            ) {
              val tabs = listOf(
                TabItem("Dashboard", Icons.Filled.Home, Icons.Outlined.Home, "nav_dashboard"),
                TabItem("Jamaah", Icons.Filled.People, Icons.Outlined.People, "nav_pilgrims"),
                TabItem("Jadwal", Icons.Filled.Event, Icons.Outlined.Event, "nav_schedules"),
                TabItem("Pembayaran", Icons.Filled.Payments, Icons.Outlined.Payments, "nav_payments"),
                TabItem("Profil", Icons.Filled.Info, Icons.Outlined.Info, "nav_profile")
              )

              tabs.forEachIndexed { index, tab ->
                val isSelected = selectedTabIndex == index
                NavigationBarItem(
                  selected = isSelected,
                  onClick = { selectedTabIndex = index },
                  icon = {
                    Icon(
                      imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                      contentDescription = tab.title,
                      modifier = Modifier.size(24.dp)
                    )
                  },
                  label = { Text(tab.title) },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                  ),
                  modifier = Modifier.testTag(tab.testTag)
                )
              }
            }
          }
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) {
            when (selectedTabIndex) {
              0 -> DashboardScreen(
                viewModel = viewModel,
                onNavigateToPilgrims = { selectedTabIndex = 1 },
                onNavigateToSchedules = { selectedTabIndex = 2 },
                onNavigateToPayments = { selectedTabIndex = 3 }
              )
              1 -> PilgrimScreen(viewModel = viewModel)
              2 -> ScheduleScreen(viewModel = viewModel)
              3 -> PaymentScreen(viewModel = viewModel)
              4 -> ProfileScreen(viewModel = viewModel)
            }
          }
        }
      }
    }
  }
}

data class TabItem(
  val title: String,
  val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
  val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
  val testTag: String
)

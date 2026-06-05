package com.cean.miritmo.ui.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cean.miritmo.model.Habit
import com.cean.miritmo.navigation.Screen
import com.cean.miritmo.viewmodel.HabitsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HabitsViewModel
) {
    val habits by viewModel.habits.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Activos", "Inactivos", "Eliminados")

    val filteredHabits = remember(habits, selectedTabIndex) {
        when (selectedTabIndex) {
            0 -> habits.filter { !it.isDeleted && it.isActive }
            1 -> habits.filter { !it.isDeleted && !it.isActive }
            else -> habits.filter { it.isDeleted }
        }
    }

    var habitToRestore by remember { mutableStateOf<Habit?>(null) }
    var habitToPermanentDelete by remember { mutableStateOf<Habit?>(null) }

    if (habitToRestore != null) {
        AlertDialog(
            onDismissRequest = { habitToRestore = null },
            title = { Text("Restaurar Hábito") },
            text = { Text("¿Deseas restaurar el hábito '${habitToRestore?.name}'? Volverá a aparecer en tu lista principal.") },
            confirmButton = {
                TextButton(onClick = {
                    habitToRestore?.let { viewModel.restoreHabit(it.id) }
                    habitToRestore = null
                }) {
                    Text("Restaurar")
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToRestore = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (habitToPermanentDelete != null) {
        AlertDialog(
            onDismissRequest = { habitToPermanentDelete = null },
            title = { Text("Eliminar Permanentemente") },
            text = { Text("¿Estás seguro de que quieres eliminar el hábito '${habitToPermanentDelete?.name}' definitivamente? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    habitToPermanentDelete?.let { viewModel.permanentlyDeleteHabit(it.id) {} }
                    habitToPermanentDelete = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToPermanentDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Historial de Hábitos", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            if (filteredHabits.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No hay hábitos en esta categoría.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredHabits) { habit ->
                        HistoryHabitCard(
                            habit = habit,
                            isDeleted = selectedTabIndex == 2,
                            onRestore = { habitToRestore = habit },
                            onPermanentDelete = { habitToPermanentDelete = habit },
                            onClick = {
                                if (selectedTabIndex != 2) {
                                    navController.navigate(Screen.ManageHabit.createRoute(habit.id))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryHabitCard(
    habit: Habit,
    isDeleted: Boolean,
    onRestore: () -> Unit,
    onPermanentDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = habit.category.replaceFirstChar { it.uppercase() } + " • " + if (habit.oneTime) "Una vez" else habit.frequency,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isDeleted) {
                Row {
                    IconButton(onClick = onRestore) {
                        Icon(Icons.Filled.Restore, contentDescription = "Restaurar", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onPermanentDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar permanentemente", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

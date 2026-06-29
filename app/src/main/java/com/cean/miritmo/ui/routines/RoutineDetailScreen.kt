package com.cean.miritmo.ui.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cean.miritmo.components.HabitCard
import com.cean.miritmo.navigation.Screen
import com.cean.miritmo.viewmodel.HabitsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDetailScreen(navController: NavController, viewModel: HabitsViewModel, routineId: String) {
    val routines by viewModel.routines.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val timers by viewModel.timers.collectAsState()

    val routine = routines.find { it.id == routineId }
    val routineHabits = remember(habits, routine) {
        if (routine != null) {
            habits.filter { it.routineId == routine.id && !it.isDeleted && it.isActive }
        } else {
            emptyList()
        }
    }

    val selectedDateFormat = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    var showAddHabitBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(routine?.name ?: "Detalle de Rutina", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Atrás")
                    }
                },
                actions = {
                    if (routine != null) {
                        IconButton(onClick = { showAddHabitBottomSheet = true }) {
                            Icon(Icons.Filled.Add, "Agregar Hábito")
                        }
                        IconButton(onClick = { navController.navigate("manage_routine/${routine.id}") }) {
                            Icon(Icons.Filled.Edit, "Editar Rutina")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (routine == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Rutina no encontrada", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                if (routine.description.isNotBlank()) {
                    item {
                        Text(
                            text = routine.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                    }
                }
                
                item {
                    Text(
                        text = "Hábitos de esta rutina",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                if (routineHabits.isEmpty()) {
                    item {
                        Text("Esta rutina aún no tiene hábitos o han sido eliminados.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    items(routineHabits) { habit ->
                        val targets = maxOf(1, habit.getEffectiveTargetTimes().size)
                        val completions = habit.completionsByDate[selectedDateFormat] ?: 0
                        val isCompleted = completions >= targets
                        
                        Box(modifier = Modifier.padding(bottom = 16.dp)) {
                            HabitCard(
                                habit = habit,
                                isCompleted = isCompleted,
                                currentCompletions = completions,
                                totalTargets = targets,
                                onToggleCompletion = {
                                    viewModel.toggleHabitCompletion(habit.id, isCompleted, selectedDateFormat)
                                },
                                onClick = { 
                                    navController.navigate(Screen.ManageHabit.createRoute(habit.id))
                                },
                                onPlayClick = {
                                    navController.navigate(Screen.Timer.createRoute(habit.id))
                                },
                                timerState = timers[habit.id]
                            )
                        }
                    }
                }
            }
            
            if (showAddHabitBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showAddHabitBottomSheet = false },
                    sheetState = sheetState
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Agregar Hábito",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                showAddHabitBottomSheet = false
                                navController.navigate(Screen.AddHabit.createRoute(routine.id))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Crear Nuevo")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Crear Nuevo Hábito", fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("O agrega uno existente:", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val availableHabits = habits.filter { !it.isDeleted && it.routineId == null }
                        if (availableHabits.isEmpty()) {
                            Text("No tienes otros hábitos disponibles.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            LazyColumn {
                                items(availableHabits) { habit ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { 
                                                val newHabitIds = routine.habitIds + habit.id
                                                viewModel.updateRoutine(routine, newHabitIds) {
                                                    showAddHabitBottomSheet = false
                                                }
                                            }
                                            .padding(vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "Agregar", tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(habit.name, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

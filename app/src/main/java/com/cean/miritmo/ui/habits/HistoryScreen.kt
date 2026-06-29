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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
    viewModel: HabitsViewModel,
    initialType: String = "habits"
) {
    val habits by viewModel.habits.collectAsState()
    val routines by viewModel.routines.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Activos", "Inactivos", "Eliminados")
    val isHabitsType = initialType == "habits"

    val filteredHabits = remember(habits, selectedTabIndex, isHabitsType) {
        if (!isHabitsType) emptyList() else when (selectedTabIndex) {
            0 -> habits.filter { !it.isDeleted && it.isActive }
            1 -> habits.filter { !it.isDeleted && !it.isActive }
            else -> habits.filter { it.isDeleted }
        }
    }

    val filteredRoutines = remember(routines, selectedTabIndex, isHabitsType) {
        if (isHabitsType) emptyList() else when (selectedTabIndex) {
            0 -> routines.filter { !it.isDeleted && it.isActive }
            1 -> routines.filter { !it.isDeleted && !it.isActive }
            else -> routines.filter { it.isDeleted }
        }
    }

    var itemToRestore by remember { mutableStateOf<Any?>(null) }
    var itemToPermanentDelete by remember { mutableStateOf<Any?>(null) }

    if (itemToRestore != null) {
        val name = if (itemToRestore is Habit) (itemToRestore as Habit).name else (itemToRestore as com.cean.miritmo.model.Routine).name
        AlertDialog(
            onDismissRequest = { itemToRestore = null },
            title = { Text("Restaurar") },
            text = { Text("¿Deseas restaurar '${name}'? Volverá a aparecer en tu lista principal.") },
            confirmButton = {
                TextButton(onClick = {
                    if (itemToRestore is Habit) {
                        viewModel.restoreHabit((itemToRestore as Habit).id)
                    } else {
                        viewModel.restoreRoutine((itemToRestore as com.cean.miritmo.model.Routine).id)
                    }
                    itemToRestore = null
                }) {
                    Text("Restaurar")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToRestore = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (itemToPermanentDelete != null) {
        val name = if (itemToPermanentDelete is Habit) (itemToPermanentDelete as Habit).name else (itemToPermanentDelete as com.cean.miritmo.model.Routine).name
        AlertDialog(
            onDismissRequest = { itemToPermanentDelete = null },
            title = { Text("Eliminar Permanentemente") },
            text = { Text("¿Estás seguro de que quieres eliminar '${name}' definitivamente? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    if (itemToPermanentDelete is Habit) {
                        viewModel.permanentlyDeleteHabit((itemToPermanentDelete as Habit).id) {}
                    } else {
                        viewModel.permanentlyDeleteRoutine((itemToPermanentDelete as com.cean.miritmo.model.Routine).id) {}
                    }
                    itemToPermanentDelete = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToPermanentDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (isHabitsType) "Historial de Hábitos" else "Historial de Rutinas", fontWeight = FontWeight.ExtraBold) },
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

            if (isHabitsType) {
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
                                isInactive = selectedTabIndex == 1,
                                onRestore = { itemToRestore = habit },
                                onPermanentDelete = { itemToPermanentDelete = habit },
                                onActivate = { viewModel.toggleHabitActiveStatus(habit.id) },
                                onClick = {
                                    if (selectedTabIndex != 2) {
                                        navController.navigate(Screen.ManageHabit.createRoute(habit.id))
                                    }
                                }
                            )
                        }
                    }
                }
            } else {
                if (filteredRoutines.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No hay rutinas en esta categoría.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredRoutines) { routine ->
                            val validHabitsCount = routine.habitIds.count { id -> habits.any { it.id == id && !it.isDeleted } }
                            HistoryRoutineCard(
                                routine = routine,
                                isDeleted = selectedTabIndex == 2,
                                isInactive = selectedTabIndex == 1,
                                validHabitsCount = validHabitsCount,
                                onRestore = { itemToRestore = routine },
                                onPermanentDelete = { itemToPermanentDelete = routine },
                                onActivate = { viewModel.toggleRoutineActiveStatus(routine.id) },
                                onClick = {
                                    if (selectedTabIndex != 2) {
                                        navController.navigate("manage_routine/${routine.id}")
                                    }
                                }
                            )
                        }
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
    isInactive: Boolean = false,
    onRestore: () -> Unit = {},
    onPermanentDelete: () -> Unit = {},
    onActivate: () -> Unit = {},
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), clip = false),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
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
            } else if (isInactive) {
                IconButton(onClick = onActivate) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Reanudar", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun HistoryRoutineCard(
    routine: com.cean.miritmo.model.Routine,
    isDeleted: Boolean,
    isInactive: Boolean = false,
    validHabitsCount: Int,
    onRestore: () -> Unit = {},
    onPermanentDelete: () -> Unit = {},
    onActivate: () -> Unit = {},
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), clip = false),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routine.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$validHabitsCount hábitos",
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
            } else if (isInactive) {
                IconButton(onClick = onActivate) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Reanudar", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

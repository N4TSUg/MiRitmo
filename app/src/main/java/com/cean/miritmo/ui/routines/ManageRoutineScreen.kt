package com.cean.miritmo.ui.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cean.miritmo.navigation.Screen
import com.cean.miritmo.viewmodel.HabitsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageRoutineScreen(
    navController: NavController,
    viewModel: HabitsViewModel,
    routineId: String
) {
    val routines by viewModel.routines.collectAsState()
    val habits by viewModel.habits.collectAsState()
    
    val routine = routines.find { it.id == routineId }
    
    if (routine == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Rutina no encontrada")
        }
        return
    }

    var name by remember { mutableStateOf(routine.name) }
    var description by remember { mutableStateOf(routine.description) }
    var isPrivate by remember { mutableStateOf(routine.isPrivate) }
    var selectedHabitIds by remember { mutableStateOf(routine.habitIds.toSet()) }

    LaunchedEffect(routine.habitIds) {
        selectedHabitIds = routine.habitIds.toSet()
    }

    var isSaving by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    var showAddHabitBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val myHabits = habits.filter { !it.isDeleted }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar Rutina") },
            text = { Text("¿Estás seguro de que quieres mandar esta rutina a la papelera? Podrás recuperarla desde el historial.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRoutine(routine.id) {
                        showDeleteConfirm = false
                        navController.popBackStack(Screen.Habits.route, inclusive = false)
                    }
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.primary)
                }
                Text(
                    text = "MiRitmo",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.toggleRoutineActiveStatus(routine.id)
                            navController.popBackStack(Screen.Habits.route, inclusive = false)
                        },
                        modifier = Modifier.weight(0.4f).height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (routine.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Icon(
                            if (routine.isActive) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (routine.isActive) "Pausar" else "Reanudar"
                        )
                    }

                    Button(
                        onClick = {
                            if (name.isBlank() || isSaving) return@Button
                            isSaving = true
                            viewModel.updateRoutine(
                                routine = routine.copy(
                                    name = name,
                                    description = description,
                                    isPrivate = isPrivate
                                ),
                                newHabitIds = selectedHabitIds.toList()
                            ) { success ->
                                isSaving = false
                                if (success) {
                                    navController.popBackStack()
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(0.6f)
                            .height(56.dp)
                            .shadow(8.dp, RoundedCornerShape(28.dp), spotColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            Color(0xFF60A5FA) // Lighter blue
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Guardar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "EDITAR RUTINA",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "¿Qué vas a ajustar hoy?",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Nombre de la rutina") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        singleLine = true
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(24.dp), clip = false)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                        .clickable { isPrivate = !isPrivate }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPrivate) Icons.Filled.Lock else Icons.Filled.Public,
                            contentDescription = "Privacidad",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Rutina Privada", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(
                            text = if (isPrivate) "Nadie más la verá" else "Visible para todos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isPrivate,
                        onCheckedChange = { isPrivate = it }
                    )
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Hábitos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Administra los hábitos de tu rutina.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { showAddHabitBottomSheet = true },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Añadir Hábito", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }

            val currentHabits = myHabits.filter { it.id in selectedHabitIds }

            if (currentHabits.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Ningún hábito en esta rutina.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(currentHabits) { habit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = habit.name,
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { 
                            val updatedSet = selectedHabitIds.toMutableSet()
                            updatedSet.remove(habit.id)
                            selectedHabitIds = updatedSet
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Quitar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // padding for bottom bar
            }
        }

        if (showAddHabitBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddHabitBottomSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Agregar Hábito",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Selecciona un hábito que ya hayas creado para añadirlo a esta rutina.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    val availableHabits = myHabits.filter { it.id !in selectedHabitIds && (it.routineId == null || it.routineId == routine.id) }
                    if (availableHabits.isEmpty()) {
                        Text("No tienes hábitos disponibles.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(availableHabits) { habit ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .clickable { 
                                            val updatedSet = selectedHabitIds.toMutableSet()
                                            updatedSet.add(habit.id)
                                            selectedHabitIds = updatedSet
                                            showAddHabitBottomSheet = false
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Agregar", tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(habit.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

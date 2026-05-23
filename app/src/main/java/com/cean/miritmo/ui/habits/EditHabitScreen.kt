package com.cean.miritmo.ui.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.filled.DateRange
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
import com.cean.miritmo.viewmodel.HabitsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHabitScreen(
    navController: NavController,
    viewModel: HabitsViewModel,
    habitId: String
) {
    val habit = viewModel.getHabitById(habitId)

    if (habit == null) {
        // En caso de que no exista el hábito, volver atrás
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    var habitName by remember { mutableStateOf(habit.name) }
    var selectedCategory by remember { mutableStateOf(habit.category.replaceFirstChar { it.uppercase() }) }
    var selectedDays by remember { mutableStateOf(habit.frequency.split(", ").toSet()) }
    
    // One-time habit state
    var isOneTime by remember { mutableStateOf(habit.oneTime) }
    var oneTimeDateMillis by remember { mutableStateOf<Long?>(
        try {
            if (habit.oneTimeDate != null) java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(habit.oneTimeDate)?.time else System.currentTimeMillis()
        } catch (e: Exception) { System.currentTimeMillis() }
    ) }
    var showDatePicker by remember { mutableStateOf(false) }

    var targetTimes by remember { mutableStateOf(habit.getEffectiveTargetTimes()) }
    var durationMinutes by remember { mutableStateOf(habit.durationMinutes ?: 0) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    data class CategoryItem(val title: String, val subtitle: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: Color)

    val categories = listOf(
        CategoryItem("Salud", "Cuerpo y energía", Icons.Outlined.FavoriteBorder, Color(0xFF10B981)), // Verde esmeralda
        CategoryItem("Mente", "Foco y calma", Icons.Outlined.Face, Color(0xFF3B82F6)), // Azul
        CategoryItem("Productividad", "Flujo de trabajo", Icons.Outlined.Star, Color(0xFFF472B6)) // Rosa suave
    )
    val daysOfWeek = listOf("L", "M", "X", "J", "V", "S", "D")

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Hábito") },
            text = { Text("¿Estás seguro de que quieres eliminar este hábito? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteHabit(habitId) {
                            navController.popBackStack()
                        }
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
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
                    text = "Editar Hábito",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { showDeleteDialog = true }) {
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
                Button(
                    onClick = {
                        if (isSaving) return@Button
                        
                        val frequency = if (isOneTime) "Una vez" else selectedDays.joinToString(", ")
                        val finalName = if (habitName.isNotBlank()) habitName else selectedCategory
                        
                        val repeatDaysInt = if (isOneTime) emptyList() else selectedDays.mapNotNull { day ->
                            when (day) {
                                "L" -> 1
                                "M" -> 2
                                "X" -> 3
                                "J" -> 4
                                "V" -> 5
                                "S" -> 6
                                "D" -> 7
                                else -> null
                            }
                        }
                        
                        val formattedDate = if (isOneTime && oneTimeDateMillis != null) {
                            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(oneTimeDateMillis!!))
                        } else null
                        
                        val updatedHabit = habit.copy(
                            name = finalName,
                            category = selectedCategory.lowercase(),
                            frequency = frequency,
                            repeatDays = repeatDaysInt,
                            targetTime = if (targetTimes.isNotEmpty()) targetTimes[0] else null,
                            targetTimes = targetTimes,
                            durationMinutes = if (durationMinutes > 0) durationMinutes else null,
                            oneTime = isOneTime,
                            oneTimeDate = formattedDate
                        )

                        isSaving = true
                        viewModel.updateHabit(updatedHabit) { success ->
                            isSaving = false
                            if (success) {
                                navController.popBackStack()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
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
                                        Color(0xFF60A5FA)
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
                                Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Guardar Cambios", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                    OutlinedTextField(
                        value = habitName,
                        onValueChange = { habitName = it },
                        label = { Text("Nombre del hábito") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                            capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences
                        ),
                        singleLine = true
                    )
                }
            }

            // Categories
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    categories.forEach { item ->
                        val isSelected = selectedCategory == item.title
                        val activeColor = item.color
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = if (isSelected) 4.dp else 0.dp,
                                    shape = RoundedCornerShape(24.dp),
                                    clip = false
                                )
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) activeColor else Color.Transparent,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .clip(RoundedCornerShape(24.dp))
                                .clickable { selectedCategory = item.title }
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(16.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(activeColor)
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) activeColor.copy(alpha = 0.1f)
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = item.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Repetición Semanal
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Frecuencia",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Una vez",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Switch(
                                checked = isOneTime,
                                onCheckedChange = { isOneTime = it },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (isOneTime) {
                        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                        val dateText = if (oneTimeDateMillis != null) dateFormat.format(java.util.Date(oneTimeDateMillis!!)) else "Seleccionar fecha"
                        
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.DateRange, contentDescription = "Fecha", tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(dateText, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Repetición semanal",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (selectedDays.size == 7) "Todos los días" else "${selectedDays.size} días",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            daysOfWeek.forEach { day ->
                                val isSelected = selectedDays.contains(day)
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                        .clickable {
                                            selectedDays = if (isSelected) {
                                                selectedDays - day
                                            } else {
                                                selectedDays + day
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                            }
                        }
                    }
                }



            // Recordatorio
            item {
                com.cean.miritmo.components.TimeSelectorUI(
                    targetTimes = targetTimes,
                    onTargetTimesChanged = { targetTimes = it }
                )
            }

            // Duracion
            item {
                Column {
                    Text(
                        text = "Duración (opcional)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(onClick = { durationMinutes += 5 }) {
                                    Icon(Icons.Filled.KeyboardArrowUp, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = if (durationMinutes > 0) "$durationMinutes min" else "Sin tiempo",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (durationMinutes > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                IconButton(onClick = { if (durationMinutes >= 5) durationMinutes -= 5 }) {
                                    Icon(Icons.Filled.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = oneTimeDateMillis ?: System.currentTimeMillis())
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        oneTimeDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }) { Text("Aceptar") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

package com.cean.miritmo.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cean.miritmo.components.HabitCard
import com.cean.miritmo.components.ProfileAvatar
import com.cean.miritmo.viewmodel.HabitsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HabitsViewModel,
    photoUrl: String?,
    onAddHabit: () -> Unit,
    onAddRoutine: () -> Unit,
    onNavigateToHabit: (String) -> Unit,
    onNavigateToTimer: (String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val habits by viewModel.habits.collectAsState()
    val routines by viewModel.routines.collectAsState()
    val timers by viewModel.timers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showCreateBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var currentWeekOffset by remember { mutableStateOf(0) }
    
    val selectedDateFormat = remember(selectedDateMillis) {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selectedDateMillis))
    }

    val validHabitsForToday = remember(habits, selectedDateMillis) {
        habits.filter { habit ->
            if (habit.isDeleted || !habit.isActive) return@filter false
            if (habit.oneTime) {
                val habitDate = habit.oneTimeDate ?: java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(habit.createdAt))
                habitDate == selectedDateFormat
            } else {
                val createdDateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(habit.createdAt))
                if (selectedDateFormat >= createdDateFormat) {
                    val calendar = java.util.Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                    val calendarDay = calendar.get(java.util.Calendar.DAY_OF_WEEK)
                    val appDay = if (calendarDay == java.util.Calendar.SUNDAY) 7 else calendarDay - 1
                    habit.repeatDays.isEmpty() || habit.repeatDays.contains(appDay)
                } else {
                    false
                }
            }
        }
    }

    // Calculo del progreso diario
    var totalTodayTargets = 0
    var completedTodayTargets = 0
    validHabitsForToday.forEach { habit ->
        val targets = maxOf(1, habit.getEffectiveTargetTimes().size)
        totalTodayTargets += targets
        val completions = habit.completionsByDate[selectedDateFormat] ?: 0
        completedTodayTargets += minOf(completions, targets)
    }

    val progressPercent = if (totalTodayTargets > 0) completedTodayTargets.toFloat() / totalTodayTargets.toFloat() else 0f
    val missingHabits = totalTodayTargets - completedTodayTargets

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateBottomSheet = true },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(Icons.Filled.Add, "Crear", modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp) // Espacio para el BottomBar y FAB
        ) {
            item {
                // Top Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Box(modifier = Modifier.align(Alignment.CenterStart)) {
                        ProfileAvatar(
                            photoUrl = photoUrl,
                            size = 48.dp,
                            onClick = onNavigateToProfile
                        )
                    }

                    Text(
                        text = "MiRitmo",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            item {
                // Calendar Row Mock
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                    ) {
                        val weekDays = remember(currentWeekOffset) {
                            val tempCal = java.util.Calendar.getInstance()
                            tempCal.firstDayOfWeek = java.util.Calendar.MONDAY
                            
                            // Adjust week
                            tempCal.add(java.util.Calendar.WEEK_OF_YEAR, currentWeekOffset)
                            
                            // Set to Monday of that week
                            tempCal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
                            tempCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                            tempCal.set(java.util.Calendar.MINUTE, 0)
                            tempCal.set(java.util.Calendar.SECOND, 0)
                            tempCal.set(java.util.Calendar.MILLISECOND, 0)
                            
                            val daysList = mutableListOf<java.util.Calendar>()
                            for (i in 0..6) {
                                daysList.add(tempCal.clone() as java.util.Calendar)
                                tempCal.add(java.util.Calendar.DAY_OF_MONTH, 1)
                            }
                            daysList
                        }

                        // Use Thursday to determine the month of the week
                        val monthNameFormat = remember { java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale("es", "ES")) }
                        val currentMonthName = monthNameFormat.format(weekDays[3].time).replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }

                        // Month Header with Navigation
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { currentWeekOffset-- }) {
                                Icon(Icons.Filled.ChevronLeft, contentDescription = "Semana anterior", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            
                            Text(
                                text = currentMonthName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            IconButton(onClick = { currentWeekOffset++ }) {
                                Icon(Icons.Filled.ChevronRight, contentDescription = "Siguiente semana", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Days Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val dayFormat = remember { java.text.SimpleDateFormat("EEE", java.util.Locale("es", "ES")) }
                            val numFormat = remember { java.text.SimpleDateFormat("d", java.util.Locale.getDefault()) }
                            val fullFormat = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()) }
    
                            weekDays.forEach { cal ->
                                val isSelected = fullFormat.format(cal.time) == fullFormat.format(Date(selectedDateMillis))
                                val dayName = dayFormat.format(cal.time).uppercase(java.util.Locale("es", "ES")).replace(".", "").take(3)
                                val dayNum = numFormat.format(cal.time)
    
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { selectedDateMillis = cal.timeInMillis }) {
                                    Text(
                                        text = dayName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayNum,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                // Progress Circle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { progressPercent },
                        modifier = Modifier.size(200.dp),
                        strokeWidth = 16.dp,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(progressPercent * 100).toInt()}%",
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "COMPLETADO",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                // Greeting
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¡Buen ritmo!",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isLoading) "Cargando tus hábitos..." else if (missingHabits > 0) "Te faltan $missingHabits hábitos para completar tu día." else "¡Has completado todos tus hábitos!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                // Agrupamos los hábitos en "Sueltos" y por Rutinas
                val standaloneHabits = validHabitsForToday.filter { it.routineId == null }
                val habitsInRoutines = validHabitsForToday.filter { it.routineId != null }
                val groupedByRoutine = habitsInRoutines.groupBy { it.routineId!! }

                // Mostrar hábitos de cada rutina
                groupedByRoutine.forEach { (routineId, routineHabits) ->
                    val routineName = routines.find { it.id == routineId }?.name ?: "Rutina Desconocida"
                    item {
                        Text(
                            text = routineName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 8.dp)
                        )
                    }
                    items(routineHabits) { habit ->
                        val targets = maxOf(1, habit.getEffectiveTargetTimes().size)
                        val completions = habit.completionsByDate[selectedDateFormat] ?: 0
                        val isCompleted = completions >= targets
                        
                        Box(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp)) {
                            HabitCard(
                                habit = habit,
                                isCompleted = isCompleted,
                                currentCompletions = completions,
                                totalTargets = targets,
                                onToggleCompletion = {
                                    viewModel.toggleHabitCompletion(habit.id, isCompleted, selectedDateFormat)
                                },
                                onClick = { onNavigateToHabit(habit.id) },
                                onPlayClick = {
                                    onNavigateToTimer(habit.id)
                                },
                                timerState = timers[habit.id]
                            )
                        }
                    }
                }

                // Mostrar hábitos sueltos si hay
                if (standaloneHabits.isNotEmpty()) {
                    if (groupedByRoutine.isNotEmpty()) {
                        item {
                            Text(
                                text = "Hábitos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 24.dp).padding(top = 8.dp, bottom = 8.dp)
                            )
                        }
                    }
                    items(standaloneHabits) { habit ->
                        val targets = maxOf(1, habit.getEffectiveTargetTimes().size)
                        val completions = habit.completionsByDate[selectedDateFormat] ?: 0
                        val isCompleted = completions >= targets
                        
                        Box(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp)) {
                            HabitCard(
                                habit = habit,
                                isCompleted = isCompleted,
                                currentCompletions = completions,
                                totalTargets = targets,
                                onToggleCompletion = {
                                    viewModel.toggleHabitCompletion(habit.id, isCompleted, selectedDateFormat)
                                },
                                onClick = { onNavigateToHabit(habit.id) },
                                onPlayClick = {
                                    onNavigateToTimer(habit.id)
                                },
                                timerState = timers[habit.id]
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCreateBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¿Qué deseas crear?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            showCreateBottomSheet = false
                            onAddHabit()
                        }.padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Hábito", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Hábito", fontWeight = FontWeight.Bold)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            showCreateBottomSheet = false
                            onAddRoutine()
                        }.padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Rutina", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Rutina", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

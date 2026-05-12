package com.cean.miritmo.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
    onLogout: () -> Unit,
    onAddHabit: () -> Unit,
    onNavigateToHabit: (String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val habits by viewModel.habits.collectAsState()
    val records by viewModel.records.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val todayDate = Date()
    val todayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(todayDate)

    // Calculo del progreso diario
    val completedToday = habits.count { habit -> 
        records.any { it.habitId == habit.id && it.date == todayFormat && it.isCompleted } 
    }
    val totalToday = habits.size
    val progressPercent = if (totalToday > 0) completedToday.toFloat() / totalToday.toFloat() else 0f
    val missingHabits = totalToday - completedToday

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddHabit,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(Icons.Filled.Add, "Crear Hábito", modifier = Modifier.size(32.dp))
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfileAvatar(
                        photoUrl = photoUrl,
                        size = 48.dp,
                        onClick = onNavigateToProfile
                    )

                    Text(
                        text = "MiRitmo",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val currentDayNum = remember { java.text.SimpleDateFormat("d", java.util.Locale.getDefault()).format(java.util.Date()) }
                        val weekDays = remember {
                            val tempCal = java.util.Calendar.getInstance()
                            // Set to the current week's Monday
                            tempCal.firstDayOfWeek = java.util.Calendar.MONDAY
                            tempCal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
                            
                            val daysList = mutableListOf<Pair<String, String>>()
                            val dayFormat = java.text.SimpleDateFormat("EEE", java.util.Locale("es", "ES"))
                            val numFormat = java.text.SimpleDateFormat("d", java.util.Locale.getDefault())
                            
                            for (i in 0..5) { // Mostramos 6 días (LUN a SAB) para igualar el ancho del mockup
                                val dayName = dayFormat.format(tempCal.time).uppercase(java.util.Locale("es", "ES")).replace(".", "").take(3)
                                val dayNum = numFormat.format(tempCal.time)
                                daysList.add(dayName to dayNum)
                                tempCal.add(java.util.Calendar.DAY_OF_MONTH, 1)
                            }
                            daysList
                        }

                        weekDays.forEach { pair ->
                            val isSelected = pair.second == currentDayNum
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = pair.first,
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
                                        text = pair.second,
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
                // Habit List
                items(habits) { habit ->
                    val isCompleted = records.any { it.habitId == habit.id && it.date == todayFormat && it.isCompleted }
                    Box(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp)) {
                        HabitCard(
                            habit = habit,
                            isCompleted = isCompleted,
                            onToggleCompletion = {
                                viewModel.toggleHabitCompletion(habit.id, isCompleted)
                            },
                            onClick = { onNavigateToHabit(habit.id) }
                        )
                    }
                }
            }
        }
    }
}

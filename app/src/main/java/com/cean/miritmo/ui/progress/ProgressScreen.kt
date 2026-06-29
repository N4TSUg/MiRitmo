package com.cean.miritmo.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cean.miritmo.model.Habit
import com.cean.miritmo.model.HabitRecord
import com.cean.miritmo.viewmodel.HabitsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
import com.cean.miritmo.viewmodel.TimerState

@Composable
fun ProgressScreen(
    viewModel: HabitsViewModel,
    onNavigateToTimer: (String) -> Unit
) {
    val habits by viewModel.habits.collectAsState()
    val routines by viewModel.routines.collectAsState()
    val timers by viewModel.timers.collectAsState()
    
    val todayDate = Date()
    val todayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(todayDate)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp, start = 24.dp, end = 24.dp, top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Progreso y Rachas",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Mira cuánto has avanzado en tus hábitos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            val activeHabits = habits.filter { habit ->
                if (habit.isDeleted || !habit.isActive) return@filter false
                if (habit.oneTime) {
                    val targets = maxOf(1, habit.getEffectiveTargetTimes().size)
                    val completions = habit.completionsByDate[habit.oneTimeDate ?: ""] ?: 0
                    completions < targets
                } else {
                    true
                }
            }

            val standaloneHabits = activeHabits.filter { it.routineId == null }
            val habitsInRoutines = activeHabits.filter { it.routineId != null }
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
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(routineHabits) { habit ->
                    val targets = maxOf(1, habit.getEffectiveTargetTimes().size)
                    val completions = habit.completionsByDate[todayFormat] ?: 0
                    val isCompleted = completions >= targets
                    val streak = habit.currentStreak
                    
                    ProgressHabitCard(
                        habit = habit,
                        isCompleted = isCompleted,
                        currentCompletions = completions,
                        totalTargets = targets,
                        streak = streak,
                        onToggleCompletion = { viewModel.toggleHabitCompletion(habit.id, isCompleted, todayFormat) },
                        onPlayClick = { onNavigateToTimer(habit.id) },
                        timerState = timers[habit.id]
                    )
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
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        )
                    }
                }
                items(standaloneHabits) { habit ->
                    val targets = maxOf(1, habit.getEffectiveTargetTimes().size)
                    val completions = habit.completionsByDate[todayFormat] ?: 0
                    val isCompleted = completions >= targets
                    val streak = habit.currentStreak
                    
                    ProgressHabitCard(
                        habit = habit,
                        isCompleted = isCompleted,
                        currentCompletions = completions,
                        totalTargets = targets,
                        streak = streak,
                        onToggleCompletion = { viewModel.toggleHabitCompletion(habit.id, isCompleted, todayFormat) },
                        onPlayClick = { onNavigateToTimer(habit.id) },
                        timerState = timers[habit.id]
                    )
                }
            }
            
            if (activeHabits.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No tienes hábitos creados.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}



@Composable
fun ProgressHabitCard(
    habit: Habit,
    isCompleted: Boolean,
    currentCompletions: Int = 0,
    totalTargets: Int = 1,
    streak: Int,
    onToggleCompletion: () -> Unit,
    onPlayClick: () -> Unit,
    timerState: TimerState? = null
) {
    val accentColor = MaterialTheme.colorScheme.secondary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(24.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), clip = false),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(20.dp),
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
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (totalTargets > 1) {
                        Text(
                            text = "$currentCompletions de $totalTargets",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Streak badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment, 
                            contentDescription = "Racha",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$streak días",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFF59E0B)
                        )
                    }
                    
                    // Duration badge (if applicable)
                    if (habit.durationMinutes != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Timer, 
                                contentDescription = "Duración",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val displayMinutes = timerState?.let { it.secondsRemaining / 60 } ?: habit.durationMinutes
                            Text(
                                text = "$displayMinutes min",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Checkbox or Play button
            val checkBgColor = if (isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent
            val checkBorderColor = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            
            if (habit.durationMinutes != null && !isCompleted) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onPlayClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Iniciar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(checkBgColor)
                        .border(2.dp, checkBorderColor, CircleShape)
                        .clickable { onToggleCompletion() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Completado",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

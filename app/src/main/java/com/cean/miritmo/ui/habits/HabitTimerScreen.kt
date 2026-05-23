package com.cean.miritmo.ui.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cean.miritmo.viewmodel.HabitsViewModel
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.StrokeCap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTimerScreen(
    navController: NavController,
    viewModel: HabitsViewModel,
    habitId: String
) {
    val habit = viewModel.getHabitById(habitId)

    if (habit == null || habit.durationMinutes == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val timers by viewModel.timers.collectAsState()
    val timerState = viewModel.getOrCreateTimer(habitId, habit.durationMinutes)
    // Obtener el estado actualizado desde el Flow si existe, o usar el que acabamos de crear
    val currentTimer = timers[habitId] ?: timerState

    val secondsRemaining = currentTimer.secondsRemaining
    val totalSeconds = currentTimer.totalSeconds
    val isRunning = currentTimer.isRunning

    val progress = if (totalSeconds > 0) secondsRemaining.toFloat() / totalSeconds.toFloat() else 0f
    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isRunning) "En progreso..." else if (secondsRemaining == 0) "¡Tiempo terminado!" else "Listo para empezar",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            // Timer Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp)
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    strokeWidth = 16.dp,
                    strokeCap = StrokeCap.Round
                )
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 16.dp,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 64.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause / Play
                FloatingActionButton(
                    onClick = { viewModel.toggleTimer(habitId) },
                    containerColor = if (isRunning) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                    contentColor = if (isRunning) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    if (isRunning) {
                        // Pause Icon using painter or shape (simulated with a simple composable if needed)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.width(6.dp).height(24.dp).background(MaterialTheme.colorScheme.onSurfaceVariant))
                            Box(modifier = Modifier.width(6.dp).height(24.dp).background(MaterialTheme.colorScheme.onSurfaceVariant))
                        }
                    } else {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Iniciar", modifier = Modifier.size(36.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Complete Button
            Button(
                onClick = {
                    val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    viewModel.toggleHabitCompletion(habitId, false, dateStr) // Marcar como completado
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(8.dp, RoundedCornerShape(28.dp), spotColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Marcar como Completado", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

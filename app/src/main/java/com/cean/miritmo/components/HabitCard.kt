package com.cean.miritmo.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cean.miritmo.model.Habit

import com.cean.miritmo.viewmodel.TimerState

@Composable
fun HabitCard(
    habit: Habit,
    isCompleted: Boolean,
    currentCompletions: Int = 0,
    totalTargets: Int = 1,
    onToggleCompletion: () -> Unit,
    onClick: () -> Unit,
    onPlayClick: (() -> Unit)? = null,
    timerState: TimerState? = null
) {
    // Definir colores basados en la "categoría" (simulado para el diseño)
    val accentColor = MaterialTheme.colorScheme.secondary
    val accentBgColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Borde lateral de color
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .padding(vertical = 16.dp)
                    .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                    .background(accentColor)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 20.dp, top = 20.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Icono con fondo circular
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(accentBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications, // Placeholder icon
                        contentDescription = "Icon",
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))

                // Textos
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (totalTargets > 1) "${habit.category.uppercase()} • $currentCompletions de $totalTargets completados" else "${habit.category.uppercase()} • ${habit.getEffectiveTargetTimes().firstOrNull() ?: habit.frequency}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (habit.durationMinutes != null) {
                        val displayMinutes = timerState?.let { it.secondsRemaining / 60 } ?: habit.durationMinutes
                        Text(
                            text = "$displayMinutes min",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Checkbox Circular Moderno
                val checkBgColor by animateColorAsState(
                    targetValue = if (isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent,
                    label = "checkBgColor"
                )
                val checkBorderColor by animateColorAsState(
                    targetValue = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    label = "checkBorderColor"
                )

                if (habit.durationMinutes != null && !isCompleted) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { onPlayClick?.invoke() },
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
}

package com.cean.miritmo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import java.util.Locale

@Composable
fun TimeSelectorUI(
    targetTimes: List<String>,
    onTargetTimesChanged: (List<String>) -> Unit
) {
    var mode by remember { mutableStateOf(0) } // 0: Específicas, 1: Intervalo
    
    // Variables for specific times
    var hour by remember { mutableStateOf(8) }
    var minute by remember { mutableStateOf(0) }
    var isAm by remember { mutableStateOf(true) }

    // Variables for interval
    var intervalValue by remember { mutableStateOf("2") }
    var intervalUnit by remember { mutableStateOf("horas") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var timesCount by remember { mutableStateOf(3) }

    Column {
        Text(
            text = "Recordatorio",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Mode Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (mode == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { mode = 0 }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Horas Específicas", color = if (mode == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (mode == 1) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { mode = 1 }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Intervalo", color = if (mode == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (mode == 0) {
            // Horas específicas
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Custom Time Picker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hour
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { hour = if (hour == 12) 1 else hour + 1 }) {
                                Icon(Icons.Filled.KeyboardArrowUp, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = String.format("%02d", hour),
                                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = { hour = if (hour == 1) 12 else hour - 1 }) {
                                Icon(Icons.Filled.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Text(
                            text = ":",
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Minute
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { minute = if (minute >= 55) 0 else minute + 5 }) {
                                Icon(Icons.Filled.KeyboardArrowUp, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = String.format("%02d", minute),
                                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = { minute = if (minute <= 0) 55 else minute - 5 }) {
                                Icon(Icons.Filled.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.width(24.dp))

                        // AM / PM
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isAm) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { isAm = true }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("AM", color = if (isAm) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (!isAm) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { isAm = false }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("PM", color = if (!isAm) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            val timeStr = String.format(Locale.US, "%02d:%02d %s", hour, minute, if (isAm) "AM" else "PM")
                            if (!targetTimes.contains(timeStr)) {
                                onTargetTimesChanged((targetTimes + timeStr).sortedBy { 
                                    // Basic sort
                                    try {
                                        java.text.SimpleDateFormat("hh:mm a", Locale.US).parse(it)?.time ?: 0L
                                    } catch(e: Exception) { 0L }
                                })
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Filled.Add, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Añadir hora")
                    }
                }
            }
        } else {
            // Modo Intervalo
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Hora de inicio", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Simple start time picker (reused UI)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hour
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { hour = if (hour == 12) 1 else hour + 1 }) {
                                Icon(Icons.Filled.KeyboardArrowUp, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(String.format("%02d", hour), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            IconButton(onClick = { hour = if (hour == 1) 12 else hour - 1 }) {
                                Icon(Icons.Filled.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text(":", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 16.dp))
                        // Minute
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { minute = if (minute >= 55) 0 else minute + 5 }) {
                                Icon(Icons.Filled.KeyboardArrowUp, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(String.format("%02d", minute), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            IconButton(onClick = { minute = if (minute <= 0) 55 else minute - 5 }) {
                                Icon(Icons.Filled.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.width(24.dp))
                        Column(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                            Box(modifier = Modifier.background(if (isAm) MaterialTheme.colorScheme.primary else Color.Transparent).clickable { isAm = true }.padding(12.dp)) {
                                Text("AM", color = if (isAm) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            }
                            Box(modifier = Modifier.background(if (!isAm) MaterialTheme.colorScheme.primary else Color.Transparent).clickable { isAm = false }.padding(12.dp)) {
                                Text("PM", color = if (!isAm) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Box {
                            Row(
                                modifier = Modifier
                                    .clickable { isDropdownExpanded = true }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Repetir cada ($intervalUnit)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            DropdownMenu(
                                expanded = isDropdownExpanded,
                                onDismissRequest = { isDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("horas") },
                                    onClick = { intervalUnit = "horas"; isDropdownExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("minutos") },
                                    onClick = { intervalUnit = "minutos"; isDropdownExpanded = false }
                                )
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { 
                                val current = intervalValue.toIntOrNull() ?: 1
                                if (current > 1) intervalValue = (current - 1).toString() 
                            }) { Icon(Icons.Filled.KeyboardArrowDown, null) }
                            
                            BasicTextField(
                                value = intervalValue,
                                onValueChange = { newValue -> 
                                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                        intervalValue = newValue
                                    }
                                },
                                textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface),
                                modifier = Modifier.width(40.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            
                            IconButton(onClick = { 
                                val current = intervalValue.toIntOrNull() ?: 0
                                intervalValue = (current + 1).toString() 
                            }) { Icon(Icons.Filled.KeyboardArrowUp, null) }
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Cantidad de veces", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (timesCount > 1) timesCount-- }) { Icon(Icons.Filled.KeyboardArrowDown, null) }
                            Text("$timesCount", style = MaterialTheme.typography.titleLarge)
                            IconButton(onClick = { if (timesCount < 24) timesCount++ }) { Icon(Icons.Filled.KeyboardArrowUp, null) }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            val newTimes = mutableListOf<String>()
                            val cal = java.util.Calendar.getInstance()
                            val timeFormat = java.text.SimpleDateFormat("hh:mm a", Locale.US)
                            val startStr = String.format(Locale.US, "%02d:%02d %s", hour, minute, if (isAm) "AM" else "PM")
                            val startDate = timeFormat.parse(startStr)
                            val interval = intervalValue.toIntOrNull() ?: 1
                            if (startDate != null) {
                                cal.time = startDate
                                for (i in 0 until timesCount) {
                                    newTimes.add(timeFormat.format(cal.time))
                                    if (intervalUnit == "horas") {
                                        cal.add(java.util.Calendar.HOUR_OF_DAY, interval)
                                    } else {
                                        cal.add(java.util.Calendar.MINUTE, interval)
                                    }
                                }
                                onTargetTimesChanged(newTimes)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Generar horas")
                    }
                }
            }
        }
        
        // List of selected times
        if (targetTimes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Horas programadas (${targetTimes.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                targetTimes.forEach { time ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(time, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        IconButton(
                            onClick = { onTargetTimesChanged(targetTimes.filter { it != time }) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Filled.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

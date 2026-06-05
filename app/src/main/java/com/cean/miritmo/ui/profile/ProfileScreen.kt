package com.cean.miritmo.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cean.miritmo.components.PasswordTextField
import com.cean.miritmo.navigation.Screen
import com.cean.miritmo.viewmodel.AuthViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.cean.miritmo.util.CloudinaryHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditPasswordDialog by remember { mutableStateOf(false) }
    var showAvatarSelectionDialog by remember { mutableStateOf(false) }
    
    var newName by remember { mutableStateOf("") }
    var apodo by remember { mutableStateOf("") }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var photoUrlInput by remember { mutableStateOf("") }
    
    var isSaving by remember { mutableStateOf(false) }

    val notificationsEnabled by authViewModel.isNotificationsEnabled.collectAsState()
    val darkModeEnabled by authViewModel.isDarkMode.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.loadCurrentUser()
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isSaving = true
            coroutineScope.launch {
                val secureUrl = CloudinaryHelper.uploadImage(context, uri)
                if (secureUrl != null) {
                    authViewModel.updateProfilePicture(secureUrl) { success ->
                        isSaving = false
                        showAvatarSelectionDialog = false
                    }
                } else {
                    isSaving = false
                    showAvatarSelectionDialog = false
                    // Ideally show error
                }
            }
        }
    }

    val predefinedAvatars = listOf(
        "avatar_1" to Triple(Color(0xFF10B981), Icons.Filled.Person, "Verde"),
        "avatar_2" to Triple(Color(0xFF3B82F6), Icons.Filled.Face, "Azul"),
        "avatar_3" to Triple(Color(0xFFF472B6), Icons.Filled.Favorite, "Rosa"),
        "avatar_4" to Triple(Color(0xFFF59E0B), Icons.Filled.Star, "Naranja")
    )

    if (showAvatarSelectionDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSaving) showAvatarSelectionDialog = false },
            title = { Text("Actualizar foto de perfil") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Elige un avatar:", style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        predefinedAvatars.forEach { (id, data) ->
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(data.first)
                                    .clickable {
                                        if (!isSaving) {
                                            isSaving = true
                                            authViewModel.updateProfilePicture(id) {
                                                isSaving = false
                                                showAvatarSelectionDialog = false
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(data.second, contentDescription = data.third, tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                    Divider()
                    Text("O sube una imagen:", style = MaterialTheme.typography.bodyMedium)
                    Button(
                        onClick = { 
                            galleryLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving
                    ) {
                        Text("Subir desde galería")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { if (!isSaving) showAvatarSelectionDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSaving) showEditNameDialog = false },
            title = { Text("Editar Nombre") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nuevo nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = apodo,
                        onValueChange = { apodo = it },
                        label = { Text("Apodo (Opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newName.isNotBlank() && !isSaving) {
                            isSaving = true
                            authViewModel.updateProfile(newName,apodo) { success ->
                                isSaving = false
                                if (success) {
                                    showEditNameDialog = false
                                }
                            }
                        }
                    }
                ) {
                    if (isSaving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { if (!isSaving) showEditNameDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showEditPasswordDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSaving) showEditPasswordDialog = false },
            title = { Text("Cambiar Contraseña") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PasswordTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = "Contraseña antigua",
                        modifier = Modifier.fillMaxWidth(),
                        passwordVisible = passwordVisible,
                        onVisibilityToggle = { passwordVisible = !passwordVisible }
                    )
                    PasswordTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = "Nueva contraseña",
                        modifier = Modifier.fillMaxWidth(),
                        passwordVisible = passwordVisible,
                        onVisibilityToggle = { passwordVisible = !passwordVisible }
                    )
                    PasswordTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirmar nueva contraseña",
                        modifier = Modifier.fillMaxWidth(),
                        passwordVisible = passwordVisible,
                        onVisibilityToggle = { passwordVisible = !passwordVisible },
                        isError = newPassword.isNotBlank() && confirmPassword.isNotBlank() && newPassword != confirmPassword,
                        errorMessage = if (newPassword.isNotBlank() && confirmPassword.isNotBlank() && newPassword != confirmPassword) "Las contraseñas no coinciden" else null
                    )
                }
            },
            confirmButton = {
                val isFormValid = oldPassword.isNotBlank() && newPassword.isNotBlank() && newPassword == confirmPassword
                TextButton(
                    onClick = {
                        if (isFormValid && !isSaving) {
                            isSaving = true
                            authViewModel.updatePassword(oldPassword, newPassword) { success ->
                                isSaving = false
                                if (success) {
                                    showEditPasswordDialog = false
                                    oldPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                } else {
                                    // Handle failure (e.g., incorrect old password)
                                    // In a real app we would show a snackbar or error message.
                                }
                            }
                        }
                    },
                    enabled = isFormValid && !isSaving
                ) {
                    if (isSaving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text("Cambiar")
                }
            },
            dismissButton = {
                TextButton(onClick = { if (!isSaving) showEditPasswordDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp, start = 24.dp, end = 24.dp, top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Profile Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clickable { 
                                if (!isSaving) {
                                    photoUrlInput = if (currentUser?.photoUrl?.startsWith("avatar_") == false) currentUser?.photoUrl ?: "" else ""
                                    showAvatarSelectionDialog = true 
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Contenedor de la imagen (recortado en círculo)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            val photoUrl = currentUser?.photoUrl
                            if (!photoUrl.isNullOrBlank()) {
                                if (photoUrl.startsWith("avatar_")) {
                                    val avatarData = predefinedAvatars.find { it.first == photoUrl }?.second ?: predefinedAvatars[0].second
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(avatarData.first),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            avatarData.second,
                                            contentDescription = "Foto de perfil",
                                            tint = Color.White,
                                            modifier = Modifier.size(64.dp)
                                        )
                                    }
                                } else {
                                    AsyncImage(
                                        model = photoUrl,
                                        contentDescription = "Foto de perfil",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            } else {
                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = "Perfil",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }
                        
                        if (isSaving) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.White)
                            }
                        } else {
                            // Pequeño icono de edición superpuesto sin ser recortado por el contenedor padre
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = currentUser?.name ?: "Cargando...",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = currentUser?.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!currentUser?.apodo.isNullOrBlank()) {
                        Text(
                            text = "\"${currentUser?.apodo}\"",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Cuenta Section
            item {
                Text(
                    text = "CUENTA",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Filled.Edit,
                            title = "Editar Nombre",
                            onClick = {
                                newName = currentUser?.name ?: ""
                                apodo = currentUser?.apodo ?: ""
                                showEditNameDialog = true
                            }
                        )
                        Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        SettingsRow(
                            icon = Icons.Filled.Lock,
                            title = "Cambiar Contraseña",
                            onClick = {
                                oldPassword = ""
                                newPassword = ""
                                confirmPassword = ""
                                passwordVisible = false
                                showEditPasswordDialog = true
                            }
                        )
                    }
                }
            }

            // Preferencias Section
            item {
                Text(
                    text = "PREFERENCIAS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column {
                        SettingsRowWithSwitch(
                            icon = Icons.Filled.Notifications,
                            title = "Notificaciones",
                            checked = notificationsEnabled,
                            onCheckedChange = { authViewModel.setNotificationsEnabled(it) }
                        )
                        Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        SettingsRowWithSwitch(
                            icon = Icons.Filled.Settings,
                            title = "Modo Oscuro",
                            checked = darkModeEnabled,
                            onCheckedChange = { authViewModel.setDarkMode(it) }
                        )
                    }
                }
            }

            // Cerrar Sesion
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cerrar Sesión", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SettingsRowWithSwitch(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

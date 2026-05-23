package com.cean.miritmo

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.cean.miritmo.datastore.PreferencesManager
import com.cean.miritmo.firebase.AuthManager
import com.cean.miritmo.firebase.FirestoreManager
import com.cean.miritmo.navigation.AppNavGraph
import com.cean.miritmo.repository.AuthRepository
import com.cean.miritmo.repository.HabitRepository
import com.cean.miritmo.theme.MiRitmoTheme
import com.cean.miritmo.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Manual Dependency Injection
        val preferencesManager = PreferencesManager(applicationContext)
        val authManager = AuthManager()
        val firestoreManager = FirestoreManager()
        val authRepository = AuthRepository(authManager, firestoreManager, preferencesManager)
        val habitRepository = HabitRepository(firestoreManager)
        val factory = AppViewModelFactory(application, authRepository, habitRepository)

        setContent {
            val isDarkMode by preferencesManager.isDarkModeFlow.collectAsState(initial = false)
            
            MiRitmoTheme(darkTheme = isDarkMode == true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val permissions = mutableListOf<String>()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                        permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                    } else {
                        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }

                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions(),
                        onResult = { _ -> }
                    )

                    LaunchedEffect(Unit) {
                        permissionLauncher.launch(permissions.toTypedArray())
                    }

                    AppNavGraph(factory = factory)
                }
            }
        }
    }
}

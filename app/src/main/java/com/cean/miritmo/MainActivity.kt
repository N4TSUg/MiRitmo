package com.cean.miritmo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
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
        val factory = AppViewModelFactory(authRepository, habitRepository)

        setContent {
            MiRitmoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph(factory = factory)
                }
            }
        }
    }
}

package com.cean.miritmo.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.cean.miritmo.components.BottomNavBar
import com.cean.miritmo.ui.auth.LoginScreen
import com.cean.miritmo.ui.auth.RegisterScreen
import com.cean.miritmo.ui.home.HomeScreen
import com.cean.miritmo.ui.profile.ProfileScreen
import com.cean.miritmo.viewmodel.AppViewModelFactory
import com.cean.miritmo.viewmodel.AuthViewModel
import com.cean.miritmo.viewmodel.HabitsViewModel
import com.cean.miritmo.viewmodel.SearchViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    factory: AppViewModelFactory
) {
    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val habitsViewModel: HabitsViewModel = viewModel(factory = factory)
    val searchViewModel: SearchViewModel = viewModel(factory = factory)

    LaunchedEffect(Unit) {
        authViewModel.loadCurrentUser()
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Habits.route,
        Screen.Search.route,
        Screen.Progress.route,
        Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController)
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (authViewModel.isUserAuthenticated) Screen.Home.route else Screen.Login.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(navController, authViewModel)
            }
            composable(Screen.Register.route) {
                RegisterScreen(navController, authViewModel)
            }
            composable(Screen.Home.route) {
                val currentUser by authViewModel.currentUser.collectAsState()
                
                HomeScreen(
                    viewModel = habitsViewModel,
                    photoUrl = currentUser?.photoUrl,
                    onAddHabit = {
                        navController.navigate(Screen.AddHabit.route)
                    },
                    onNavigateToHabit = { habitId ->
                        navController.navigate(Screen.ManageHabit.createRoute(habitId))
                    },
                    onNavigateToTimer = { habitId ->
                        navController.navigate(Screen.Timer.createRoute(habitId))
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.AddHabit.route) {
                val currentUser by authViewModel.currentUser.collectAsState()
                com.cean.miritmo.ui.habits.AddHabitScreen(
                    navController = navController,
                    viewModel = habitsViewModel,
                    photoUrl = currentUser?.photoUrl
                )
            }
            composable(Screen.Habits.route) {
                com.cean.miritmo.ui.habits.HabitsScreen(
                    navController = navController,
                    viewModel = habitsViewModel
                )
            }
            composable(Screen.ManageHabit.route) { backStackEntry ->
                val habitId = backStackEntry.arguments?.getString("habitId") ?: ""
                com.cean.miritmo.ui.habits.EditHabitScreen(
                    navController = navController,
                    viewModel = habitsViewModel,
                    habitId = habitId
                )
            }
            composable(Screen.Timer.route) { backStackEntry ->
                val habitId = backStackEntry.arguments?.getString("habitId") ?: ""
                com.cean.miritmo.ui.habits.HabitTimerScreen(
                    navController = navController,
                    viewModel = habitsViewModel,
                    habitId = habitId
                )
            }
            composable(Screen.Progress.route) {
                com.cean.miritmo.ui.progress.ProgressScreen(
                    viewModel = habitsViewModel,
                    onNavigateToTimer = { habitId ->
                        navController.navigate(Screen.Timer.createRoute(habitId))
                    }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
            composable("history") {
                com.cean.miritmo.ui.habits.HistoryScreen(
                    navController = navController,
                    viewModel = habitsViewModel
                )
            }
            composable(Screen.Search.route) {
                com.cean.miritmo.ui.search.SearchScreen(
                    navController = navController,
                    viewModel = searchViewModel
                )
            }
            composable(Screen.UserProfile.route) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                com.cean.miritmo.ui.profile.UserProfileScreen(
                    navController = navController,
                    searchViewModel = searchViewModel,
                    habitsViewModel = habitsViewModel,
                    userId = userId
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title)
    }
}

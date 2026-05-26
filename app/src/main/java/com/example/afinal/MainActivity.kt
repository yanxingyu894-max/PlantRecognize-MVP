package com.example.afinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.afinal.data.local.PlantDatabase
import com.example.afinal.data.remote.RetrofitClient
import com.example.afinal.data.repository.PlantRepository
import com.example.afinal.ui.screens.*
import com.example.afinal.ui.theme.PlantAssistantTheme
import com.example.afinal.ui.viewmodel.PlantViewModel
import com.example.afinal.ui.viewmodel.PlantViewModelFactory

/**
 * MainActivity —— Main Entry and Optimized Navigation Controller Hub
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = PlantDatabase.getDatabase(this)
        val repository = PlantRepository(
            database.plantDao(),
            database.userDao(),
            RetrofitClient.trefleApiService,
            RetrofitClient.plantNetApiService,
            RetrofitClient.deepSeekApiService
        )

        setContent {
            PlantAssistantTheme {
                val navController = rememberNavController()
                val viewModel: PlantViewModel = viewModel(
                    factory = PlantViewModelFactory(repository)
                )

                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    // --- Home Screen ---
                    composable("home") {
                        HomeScreen(
                            onNavigateToList = { navController.navigateSafe("list") },
                            onNavigateToFav = { navController.navigateSafe("fav") },
                            onNavigateToRecognition = { navController.navigateSafe("recognition") },
                            onNavigateToCategory = { navController.navigateSafe("category") },
                            onNavigateToMy = { navController.navigateSafe("my") },
                            onNavigateToDetail = { plantId -> navController.navigateSafe("detail/$plantId") },
                            viewModel = viewModel
                        )
                    }

                    // --- Plant Encyclopedia List ---
                    composable("list") {
                        PlantListScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onNavigateToDetail = { plantId ->
                                navController.navigateSafe("detail/$plantId")
                            }
                        )
                    }

                    // --- Collection Favorites ---
                    composable("fav") {
                        FavoriteScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onNavigateToDetail = { plantId ->
                                navController.navigateSafe("detail/$plantId")
                            }
                        )
                    }

                    // --- Refined Grouped Staggered Category Screen ---
                    composable("category") {
                        PlantCategoryScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onNavigateToDetail = { plantId ->
                                navController.navigateSafe("detail/$plantId")
                            }
                        )
                    }

                    // --- My Garden Info Page ---
                    composable("my") {
                        MyScreen(
                            viewModel = viewModel,
                            onNavigateToHome = { navController.navigateSafe("home") },
                            onLogin = { navController.navigateSafe("login") },
                            onRegister = { navController.navigateSafe("register") },
                            onAbout = { navController.navigateSafe("about") },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // --- Authentication Screens ---
                    composable("login") {
                        LoginScreen(
                            viewModel = viewModel,
                            onNavigateToRegister = { navController.navigateSafe("register") },
                            onLoginSuccess = {
                                navController.navigate("my") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("register") {
                        RegisterScreen(
                            viewModel = viewModel,
                            onNavigateToLogin = { navController.navigateSafe("login") },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("about") {
                        AboutScreen(onBack = { navController.popBackStack() })
                    }

                    // --- AI Camera Scanner ---
                    composable("recognition") {
                        RecognitionScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onNavigateToDetail = { plantId ->
                                if (navController.currentBackStackEntry?.lifecycleIsResumed() == true) {
                                    navController.navigate("detail/$plantId") {
                                        popUpTo("recognition") { inclusive = true }
                                    }
                                }
                            }
                        )
                    }

                    // --- Plant Encyclopedia Detail Page ---
                    composable(
                        route = "detail/{plantId}",
                        arguments = listOf(navArgument("plantId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val plantId = backStackEntry.arguments?.getString("plantId") ?: ""
                        DetailScreen(
                            plantId = plantId,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Safe navigation utility to block duplicate execution triggers
 */
fun NavHostController.navigateSafe(route: String) {
    if (currentBackStackEntry?.lifecycleIsResumed() == true) {
        navigate(route)
    }
}

/**
 * Validates whether the controller transaction backstack is active
 */
fun NavBackStackEntry.lifecycleIsResumed() =
    this.lifecycle.currentState == Lifecycle.State.RESUMED

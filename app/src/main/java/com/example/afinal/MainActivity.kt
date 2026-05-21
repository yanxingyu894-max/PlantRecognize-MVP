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
 * MainActivity —— 应用的主入口和导航中枢
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = PlantDatabase.getDatabase(this)
        // 使用新的 Trefle 和 PlantNet 服务
        val repository = PlantRepository(
            database.plantDao(),
            database.userDao(),
            RetrofitClient.trefleApiService,
            RetrofitClient.plantNetApiService
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
                    // --- 首页 ---
                    composable("home") {
                        HomeScreen(
                            onNavigateToList = { navController.navigateSafe("list") },
                            onNavigateToFav = { navController.navigateSafe("fav") },
                            onNavigateToRecognition = { navController.navigateSafe("recognition") },
                            onNavigateToCategory = { navController.navigateSafe("category") },
                            onNavigateToMy = { navController.navigateSafe("my") },
                            viewModel = viewModel
                        )
                    }

                    // --- 植物百科列表页 ---
                    composable("list") {
                        PlantListScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onNavigateToDetail = { plantId ->
                                navController.navigateSafe("detail/$plantId")
                            }
                        )
                    }

                    // --- 收藏页 ---
                    composable("fav") {
                        FavoriteScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onNavigateToDetail = { plantId ->
                                navController.navigateSafe("detail/$plantId")
                            }
                        )
                    }

                    // --- 分类页 ---
                    composable("category") {
                        PlantCategoryScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onNavigateToDetail = { plantId ->
                                navController.navigateSafe("detail/$plantId")
                            }
                        )
                    }

                    // --- 我的页 ---
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

                    // --- 登录/注册/关于 ---
                    composable("login") {
                        LoginScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }

                    composable("register") {
                        RegisterScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }

                    composable("about") {
                        AboutScreen(onBack = { navController.popBackStack() })
                    }

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

                    // --- 植物详情页 ---
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
 * 安全导航扩展：防止多次点击导致的重复跳转
 */
fun NavHostController.navigateSafe(route: String) {
    if (currentBackStackEntry?.lifecycleIsResumed() == true) {
        navigate(route)
    }
}

/**
 * 检查当前 BackStackEntry 是否处于 RESUMED 状态
 */
fun NavBackStackEntry.lifecycleIsResumed() =
    this.lifecycle.currentState == Lifecycle.State.RESUMED

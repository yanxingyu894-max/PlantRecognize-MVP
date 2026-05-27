package com.example.afinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
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
 * 主活动类：整个App的入口，负责初始化核心组件和管理页面导航
 * ComponentActivity是Android Jetpack Compose的基础活动类
 * 功能：
 * 1. 初始化数据库、网络请求、数据仓库等核心依赖
 * 2. 配置Compose界面和主题
 * 3. 管理所有页面的导航逻辑（比如从首页跳转到详情页、登录页等）
 */
class MainActivity : ComponentActivity() {
    /**
     * 活动创建时执行的核心方法：App启动/重建时调用
     * @param savedInstanceState 保存的活动状态（比如屏幕旋转时的数据），可为空
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 初始化本地数据库：获取数据库单例（保证整个App只有一个数据库实例）
        val database = PlantDatabase.getDatabase(this)
        // 2. 初始化数据仓库：整合本地数据库和远程网络接口，统一管理数据读写
        val repository = PlantRepository(
            database.plantDao(), // 植物数据操作接口（本地数据库）
            database.userDao(), // 用户数据操作接口（本地数据库）
            RetrofitClient.trefleApiService, // 植物百科网络接口
            RetrofitClient.plantNetApiService, // 植物识别网络接口
            RetrofitClient.deepSeekApiService // AI相关网络接口
        )

        // 3. 设置Compose界面内容（Jetpack Compose的UI入口）
        setContent {
            // 应用自定义主题（包含颜色、字体、样式等）
            PlantAssistantTheme {
                // 创建导航控制器：管理页面跳转、回退、栈管理等
                val navController = rememberNavController()
                // 创建ViewModel：管理页面数据和业务逻辑（通过工厂类注入仓库依赖）
                val viewModel: PlantViewModel = viewModel(
                    factory = PlantViewModelFactory(repository)
                )

                // 导航容器：管理所有可跳转的页面，定义页面间的导航规则
                NavHost(
                    navController = navController, // 绑定导航控制器
                    startDestination = "home" // App启动后默认显示的页面（首页）
                ) {
                    // --- 首页：定义首页的导航规则和页面组件 ---
                    composable("home") {
                        // 加载首页UI组件，并传递导航回调和ViewModel
                        HomeScreen(
                            onNavigateToList = { navController.navigateSafe("list") }, // 跳转到植物列表页
                            onNavigateToFav = { navController.navigateSafe("fav") }, // 跳转到收藏页
                            onNavigateToRecognition = { navController.navigateSafe("recognition") }, // 跳转到植物识别页
                            onNavigateToCategory = { navController.navigateSafe("category") }, // 跳转到分类页
                            onNavigateToMy = { navController.navigateTab("my") }, // 跳转到“我的”页（底部导航栏专用）
                            onNavigateToDetail = { plantId -> navController.navigateSafe("detail/$plantId") }, // 跳转到植物详情页（带植物ID参数）
                            viewModel = viewModel // 传递ViewModel给页面
                        )
                    }

                    // --- 植物百科列表页 ---
                    composable("list") {
                        PlantListScreen(
                            viewModel = viewModel, // 数据和业务逻辑依赖
                            onBack = { navController.popBackStack() }, // 点击返回按钮：回到上一页
                            onNavigateToDetail = { plantId -> // 点击植物项：跳转到详情页
                                navController.navigateSafe("detail/$plantId")
                            }
                        )
                    }

                    // --- 收藏页：显示用户收藏的植物 ---
                    composable("fav") {
                        FavoriteScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }, // 返回上一页
                            onNavigateToDetail = { plantId -> // 跳转到收藏植物的详情页
                                navController.navigateSafe("detail/$plantId")
                            }
                        )
                    }

                    // --- 植物分类页：按类别展示植物 ---
                    composable("category") {
                        PlantCategoryScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }, // 返回上一页
                            onNavigateToDetail = { plantId -> // 跳转到分类植物的详情页
                                navController.navigateSafe("detail/$plantId")
                            }
                        )
                    }

                    // --- “我的”页面：个人中心（登录/注册、关于、返回首页等） ---
                    composable("my") {
                        MyScreen(
                            viewModel = viewModel,
                            onNavigateToHome = { navController.navigateTab("home") }, // 跳转到首页（底部导航栏专用）
                            onLogin = { navController.navigateSafe("login") }, // 跳转到登录页
                            onRegister = { navController.navigateSafe("register") }, // 跳转到注册页
                            onAbout = { navController.navigateSafe("about") }, // 跳转到关于页
                            onBack = { navController.popBackStack() } // 返回上一页
                        )
                    }

                    // --- 登录页：用户账号密码登录 ---
                    composable("login") {
                        LoginScreen(
                            viewModel = viewModel,
                            onNavigateToRegister = { navController.navigateSafe("register") }, // 跳转到注册页
                            onLoginSuccess = { // 登录成功后的操作
                                navController.navigate("my") {
                                    // 登录成功后，从返回栈中移除登录页（避免返回时又回到登录页）
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onBack = { navController.popBackStack() } // 返回上一页
                        )
                    }

                    // --- 注册页：用户账号密码注册 ---
                    composable("register") {
                        RegisterScreen(
                            viewModel = viewModel,
                            onNavigateToLogin = { navController.navigateSafe("login") }, // 跳转到登录页
                            onBack = { navController.popBackStack() } // 返回上一页
                        )
                    }

                    // --- 关于页：显示App版本、说明等信息 ---
                    composable("about") {
                        AboutScreen(onBack = { navController.popBackStack() }) // 返回上一页
                    }

                    // --- AI相机识别页：拍照/选图识别植物 ---
                    composable("recognition") {
                        RecognitionScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }, // 返回上一页
                            onNavigateToDetail = { plantId -> // 识别成功后跳转到植物详情页
                                // 安全检查：确保当前页面处于活跃状态，避免重复跳转
                                if (navController.currentBackStackEntry?.lifecycleIsResumed() == true) {
                                    navController.navigate("detail/$plantId") {
                                        // 识别后移除识别页，避免返回时重复识别
                                        popUpTo("recognition") { inclusive = true }
                                    }
                                }
                            }
                        )
                    }

                    // --- 植物详情页：显示单个植物的详细信息（带参数路由） ---
                    composable(
                        route = "detail/{plantId}", // 路由规则：带植物ID参数（比如detail/123）
                        arguments = listOf(navArgument("plantId") { type = NavType.StringType }) // 定义参数类型为字符串
                    ) { backStackEntry ->
                        // 从导航参数中获取植物ID（为空则设为空字符串）
                        val plantId = backStackEntry.arguments?.getString("plantId") ?: ""
                        // 加载详情页UI，传递植物ID和ViewModel
                        DetailScreen(
                            plantId = plantId,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() } // 返回上一页
                        )
                    }
                }
            }
        }
    }
}

/**
 * 导航控制器扩展方法：安全跳转（防止重复点击导致多次跳转）
 * @param route 目标页面的路由（比如"home"、"login"）
 * 原理：只有当前页面处于“已恢复”状态（用户可见、可交互）时，才执行跳转
 */
fun NavHostController.navigateSafe(route: String) {
    // 检查当前页面的生命周期状态是否为RESUMED（活跃）
    if (currentBackStackEntry?.lifecycleIsResumed() == true) {
        navigate(route) // 执行跳转
    }
}

/**
 * 导航控制器扩展方法：底部导航栏专用跳转（避免返回栈堆积）
 * @param route 目标页面的路由（底部导航栏的页面：首页、我的等）
 * 特点：
 * 1. 跳转到目标页时，清空返回栈到首页（避免栈里有大量重复页面）
 * 2. 同一页面重复点击时，不创建新实例（launchSingleTop）
 * 3. 恢复之前的页面状态（比如首页的滚动位置）
 */
fun NavHostController.navigateTab(route: String) {
    if (currentBackStackEntry?.lifecycleIsResumed() == true) {
        navigate(route) {
            // 跳转到导航图的起始页（首页），并保存当前状态
            popUpTo(graph.findStartDestination().id) {
                saveState = true
            }
            // 避免重复点击时创建多个相同页面
            launchSingleTop = true
            // 重新选择页面时恢复之前的状态（比如滚动位置、输入内容）
            restoreState = true
        }
    }
}

/**
 * 导航返回栈条目扩展方法：检查页面是否处于活跃状态
 * @return true=页面已恢复（用户可见、可交互），false=页面未活跃（比如后台、销毁）
 * 用途：防止页面未活跃时执行跳转/数据操作，避免异常
 */
fun NavBackStackEntry.lifecycleIsResumed() =
    this.lifecycle.currentState == Lifecycle.State.RESUMED
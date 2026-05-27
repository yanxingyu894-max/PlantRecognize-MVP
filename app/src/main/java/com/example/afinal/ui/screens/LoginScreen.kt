package com.example.afinal.ui.screens

// 导入Compose相关的UI布局、组件、状态管理等核心库
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afinal.ui.theme.PlantGreenPrimary // 自定义主题色（植物绿）
import com.example.afinal.ui.viewmodel.PlantViewModel // 业务逻辑处理的ViewModel
import kotlinx.coroutines.launch // 协程启动工具，用于异步处理登录请求

/**
 * 登录页面组件
 * 外行人理解：这是APP的登录界面，包含输入账号密码、记住我、忘记密码、登录按钮等功能
 * @param viewModel 处理植物相关（含用户登录）业务逻辑的核心类，相当于"大脑"，负责和后台/数据库交互
 * @param onNavigateToRegister 点击"注册"文字时的跳转回调（跳转到注册页）
 * @param onLoginSuccess 登录成功后的回调（比如跳转到首页）
 * @param onBack 点击"返回"按钮的回调（比如回到上一级页面）
 */
@Composable
fun LoginScreen(
    viewModel: PlantViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit
) {
    // 以下是页面的状态变量（记住这些值，页面刷新不会丢失）
    var username by remember { mutableStateOf("") } // 用户名/邮箱输入框内容，初始为空
    var password by remember { mutableStateOf("") } // 密码输入框内容，初始为空
    var passwordVisible by remember { mutableStateOf(false) } // 密码是否可见，默认隐藏
    var errorMessage by remember { mutableStateOf("") } // 错误提示文字，初始为空
    var rememberMe by remember { mutableStateOf(false) } // "记住我"复选框状态，默认未勾选
    var isLoading by remember { mutableStateOf(false) } // 登录请求中是否加载中，默认未加载

    // 协程作用域：用于在Compose中启动异步任务（比如登录请求），不阻塞UI
    val coroutineScope = rememberCoroutineScope()

    // 表单验证逻辑：判断输入是否合法
    val isUsernameValid = username.isNotBlank() // 用户名非空即为合法
    val isPasswordValid = password.isNotBlank() // 密码非空即为合法
    val isFormValid = isUsernameValid && isPasswordValid // 用户名和密码都合法，表单才合法

    // 页面根布局：Surface是Compose的基础容器，类似"画布"
    Surface(
        modifier = Modifier
            .fillMaxSize() // 占满整个屏幕
            .background(Color(0xFFF5F5F5)) // 背景色设为浅灰色
    ) {
        // 垂直列布局：所有子组件从上到下排列
        Column(
            modifier = Modifier
                .fillMaxSize() // 占满父容器
                .verticalScroll(rememberScrollState()) // 允许垂直滚动（内容超出屏幕时）
                .padding(horizontal = 24.dp), // 左右内边距24dp（手机屏幕单位）
            verticalArrangement = Arrangement.spacedBy(16.dp), // 子组件之间垂直间距16dp
            horizontalAlignment = Alignment.CenterHorizontally // 子组件水平居中
        ) {
            // 空白占位符：顶部留60dp空白，让界面不顶格
            Spacer(modifier = Modifier.height(60.dp))

            // 欢迎文字：大标题
            Text("Welcome Back", fontSize = 32.sp, fontWeight = FontWeight.Black, color = PlantGreenPrimary)
            // 副标题：提示用户登录
            Text("Login to continue exploring plants", fontSize = 14.sp, color = Color.Gray)
            // 空白占位符：标题和输入框之间留32dp空白
            Spacer(modifier = Modifier.height(32.dp))

            // 用户名/邮箱输入框
            OutlinedTextField(
                value = username, // 输入框显示的内容（绑定上面的username变量）
                onValueChange = { username = it; errorMessage = "" }, // 输入内容变化时更新变量，并清空错误提示
                label = { Text("Username or Email") }, // 输入框的提示文字（比如"请输入用户名/邮箱"）
                modifier = Modifier.fillMaxWidth(), // 输入框占满父容器宽度
                leadingIcon = { Icon(Icons.Default.Email, "Username", tint = PlantGreenPrimary) }, // 左侧邮箱图标
                shape = RoundedCornerShape(12.dp), // 输入框圆角12dp（更美观）
                isError = errorMessage.isNotBlank() && !isUsernameValid // 有错误提示且用户名不合法时，输入框变红
            )

            // 密码输入框
            OutlinedTextField(
                value = password, // 绑定密码变量
                onValueChange = { password = it; errorMessage = "" }, // 输入变化时更新密码，清空错误
                label = { Text("Password") }, // 提示文字"密码"
                modifier = Modifier.fillMaxWidth(), // 占满宽度
                leadingIcon = { Icon(Icons.Default.Lock, "Password", tint = PlantGreenPrimary) }, // 左侧锁图标
                // 密码是否隐藏：visible则显示明文，否则显示圆点
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { // 右侧图标（切换密码可见性）
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility" // 辅助功能描述（无障碍）
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp), // 圆角
                isError = errorMessage.isNotBlank() && !isPasswordValid // 有错误且密码不合法时变红
            )

            // 行布局：包含"记住我"和"忘记密码"
            Row(
                modifier = Modifier.fillMaxWidth(), // 占满宽度
                horizontalArrangement = Arrangement.SpaceBetween, // 子组件左右两端对齐
                verticalAlignment = Alignment.CenterVertically // 垂直居中
            ) {
                // "记住我"复选框行
                Row(
                    verticalAlignment = Alignment.CenterVertically, // 垂直居中
                    modifier = Modifier.clickable { rememberMe = !rememberMe } // 点击整行也能切换复选框
                ) {
                    Checkbox(
                        checked = rememberMe, // 绑定"记住我"状态
                        onCheckedChange = { rememberMe = it }, // 勾选状态变化时更新变量
                        colors = CheckboxDefaults.colors(checkedColor = PlantGreenPrimary) // 勾选后颜色为植物绿
                    )
                    Text("Remember me", fontSize = 13.sp, color = Color.Gray) // "记住我"文字
                }
                // "忘记密码"文字（点击暂未实现功能）
                Text("Forgot Password?", fontSize = 13.sp, color = PlantGreenPrimary, modifier = Modifier.clickable { })
            }

            // 错误提示卡片：有错误信息时显示
            if (errorMessage.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(), // 占满宽度
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), // 浅红色背景
                    shape = RoundedCornerShape(8.dp) // 圆角
                ) {
                    Text(errorMessage, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(12.dp)) // 红色错误文字
                }
            }

            // 空白占位符：错误提示和登录按钮之间留12dp
            Spacer(modifier = Modifier.height(12.dp))

            // 登录按钮
            Button(
                onClick = {
                    // 表单不合法时（用户名/密码为空），显示对应错误
                    if (!isFormValid) {
                        errorMessage = when {
                            !isUsernameValid -> "Please enter your username" // 未输入用户名
                            !isPasswordValid -> "Please enter your password" // 未输入密码
                            else -> "Please fill in all details" // 其他情况（理论上不会触发）
                        }
                    } else {
                        errorMessage = "" // 清空错误提示
                        isLoading = true // 标记为加载中（显示转圈）
                        // 启动协程：异步执行登录请求（不卡UI）
                        coroutineScope.launch {
                            // 调用ViewModel的登录方法，传入用户名和密码
                            val result = viewModel.loginUser(username, password)
                            isLoading = false // 登录请求完成，停止加载
                            if (result.isSuccess) {
                                onLoginSuccess() // 登录成功，执行回调（比如跳首页）
                            } else {
                                // 登录失败，显示错误信息（取异常信息或默认文字）
                                errorMessage = result.exceptionOrNull()?.message ?: "Login Failed"
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp), // 占满宽度，高度50dp
                enabled = !isLoading, // 加载中时按钮不可点击
                shape = RoundedCornerShape(12.dp), // 圆角
                // 按钮颜色：正常为植物绿，禁用时为灰色
                colors = ButtonDefaults.buttonColors(containerColor = PlantGreenPrimary, disabledContainerColor = Color.Gray)
            ) {
                // 加载中显示转圈，否则显示"Login"文字
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Login", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            // 分割线+Or：登录按钮和注册文字之间的分隔
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f)) // 左侧分割线（占1份宽度）
                Text("Or", modifier = Modifier.padding(horizontal = 8.dp), color = Color.Gray) // 中间"Or"文字
                HorizontalDivider(modifier = Modifier.weight(1f)) // 右侧分割线（占1份宽度）
            }

            // 注册引导行："没有账号？立即注册"
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("No account?", fontSize = 14.sp, color = Color.Gray) // "没有账号？"
                Text(
                    "Register Now",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PlantGreenPrimary,
                    modifier = Modifier.padding(start = 4.dp).clickable { onNavigateToRegister() } // 点击跳注册页
                )
            }

            // 空白占位符
            Spacer(modifier = Modifier.height(12.dp))

            // 返回按钮
            OutlinedButton(
                onClick = onBack, // 点击执行返回回调
                modifier = Modifier.fillMaxWidth().height(50.dp), // 占满宽度，高度50dp
                shape = RoundedCornerShape(12.dp) // 圆角
            ) {
                Text("Back", fontSize = 16.sp) // "返回"文字
            }

            // 底部空白占位符
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
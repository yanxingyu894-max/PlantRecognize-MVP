package com.example.afinal.ui.screens

// 导入Compose核心UI库、状态管理、图标等
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.example.afinal.ui.theme.PlantGreenPrimary // 自定义植物绿主题色
import com.example.afinal.ui.viewmodel.PlantViewModel // 业务逻辑ViewModel
import kotlinx.coroutines.delay // 协程延迟工具（用于显示成功提示后跳转）
import kotlinx.coroutines.launch // 协程启动工具

/**
 * 注册页面组件
 * 外行人理解：这是APP的注册界面，需要输入用户名、邮箱、密码、确认密码，勾选协议后才能注册
 * @param viewModel 处理注册业务的核心类，负责和后台交互完成注册
 * @param onNavigateToLogin 注册成功后跳转到登录页的回调
 * @param onBack 点击"返回登录"按钮的回调
 */
@Composable
fun RegisterScreen(viewModel: PlantViewModel, onNavigateToLogin: () -> Unit, onBack: () -> Unit) {
    // 页面状态变量（remember保证页面刷新不丢失值）
    var username by remember { mutableStateOf("") } // 用户名输入框，初始为空
    var password by remember { mutableStateOf("") } // 密码输入框，初始为空
    var confirmPassword by remember { mutableStateOf("") } // 确认密码输入框，初始为空
    var email by remember { mutableStateOf("") } // 邮箱输入框，初始为空
    var agree by remember { mutableStateOf(false) } // 是否同意协议，默认未勾选
    var passwordVisible by remember { mutableStateOf(false) } // 密码是否可见，默认隐藏
    var confirmPasswordVisible by remember { mutableStateOf(false) } // 确认密码是否可见，默认隐藏
    var errorMessage by remember { mutableStateOf("") } // 错误提示文字，初始为空
    var successMessage by remember { mutableStateOf("") } // 成功提示文字，初始为空
    var isLoading by remember { mutableStateOf(false) } // 注册请求是否加载中，默认未加载

    // 协程作用域：用于异步执行注册请求，不阻塞UI
    val coroutineScope = rememberCoroutineScope()

    // 表单验证规则：判断输入是否合法
    val isUsernameValid = username.isNotBlank() && username.length >= 3 // 用户名非空且至少3个字符
    val isEmailValid = email.isNotBlank() && email.contains("@") // 邮箱非空且包含@（简单验证）
    val isPasswordValid = password.isNotBlank() && password.length >= 6 // 密码非空且至少6个字符
    val isPasswordMatching = password == confirmPassword && password.isNotBlank() // 两次密码一致且非空
    val isFormValid = isUsernameValid && isEmailValid && isPasswordValid && isPasswordMatching && agree // 所有条件满足，表单才合法

    // 页面根容器：Surface是Compose的基础画布
    Surface(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)) // 占满屏幕，背景浅灰色
    ) {
        // 垂直列布局：子组件从上到下排列
        Column(
            modifier = Modifier
                .fillMaxSize() // 占满父容器
                .verticalScroll(rememberScrollState()) // 内容超出时可垂直滚动
                .padding(horizontal = 24.dp), // 左右内边距24dp
            verticalArrangement = Arrangement.spacedBy(16.dp), // 子组件垂直间距16dp
            horizontalAlignment = Alignment.CenterHorizontally // 子组件水平居中
        ) {
            // 顶部空白占位符：留32dp空白，避免内容顶格
            Spacer(modifier = Modifier.height(32.dp))

            // 注册标题
            Text("Create Account", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = PlantGreenPrimary)
            // 副标题：提示语
            Text("Join the plant community today", fontSize = 14.sp, color = Color.Gray)

            // 标题和输入框之间的空白
            Spacer(modifier = Modifier.height(24.dp))

            // 用户名输入框
            OutlinedTextField(
                value = username, // 绑定用户名变量
                onValueChange = { username = it; errorMessage = "" }, // 输入变化时更新变量，清空错误
                label = { Text("Username") }, // 提示文字"用户名"
                modifier = Modifier.fillMaxWidth(), // 占满宽度
                shape = RoundedCornerShape(12.dp), // 圆角12dp
                isError = username.isNotBlank() && !isUsernameValid, // 输入了但不合法时变红
                // 辅助提示文字：输入不合法时显示"至少3个字符"
                supportingText = { if (username.isNotBlank() && !isUsernameValid) Text("Minimum 3 characters", color = Color.Red, fontSize = 11.sp) }
            )

            // 邮箱输入框
            OutlinedTextField(
                value = email, // 绑定邮箱变量
                onValueChange = { email = it; errorMessage = "" }, // 输入变化更新变量，清空错误
                label = { Text("Email Address") }, // 提示文字"邮箱地址"
                modifier = Modifier.fillMaxWidth(), // 占满宽度
                shape = RoundedCornerShape(12.dp), // 圆角
                isError = email.isNotBlank() && !isEmailValid, // 输入了但不合法时变红
                // 辅助提示：输入不合法时显示"请输入有效邮箱"
                supportingText = { if (email.isNotBlank() && !isEmailValid) Text("Please enter a valid email", color = Color.Red, fontSize = 11.sp) }
            )

            // 密码输入框
            OutlinedTextField(
                value = password, // 绑定密码变量
                onValueChange = { password = it; errorMessage = "" }, // 输入变化更新变量，清空错误
                label = { Text("Password") }, // 提示文字"密码"
                modifier = Modifier.fillMaxWidth(), // 占满宽度
                shape = RoundedCornerShape(12.dp), // 圆角
                // 密码是否隐藏：visible则显示明文，否则圆点
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { // 右侧切换可见性图标
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = "Toggle password")
                    }
                },
                isError = password.isNotBlank() && !isPasswordValid, // 输入了但不合法时变红
                // 辅助提示：输入不合法时显示"至少6个字符"
                supportingText = { if (password.isNotBlank() && !isPasswordValid) Text("Minimum 6 characters", color = Color.Red, fontSize = 11.sp) }
            )

            // 确认密码输入框
            OutlinedTextField(
                value = confirmPassword, // 绑定确认密码变量
                onValueChange = { confirmPassword = it; errorMessage = "" }, // 输入变化更新变量，清空错误
                label = { Text("Confirm Password") }, // 提示文字"确认密码"
                modifier = Modifier.fillMaxWidth(), // 占满宽度
                shape = RoundedCornerShape(12.dp), // 圆角
                // 确认密码是否隐藏
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { // 右侧显示"匹配/不匹配"图标
                    if (confirmPassword.isNotBlank()) {
                        Icon(
                            if (isPasswordMatching) Icons.Default.Check else Icons.Default.Close, // 匹配显示√，不匹配显示×
                            contentDescription = "Match Status", // 辅助功能描述
                            tint = if (isPasswordMatching) Color.Green else Color.Red // 匹配绿色，不匹配红色
                        )
                    }
                },
                isError = confirmPassword.isNotBlank() && !isPasswordMatching, // 输入了但不匹配时变红
                // 辅助提示：不匹配时显示"密码不一致"
                supportingText = { if (confirmPassword.isNotBlank() && !isPasswordMatching) Text("Passwords do not match", color = Color.Red, fontSize = 11.sp) }
            )

            // 错误提示卡片：有错误时显示
            if (errorMessage.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), shape = RoundedCornerShape(8.dp)) {
                    Text(errorMessage, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(12.dp)) // 红色错误文字
                }
            }

            // 成功提示卡片：有成功信息时显示
            if (successMessage.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), shape = RoundedCornerShape(8.dp)) {
                    Text(successMessage, color = Color(0xFF2E7D32), fontSize = 12.sp, modifier = Modifier.padding(12.dp)) // 绿色成功文字
                }
            }

            // 协议勾选行："我同意条款和隐私政策"
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = agree, onCheckedChange = { agree = it }, colors = CheckboxDefaults.colors(checkedColor = PlantGreenPrimary)) // 勾选框
                Text("I agree to the Terms and Privacy Policy", fontSize = 13.sp, modifier = Modifier.clickable { agree = !agree }) // 点击文字也能勾选
            }

            // 空白占位符
            Spacer(modifier = Modifier.height(12.dp))

            // 注册按钮
            Button(
                onClick = {
                    // 表单不合法时，显示对应错误
                    if (!isFormValid) {
                        errorMessage = when {
                            !isUsernameValid -> "Username format is invalid." // 用户名格式错误
                            !isEmailValid -> "Email format is invalid." // 邮箱格式错误
                            !isPasswordValid -> "Password must be at least 6 characters." // 密码长度不够
                            !isPasswordMatching -> "Passwords do not match." // 密码不一致
                            !agree -> "Please agree to the Terms." // 未同意协议
                            else -> "Please fill in all information." // 其他情况
                        }
                    } else {
                        isLoading = true // 标记加载中（显示转圈）
                        // 启动协程：异步执行注册请求
                        coroutineScope.launch {
                            // 调用ViewModel的注册方法
                            val result = viewModel.registerUser(username, password, agree)
                            isLoading = false // 注册请求完成，停止加载
                            if (result.isSuccess) {
                                successMessage = "Registration successful! Redirecting..." // 注册成功提示
                                errorMessage = "" // 清空错误
                                delay(1200) // 延迟1.2秒（让用户看到成功提示）
                                onNavigateToLogin() // 跳转到登录页
                            } else {
                                // 注册失败，显示错误信息
                                errorMessage = result.exceptionOrNull()?.message ?: "Registration Failed"
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp), // 占满宽度，高度50dp
                enabled = !isLoading, // 加载中时按钮不可点击
                shape = RoundedCornerShape(12.dp), // 圆角
                // 按钮颜色：正常植物绿，禁用灰色
                colors = ButtonDefaults.buttonColors(containerColor = PlantGreenPrimary, disabledContainerColor = Color.Gray)
            ) {
                // 加载中显示转圈，否则显示"Register Account"
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Register Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            // 返回登录按钮
            OutlinedButton(
                onClick = onBack, // 点击返回登录页
                modifier = Modifier.fillMaxWidth().height(50.dp), // 占满宽度，高度50dp
                shape = RoundedCornerShape(12.dp) // 圆角
            ) {
                Text("Back to Login", fontSize = 16.sp) // "返回登录"文字
            }

            // 底部空白占位符
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
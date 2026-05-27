package com.example.afinal.ui.screens

// 导入安卓权限相关类
import android.Manifest
// 导入安卓上下文相关类，用于获取应用环境信息
import android.content.Context
// 导入权限检查相关类
import android.content.pm.PackageManager
// 导入屏幕方向配置相关类
import android.content.res.Configuration
// 导入安卓Uri类，用于处理文件路径
import android.net.Uri
// 导入Compose的Activity结果启动器相关类，用于申请权限/选择图片
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
// 导入CameraX相机相关核心类
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
// 导入Compose动画相关类
import androidx.compose.animation.core.*
// 导入Compose布局相关基础组件
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
// 导入Compose Material3图标和组件
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
// 导入Compose状态管理相关类
import androidx.compose.runtime.*
// 导入Compose布局对齐相关类
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// 导入自定义主题颜色
import com.example.afinal.ui.theme.PlantGreenPrimary
// 导入ViewModel，用于处理植物识别的业务逻辑
import com.example.afinal.ui.viewmodel.PlantViewModel
// 导入Coil图片加载库，用于异步加载图片
import coil.compose.rememberAsyncImagePainter
// 导入文件操作相关类
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executor

// 声明使用ExperimentalMaterial3Api（Material3部分功能仍在实验阶段）
@OptIn(ExperimentalMaterial3Api::class)
/**
 * 植物智能识别页面的核心Compose组件
 * @param viewModel 植物识别的ViewModel，处理数据逻辑和状态管理
 * @param onBack 点击返回按钮的回调函数，用于返回上一级页面
 * @param onNavigateToDetail 识别成功后跳转到植物详情页的回调函数，参数为植物ID
 */
@Composable
fun RecognitionScreen(
    viewModel: PlantViewModel,
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    // 获取当前应用的上下文（Context），包含应用环境、资源等信息
    val context = LocalContext.current
    // 获取当前设备的配置信息（如屏幕方向）
    val configuration = LocalConfiguration.current
    // 判断是否为横屏模式：屏幕方向等于横屏配置
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // 从ViewModel中收集植物识别结果的状态（自动感知生命周期，避免内存泄漏）
    val recognitionResult by viewModel.recognitionResult.collectAsStateWithLifecycle()
    // 从ViewModel中收集识别是否正在进行的状态
    val recognitionInProgress by viewModel.recognitionInProgress.collectAsStateWithLifecycle()
    // 从ViewModel中收集选中图片路径的状态（拍照/从相册选择的图片）
    val selectedImagePath by viewModel.selectedImagePath.collectAsStateWithLifecycle()

    // 初始化ImageCapture实例，用于CameraX拍摄照片
    val imageCapture = remember { ImageCapture.Builder().build() }

    // 声明并初始化相机权限状态：检查是否已授予相机权限
    // remember{} 用于缓存状态，避免重组时重复计算
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    // 初始化相机权限申请的启动器
    // ActivityResultContracts.RequestPermission() 用于申请单个权限
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        // 权限申请结果回调：更新相机权限状态
        onResult = { granted -> hasCameraPermission = granted }
    )

    // 初始化相册选择图片的启动器
    // ActivityResultContracts.GetContent() 用于从系统选择文件（此处限定为图片）
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // 当用户从相册选择图片后，处理选中的图片Uri
        uri?.let {
            // 将Uri转换为本地文件
            val file = uriToFile(context, it)
            if (file != null) {
                // 更新ViewModel中选中图片的路径
                viewModel.setSelectedImagePath(file.absolutePath)
                // 调用ViewModel的方法识别植物
                viewModel.identifyPlant(file)
            } else {
                // 图片处理失败时显示Toast提示
                android.widget.Toast.makeText(context, "Image processing failed.", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    // LaunchedEffect：在组件首次组合时执行（Unit为key，仅执行一次）
    // 作用：如果没有相机权限，自动申请相机权限
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // 初始化无限循环动画，用于识别中显示的扫描线动画
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    // 扫描线的垂直位移动画：0f -> 1f 循环往返，持续2秒，线性动画
    val translateY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing), // tween：线性动画，时长2000ms
            repeatMode = RepeatMode.Reverse // 反向重复（1f -> 0f -> 1f...）
        ),
        label = "line"
    )

    // Scaffold：Compose的基础布局组件，包含顶部栏、内容区、底部栏等
    Scaffold(
        // 顶部应用栏配置
        topBar = {
            TopAppBar(
                // 标题文字："Smart Recognition"，白色
                title = { Text("Smart Recognition", color = Color.White) },
                // 导航图标（返回按钮）：点击触发onBack回调
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                // 右侧操作按钮（相册）：点击打开相册选择图片
                actions = {
                    IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Album", tint = Color.White)
                    }
                },
                // 顶部栏颜色：半透明黑色（0.5透明度）
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black.copy(alpha = 0.5f))
            )
        }
    ) { padding ->
        // 根布局：填充全屏，黑色背景，添加Scaffold的内边距（避免内容被顶部栏遮挡）
        Box(modifier = Modifier.fillMaxSize().background(Color.Black).padding(padding)) {

            // 条件：有相机权限 + 未选择图片 + 识别未进行 → 显示相机预览
            if (hasCameraPermission && selectedImagePath == null && !recognitionInProgress) {
                CameraPreview(imageCapture = imageCapture, modifier = Modifier.fillMaxSize())
            }

            // 定义扫描状态：
            // 0 - 初始状态（未开始识别）
            // 1 - 识别中
            // 2 - 识别完成（有结果）
            val scanState = when {
                recognitionInProgress -> 1
                recognitionResult != null -> 2
                else -> 0
            }

            // 扫描框大小：横屏200dp，竖屏280dp
            val scanSize = if (isLandscape) 200.dp else 280.dp

            // 扫描状态为1（识别中）→ 显示扫描框和扫描线动画
            if (scanState == 1) {
                Box(
                    modifier = Modifier.size(scanSize).align(Alignment.Center) // 居中显示，大小为scanSize
                        .border(2.dp, PlantGreenPrimary, RoundedCornerShape(12.dp)) // 绿色边框，圆角12dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.02f) // 宽度全屏，高度为父布局的2%
                            .align(Alignment.TopCenter) // 顶部居中
                            .offset(y = scanSize * translateY) // 根据动画值偏移垂直位置
                            // 渐变背景：透明→绿色→透明，模拟扫描线效果
                            .background(Brush.verticalGradient(listOf(Color.Transparent, PlantGreenPrimary, Color.Transparent)))
                    )
                }
            }

            // 如果选中了图片（拍照/相册选择）→ 全屏半透明遮罩 + 居中显示选中的图片
            selectedImagePath?.let { path ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)), // 黑色半透明遮罩
                    contentAlignment = Alignment.Center // 内容居中
                ) {
                    Image(
                        // 异步加载本地图片文件
                        painter = rememberAsyncImagePainter(model = File(path)),
                        contentDescription = "Selected image", // 无障碍描述
                        modifier = Modifier
                            .size(scanSize) // 大小与扫描框一致
                            .clip(RoundedCornerShape(12.dp)), // 圆角裁剪
                        contentScale = ContentScale.Crop // 图片裁剪适配（保持比例，填满容器）
                    )
                }
            }

            // 底部操作区：横屏显示在右下角，竖屏显示在底部居中
            Box(
                modifier = Modifier
                    .align(if (isLandscape) Alignment.BottomEnd else Alignment.BottomCenter)
                    .padding(if (isLandscape) 24.dp else 0.dp)
                    // 竖屏：宽度全屏 + 半透明黑色背景 + 内边距32dp；横屏：宽度350dp
                    .then(
                        if (!isLandscape) Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.6f)).padding(32.dp)
                        else Modifier.width(350.dp)
                    )
            ) {
                // 根据扫描状态显示不同内容
                when {
                    // 状态0：初始状态 → 显示"拍摄照片"按钮
                    scanState == 0 -> {
                        Button(
                            onClick = {
                                // 点击按钮：拍摄照片
                                takePhoto(context, imageCapture, ContextCompat.getMainExecutor(context), { file ->
                                    // 拍照成功回调：更新选中图片路径 + 开始识别植物
                                    viewModel.setSelectedImagePath(file.absolutePath)
                                    viewModel.identifyPlant(file)
                                }, { })
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp), // 宽度全屏，高度56dp
                            shape = RoundedCornerShape(28.dp), // 圆角28dp（胶囊形状）
                            enabled = hasCameraPermission // 仅当有相机权限时按钮可用
                        ) { Text("Capture Photo", fontSize = 18.sp) }
                    }
                    // 状态1：识别中 → 显示进度条 + 提示文字
                    scanState == 1 && recognitionResult == null -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // 线性进度条，绿色
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = PlantGreenPrimary)
                            Spacer(modifier = Modifier.height(8.dp)) // 8dp间距
                            Text("Analyzing AI features...", color = Color.White, fontSize = 12.sp) // 提示文字
                        }
                    }
                    // 状态2：识别完成 → 显示结果/错误卡片
                    recognitionResult != null -> {
                        val result = recognitionResult!!
                        if (result.isSuccess) {
                            // 识别成功：获取植物信息，显示结果卡片
                            val plant = result.getOrNull()!!
                            RecognitionResultCard(plant.name, plant.family) {
                                // 点击"Details"按钮：重置识别状态 + 跳转到详情页
                                viewModel.resetRecognition()
                                onNavigateToDetail(plant.id)
                            }
                        } else {
                            // 识别失败：显示错误卡片，提示失败原因
                            ErrorCard(result.exceptionOrNull()?.message ?: "Identification failed.") {
                                // 点击"Retry"按钮：重置识别状态
                                viewModel.resetRecognition()
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 将安卓Uri（统一资源标识符，用于定位图片）转换为本地File文件
 * @param context 应用上下文
 * @param uri 图片的Uri路径（如相册中图片的Uri）
 * @return 转换后的本地File文件，失败返回null
 */
private fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        // 创建临时文件：缓存目录 + 唯一文件名（时间戳）+ jpg后缀
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        // 打开Uri的输入流，将内容复制到临时文件的输出流
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream -> inputStream.copyTo(outputStream) }
        }
        // 返回转换后的文件
        file
    } catch (e: Exception) {
        // 异常（如文件读写失败）→ 返回null
        null
    }
}

/**
 * 使用CameraX拍摄照片
 * @param context 应用上下文
 * @param imageCapture CameraX的ImageCapture实例，用于拍照
 * @param executor 执行器（此处为主线程执行器）
 * @param onCaptured 拍照成功的回调，返回拍摄的文件
 * @param onError 拍照失败的回调
 */
private fun takePhoto(context: Context, imageCapture: ImageCapture, executor: Executor, onCaptured: (File) -> Unit, onError: () -> Unit) {
    // 创建拍照后的输出文件：缓存目录 + 时间戳命名的jpg文件
    val file = File(context.cacheDir, "${System.currentTimeMillis()}.jpg")
    // 配置拍照输出选项
    val options = ImageCapture.OutputFileOptions.Builder(file).build()
    // 执行拍照
    imageCapture.takePicture(options, executor, object : ImageCapture.OnImageSavedCallback {
        // 拍照成功：调用onCaptured回调，返回文件
        override fun onImageSaved(res: ImageCapture.OutputFileResults) = onCaptured(file)
        // 拍照失败：调用onError回调
        override fun onError(exc: ImageCaptureException) = onError()
    })
}

/**
 * 相机预览组件：显示CameraX的实时相机画面
 * @param imageCapture CameraX的ImageCapture实例，用于绑定相机功能
 * @param modifier 布局修饰符，用于控制组件大小、位置等
 */
@Composable
fun CameraPreview(imageCapture: ImageCapture, modifier: Modifier = Modifier) {
    // 获取上下文和生命周期所有者（与组件生命周期绑定）
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    // 获取CameraX的相机提供者（管理相机生命周期）
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    // AndroidView：在Compose中嵌入安卓原生View（此处为PreviewView，用于相机预览）
    AndroidView(
        // 工厂方法：创建PreviewView并配置CameraX
        factory = { ctx ->
            // 创建PreviewView，设置缩放类型为"填充居中"（画面填满控件，居中显示）
            val previewView = PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
            // 相机提供者准备完成后，绑定相机功能
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                // 初始化相机预览功能
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                try {
                    // 解绑所有已绑定的相机功能（避免冲突）
                    cameraProvider.unbindAll()
                    // 绑定相机生命周期：
                    // lifecycleOwner - 与组件生命周期绑定
                    // CameraSelector.DEFAULT_BACK_CAMERA - 使用后置摄像头
                    // preview - 预览功能
                    // imageCapture - 拍照功能
                    cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                } catch (e: Exception) {
                    // 捕获异常（如相机被占用），此处为空处理
                }
            }, ContextCompat.getMainExecutor(ctx)) // 主线程执行
            previewView
        },
        modifier = modifier // 应用布局修饰符
    )
}

/**
 * 识别成功的结果卡片
 * @param plantName 植物名称
 * @param family 植物科属
 * @param onViewDetail 点击"Details"按钮的回调（跳转到详情页）
 */
@Composable
fun RecognitionResultCard(plantName: String, family: String, onViewDetail: () -> Unit) {
    // Card：Material3的卡片组件，带阴影和圆角
    Card(
        modifier = Modifier.fillMaxWidth(), // 宽度全屏
        shape = RoundedCornerShape(16.dp), // 圆角16dp
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)) // 白色半透明背景
    ) {
        // 行布局：垂直居中，内边距16dp
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // 成功图标：绿色，大小36dp
            Icon(Icons.Default.CheckCircle, null, tint = PlantGreenPrimary, modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(12.dp)) // 12dp水平间距
            // 文本列：占剩余宽度
            Column(Modifier.weight(1f)) {
                Text("Success", fontSize = 12.sp, color = Color.Gray) // 小标题：Success
                Text(plantName, fontSize = 18.sp, fontWeight = FontWeight.Bold) // 植物名称（加粗）
                Text(family, fontSize = 12.sp, color = PlantGreenPrimary) // 植物科属（绿色）
            }
            // 详情按钮：绿色背景，圆角20dp
            Button(
                onClick = onViewDetail,
                colors = ButtonDefaults.buttonColors(containerColor = PlantGreenPrimary),
                shape = RoundedCornerShape(20.dp)
            ) { Text("Details", color = Color.White) }
        }
    }
}

/**
 * 识别失败的错误卡片
 * @param message 错误提示信息
 * @param onRetry 点击"Retry"按钮的回调（重置识别状态）
 */
@Composable
fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(), // 宽度全屏
        shape = RoundedCornerShape(16.dp), // 圆角16dp
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)) // 白色半透明背景
    ) {
        // 行布局：垂直居中，内边距16dp
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // 错误图标：红色，大小36dp
            Icon(Icons.Default.Error, null, tint = Color.Red, modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(12.dp)) // 12dp水平间距
            // 文本列：占剩余宽度
            Column(Modifier.weight(1f)) {
                Text("Error", fontSize = 12.sp, color = Color.Gray) // 小标题：Error
                Text(message, fontSize = 14.sp, fontWeight = FontWeight.Bold) // 错误信息（加粗）
            }
            // 重试按钮：文本按钮，无背景
            TextButton(onClick = onRetry) { Text("Retry") }
        }
    }
}
package com.example.afinal.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.afinal.ui.theme.PlantGreenPrimary
import com.example.afinal.ui.viewmodel.PlantViewModel
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecognitionScreen(
    viewModel: PlantViewModel,
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val recognitionResult by viewModel.recognitionResult.collectAsStateWithLifecycle()
    val recognitionInProgress by viewModel.recognitionInProgress.collectAsStateWithLifecycle()
    val selectedImagePath by viewModel.selectedImagePath.collectAsStateWithLifecycle()

    val imageCapture = remember { ImageCapture.Builder().build() }

    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = uriToFile(context, it)
            if (file != null) {
                viewModel.setSelectedImagePath(file.absolutePath)
                viewModel.identifyPlant(file)
            } else {
                android.widget.Toast.makeText(context, "Image processing failed.", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val translateY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "line"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Recognition", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Album", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black.copy(alpha = 0.5f))
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(Color.Black).padding(padding)) {

            if (hasCameraPermission && selectedImagePath == null && !recognitionInProgress) {
                CameraPreview(imageCapture = imageCapture, modifier = Modifier.fillMaxSize())
            }

            val scanState = when {
                recognitionInProgress -> 1
                recognitionResult != null -> 2
                else -> 0
            }

            val scanSize = if (isLandscape) 200.dp else 280.dp

            if (scanState == 1) {
                Box(
                    modifier = Modifier.size(scanSize).align(Alignment.Center)
                        .border(2.dp, PlantGreenPrimary, RoundedCornerShape(12.dp))
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.02f)
                            .align(Alignment.TopCenter).offset(y = scanSize * translateY)
                            .background(Brush.verticalGradient(listOf(Color.Transparent, PlantGreenPrimary, Color.Transparent)))
                    )
                }
            }

            // Exactly Centered overlay
            selectedImagePath?.let { path ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = File(path)),
                        contentDescription = "Selected image",
                        modifier = Modifier
                            .size(scanSize)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(if (isLandscape) Alignment.BottomEnd else Alignment.BottomCenter)
                    .padding(if (isLandscape) 24.dp else 0.dp)
                    .then(
                        if (!isLandscape) Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.6f)).padding(32.dp)
                        else Modifier.width(350.dp)
                    )
            ) {
                when {
                    scanState == 0 -> {
                        Button(
                            onClick = {
                                takePhoto(context, imageCapture, ContextCompat.getMainExecutor(context), { file ->
                                    viewModel.setSelectedImagePath(file.absolutePath)
                                    viewModel.identifyPlant(file)
                                }, { })
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            enabled = hasCameraPermission
                        ) { Text("Capture Photo", fontSize = 18.sp) }
                    }
                    scanState == 1 && recognitionResult == null -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = PlantGreenPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Analyzing AI features...", color = Color.White, fontSize = 12.sp)
                        }
                    }
                    recognitionResult != null -> {
                        val result = recognitionResult!!
                        if (result.isSuccess) {
                            val plant = result.getOrNull()!!
                            RecognitionResultCard(plant.name, plant.family) {
                                viewModel.resetRecognition()
                                onNavigateToDetail(plant.id)
                            }
                        } else {
                            ErrorCard(result.exceptionOrNull()?.message ?: "Identification failed.") {
                                viewModel.resetRecognition()
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream -> inputStream.copyTo(outputStream) }
        }
        file
    } catch (e: Exception) { null }
}

private fun takePhoto(context: Context, imageCapture: ImageCapture, executor: Executor, onCaptured: (File) -> Unit, onError: () -> Unit) {
    val file = File(context.cacheDir, "${System.currentTimeMillis()}.jpg")
    val options = ImageCapture.OutputFileOptions.Builder(file).build()
    imageCapture.takePicture(options, executor, object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(res: ImageCapture.OutputFileResults) = onCaptured(file)
        override fun onError(exc: ImageCaptureException) = onError()
    })
}

@Composable
fun CameraPreview(imageCapture: ImageCapture, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                } catch (e: Exception) {}
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = modifier
    )
}

@Composable
fun RecognitionResultCard(plantName: String, family: String, onViewDetail: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, null, tint = PlantGreenPrimary, modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Success", fontSize = 12.sp, color = Color.Gray)
                Text(plantName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(family, fontSize = 12.sp, color = PlantGreenPrimary)
            }
            Button(
                onClick = onViewDetail,
                colors = ButtonDefaults.buttonColors(containerColor = PlantGreenPrimary),
                shape = RoundedCornerShape(20.dp)
            ) { Text("Details", color = Color.White) }
        }
    }
}

@Composable
fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Error, null, tint = Color.Red, modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Error", fontSize = 12.sp, color = Color.Gray)
                Text(message, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onRetry) { Text("Retry") }
        }
    }
}
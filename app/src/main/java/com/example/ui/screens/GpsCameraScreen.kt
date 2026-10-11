package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.viewmodel.MainViewModel
import com.example.utils.CameraHelper
import com.example.utils.GpsHelper
import com.example.utils.GpsLocationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

@Composable
fun GpsCameraScreen(
    viewModel: MainViewModel,
    mode: String, // "CHECK_IN" or "CHECK_OUT"
    onCaptured: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val currentUser by viewModel.currentUser.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission || !hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    // Camera state
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_FRONT) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // Live GPS state
    var liveGpsData by remember { mutableStateOf<GpsLocationData?>(null) }
    var isFetchingLocation by remember { mutableStateOf(true) }

    // Preview Confirmation state
    var capturedPhotoPath by remember { mutableStateOf<String?>(null) }
    var capturedGpsData by remember { mutableStateOf<GpsLocationData?>(null) }

    // Fetch initial GPS
    LaunchedEffect(Unit) {
        val loc = GpsHelper.getCurrentLocation(context)
        liveGpsData = loc
        isFetchingLocation = false
    }

    val timeFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss a", Locale.getDefault()) }
    var currentTimeString by remember { mutableStateOf(timeFormat.format(Date())) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeString = timeFormat.format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!hasCameraPermission) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera Permission Required",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "To prevent attendance fraud, photo must be captured live via camera. Gallery uploads are strictly forbidden.",
                    fontSize = 14.sp,
                    color = Color(0xFFE2E8F0),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Grant Permission")
                }
            }
        } else if (capturedPhotoPath != null && capturedGpsData != null) {
            // CONFIRMATION PREVIEW SCREEN
            val photoFile = remember(capturedPhotoPath) { File(capturedPhotoPath ?: "") }
            val bitmap = remember(photoFile) {
                if (photoFile.exists()) BitmapFactory.decodeFile(photoFile.absolutePath) else null
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A))
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Verify GPS Attendance Photo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Captured GPS Photo",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // GPS Details Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "GPS Verified Location",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                        Text(
                            text = capturedGpsData?.address ?: "Location verified",
                            fontSize = 13.sp,
                            color = Color.White,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "Lat: ${String.format(Locale.US, "%.5f", capturedGpsData?.latitude ?: 0.0)}, Lng: ${String.format(Locale.US, "%.5f", capturedGpsData?.longitude ?: 0.0)}",
                            fontSize = 11.sp,
                            color = Color(0xFFE2E8F0),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            capturedPhotoPath = null
                            capturedGpsData = null
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("retake_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retake",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retake", color = Color.White)
                    }

                    Button(
                        onClick = {
                            val path = capturedPhotoPath ?: return@Button
                            val gps = capturedGpsData ?: return@Button

                            if (mode == "CHECK_IN") {
                                viewModel.recordClockIn(path, gps) { success, _ ->
                                    if (success) onCaptured()
                                }
                            } else {
                                viewModel.recordClockOut(path, gps) { success, _ ->
                                    if (success) onCaptured()
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("confirm_attendance_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (mode == "CHECK_IN") Color(0xFF10B981) else Color(0xFFF43F5E)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Confirm",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (mode == "CHECK_IN") "Clock In" else "Clock Out",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            // LIVE CAMERA PREVIEW SCREEN
            val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()
                        imageCapture = capture

                        val selector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                selector,
                                preview,
                                capture
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                update = { previewView ->
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()
                        imageCapture = capture

                        val selector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                selector,
                                preview,
                                capture
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(previewView.context))
                }
            )

            // Top Bar Overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .background(Color(0x88000000), CircleShape)
                        .testTag("camera_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Mode Badge
                Surface(
                    color = if (mode == "CHECK_IN") Color(0xDD10B981) else Color(0xEEF43F5E),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (mode == "CHECK_IN") "📸 GPS LOGIN PHOTO" else "📸 GPS LOGOUT PHOTO",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Camera Switch (Front/Back)
                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                            CameraSelector.LENS_FACING_BACK
                        } else {
                            CameraSelector.LENS_FACING_FRONT
                        }
                    },
                    modifier = Modifier
                        .background(Color(0x88000000), CircleShape)
                        .testTag("switch_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = Color.White
                    )
                }
            }

            // Bottom Live Watermark Banner & Shutter
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Live Watermark HUD Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xBB0F172A)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${currentUser?.name ?: "Employee"} (${currentUser?.empId ?: ""})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (mode == "CHECK_IN") "CHECK IN" else "CHECK OUT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (mode == "CHECK_IN") Color(0xFF34D399) else Color(0xFFFB7185)
                            )
                        }

                        Text(
                            text = "🕒 $currentTimeString",
                            fontSize = 11.sp,
                            color = Color(0xFFE2E8F0),
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val gps = liveGpsData
                            val gpsText = if (gps != null) {
                                "Lat: ${String.format(Locale.US, "%.4f", gps.latitude)}, Lng: ${String.format(Locale.US, "%.4f", gps.longitude)} | ${gps.address}"
                            } else {
                                "Acquiring high accuracy GPS coordinates..."
                            }
                            Text(
                                text = gpsText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Shutter Button
                if (isProcessing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .border(4.dp, Color.White, CircleShape)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(if (mode == "CHECK_IN") Color(0xFF10B981) else Color(0xFFF43F5E))
                            .clickable {
                                val capture = imageCapture ?: return@clickable
                                isProcessing = true

                                capture.takePicture(
                                    cameraExecutor,
                                    object : ImageCapture.OnImageCapturedCallback() {
                                        override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                            scope.launch {
                                                val gps = liveGpsData ?: GpsHelper.getCurrentLocation(context)
                                                val user = currentUser
                                                val savedPath = CameraHelper.processAndSaveGpsPhoto(
                                                    context = context,
                                                    imageProxy = imageProxy,
                                                    employeeName = user?.name ?: "Employee",
                                                    employeeId = user?.empId ?: "EMP",
                                                    gpsData = gps,
                                                    actionType = mode
                                                )

                                                withContext(Dispatchers.Main) {
                                                    isProcessing = false
                                                    capturedPhotoPath = savedPath
                                                    capturedGpsData = gps
                                                }
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            exception.printStackTrace()
                                            isProcessing = false
                                        }
                                    }
                                )
                            }
                            .testTag("camera_shutter_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = "Capture Live Photo",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Text(
                    text = "Live In-App GPS Camera Only • Gallery Disabled",
                    fontSize = 11.sp,
                    color = Color(0xCCFFFFFF),
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}

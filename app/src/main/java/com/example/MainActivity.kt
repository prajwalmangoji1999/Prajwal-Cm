package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.EmployeeDashboardScreen
import com.example.ui.screens.GpsCameraScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class ScreenState {
    LOGIN,
    EMPLOYEE_DASHBOARD,
    ADMIN_DASHBOARD,
    GPS_CAMERA
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: MainViewModel = viewModel()
                GeoAttendApp(viewModel)
            }
        }
    }
}

@Composable
fun GeoAttendApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(ScreenState.LOGIN) }
    var cameraMode by remember { mutableStateOf("CHECK_IN") }

    val currentUser by viewModel.currentUser.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.uiMessage.collectLatest { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when (currentScreen) {
            ScreenState.LOGIN -> {
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToEmployee = {
                        currentScreen = ScreenState.EMPLOYEE_DASHBOARD
                    },
                    onNavigateToAdmin = {
                        currentScreen = ScreenState.ADMIN_DASHBOARD
                    }
                )
            }

            ScreenState.EMPLOYEE_DASHBOARD -> {
                BackHandler {
                    viewModel.logout {
                        currentScreen = ScreenState.LOGIN
                    }
                }

                EmployeeDashboardScreen(
                    viewModel = viewModel,
                    onOpenGpsCamera = { mode ->
                        cameraMode = mode
                        currentScreen = ScreenState.GPS_CAMERA
                    },
                    onLogout = {
                        currentScreen = ScreenState.LOGIN
                    }
                )
            }

            ScreenState.ADMIN_DASHBOARD -> {
                BackHandler {
                    viewModel.logout {
                        currentScreen = ScreenState.LOGIN
                    }
                }

                AdminDashboardScreen(
                    viewModel = viewModel,
                    onLogout = {
                        currentScreen = ScreenState.LOGIN
                    }
                )
            }

            ScreenState.GPS_CAMERA -> {
                BackHandler {
                    currentScreen = ScreenState.EMPLOYEE_DASHBOARD
                }

                GpsCameraScreen(
                    viewModel = viewModel,
                    mode = cameraMode,
                    onCaptured = {
                        currentScreen = ScreenState.EMPLOYEE_DASHBOARD
                    },
                    onBack = {
                        currentScreen = ScreenState.EMPLOYEE_DASHBOARD
                    }
                )
            }
        }
    }
}

package com.localkarar.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.localkarar.app.auth.AuthRepository
import com.localkarar.app.auth.AuthViewModel
import com.localkarar.app.auth.SecureStorage
import com.localkarar.app.auth.SessionState
import com.localkarar.app.network.createHttpClient
import com.localkarar.app.home.DashboardRepository
import com.localkarar.app.home.HomeViewModel
import com.localkarar.app.courses.CourseRepository
import com.localkarar.app.decision.DecisionRepository
import com.localkarar.app.ui.LoginScreen
import com.localkarar.app.ui.shell.AppShell
import com.localkarar.app.ui.theme.LocalKararTheme

@Composable
fun App(secureStorage: SecureStorage) {
    val httpClient = remember { createHttpClient(secureStorage) }
    val authRepository = remember { AuthRepository(httpClient, secureStorage) }
    val authViewModel = remember { AuthViewModel(authRepository) }
    
    val dashboardRepository = remember { DashboardRepository(httpClient, secureStorage) }
    val homeViewModel = remember { HomeViewModel(dashboardRepository) }
    val courseRepository = remember { CourseRepository(httpClient, secureStorage) }
    val decisionRepository = remember { DecisionRepository(httpClient) }
    
    LocalKararTheme {
        val sessionState by authViewModel.sessionState.collectAsState()

        MaterialTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(com.localkarar.app.ui.theme.LkSurfaceCanvas)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                when (val state = sessionState) {
                    is SessionState.CheckingSession -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is SessionState.Unauthenticated -> {
                        LoginScreen(viewModel = authViewModel)
                    }
                    is SessionState.Authenticated -> {
                        AppShell(
                            firstName = state.user.name,
                            homeViewModel = homeViewModel,
                            courseRepository = courseRepository,
                            decisionRepository = decisionRepository,
                            onLogout = { authViewModel.logout() }
                        )
                    }
                }
            }
        }
    }
}



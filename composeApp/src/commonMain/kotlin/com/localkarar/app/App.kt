package com.localkarar.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.CircularProgressIndicator
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
import com.localkarar.app.calculations.CalculationsRepository
import com.localkarar.app.network.SafeApiClient
import com.localkarar.app.network.createHttpClient
import com.localkarar.app.home.DashboardRepository
import com.localkarar.app.home.HomeViewModel
import com.localkarar.app.courses.CourseRepository
import com.localkarar.app.decision.DecisionRepository
import com.localkarar.app.workspaces.ActiveWorkspaceStore
import com.localkarar.app.workspaces.WorkspaceRepository
import com.localkarar.app.mentor.MentorRepository
import com.localkarar.app.news.NewsRepository
import com.localkarar.app.community.CommunityRepository
import com.localkarar.app.settings.SettingsRepository
import com.localkarar.app.ui.LoginScreen
import com.localkarar.app.ui.shell.AppShell
import com.localkarar.app.ui.theme.LocalKararTheme
import com.localkarar.app.ui.theme.LkSurfaceCanvas

@Composable
fun App(secureStorage: SecureStorage) {
    val httpClient = remember { createHttpClient(secureStorage) }

    val authRepository = remember { AuthRepository(httpClient, secureStorage) }
    val authViewModel = remember { AuthViewModel(authRepository) }

    val dashboardRepository = remember { DashboardRepository(httpClient, secureStorage) }
    val homeViewModel = remember { HomeViewModel(dashboardRepository) }
    val courseRepository = remember { CourseRepository(httpClient, secureStorage) }
    val decisionRepository = remember { DecisionRepository(httpClient) }

    val calculationsRepository = remember { CalculationsRepository(SafeApiClient(httpClient, "Hesaplamalar")) }
    val activeWorkspaceStore = remember { ActiveWorkspaceStore() }
    val workspaceRepository = remember { WorkspaceRepository(SafeApiClient(httpClient, "İşletme")) }
    val mentorRepository = remember { MentorRepository(httpClient) }
    val newsRepository = remember { NewsRepository(httpClient) }
    val communityRepository = remember { CommunityRepository(httpClient) }
    val settingsRepository = remember { SettingsRepository(httpClient) }

    // LocalKararTheme is the single authoritative theme provider.
    // It wraps MaterialTheme internally in Theme.kt with LkTypography, LkShapes, and LkColors.
    // Do NOT add a nested MaterialTheme() here — it resets typography/shapes to defaults.
    LocalKararTheme {
        val sessionState by authViewModel.sessionState.collectAsState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LkSurfaceCanvas)
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
                        user = state.user,
                        homeViewModel = homeViewModel,
                        courseRepository = courseRepository,
                        decisionRepository = decisionRepository,
                        calculationsRepository = calculationsRepository,
                        activeWorkspaceStore = activeWorkspaceStore,
                        workspaceRepository = workspaceRepository,
                        mentorRepository = mentorRepository,
                        newsRepository = newsRepository,
                        communityRepository = communityRepository,
                        settingsRepository = settingsRepository,
                        onNewSession = { token, user ->
                            authRepository.applyNewSession(token, user)
                        },
                        onLogout = { authViewModel.logout() }
                    )
                }
            }
        }
    }
}
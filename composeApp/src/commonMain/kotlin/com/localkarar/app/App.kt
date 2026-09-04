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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.localkarar.app.auth.AuthRepository
import com.localkarar.app.auth.AuthViewModel
import androidx.compose.foundation.isSystemInDarkTheme
import com.localkarar.app.auth.SecureStorage
import com.localkarar.app.core.AppPreferences
import com.localkarar.app.ui.theme.ThemeController
import com.localkarar.app.ui.theme.ThemeMode
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
import com.localkarar.app.workspaces.DocumentUploadRepository
import com.localkarar.app.mentor.MentorRepository
import com.localkarar.app.news.NewsRepository
import com.localkarar.app.community.CommunityRepository
import com.localkarar.app.settings.SettingsRepository
import com.localkarar.app.settings.AccountNotificationsRepository
import com.localkarar.app.ui.ForgotPasswordScreen
import com.localkarar.app.ui.LoginScreen
import com.localkarar.app.ui.RegisterScreen
import com.localkarar.app.ui.WelcomeScreen
import com.localkarar.app.ui.ResetPasswordScreen
import com.localkarar.app.ui.shell.AppShell
import com.localkarar.app.ui.theme.LocalKararTheme
import com.localkarar.app.ui.theme.LkSurfaceCanvas

private enum class AuthRoute {
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD,
    RESET_PASSWORD
}

@Composable
fun App(secureStorage: SecureStorage, appPreferences: AppPreferences) {
    var authRepoHolder: AuthRepository? = null
    val httpClient = remember {
        createHttpClient(
            secureStorage = secureStorage,
            onUserUpdated = { user -> authRepoHolder?.updateUser(user) },
            onSessionExpired = { authRepoHolder?.logout() }
        )
    }

    val authRepository = remember {
        AuthRepository(httpClient, secureStorage).also { authRepoHolder = it }
    }
    val authViewModel = viewModel(key = "auth_root") { AuthViewModel(authRepository) }

    val dashboardRepository = remember { DashboardRepository(httpClient, secureStorage) }
    val courseRepository = remember { CourseRepository(httpClient, secureStorage) }
    val decisionRepository = remember { DecisionRepository(httpClient) }
    val activeWorkspaceStore = remember { ActiveWorkspaceStore() }
    val workspaceRepository = remember { WorkspaceRepository(SafeApiClient(httpClient, "İşletme")) }
    
    val homeViewModel = viewModel(key = "home_root") { 
        HomeViewModel(
            dashboardRepository,
            workspaceRepository,
            decisionRepository,
            activeWorkspaceStore
        ) 
    }

    val calculationsRepository = remember { CalculationsRepository(SafeApiClient(httpClient, "Hesaplamalar")) }
    val mentorRepository = remember { MentorRepository(httpClient) }
    val newsRepository = remember { NewsRepository(httpClient) }
    val communityRepository = remember { CommunityRepository(httpClient) }
    val settingsRepository = remember { SettingsRepository(httpClient) }
    val documentUploadRepository = remember { DocumentUploadRepository(httpClient) }
    val accountNotificationsRepository = remember { AccountNotificationsRepository(SafeApiClient(httpClient, "Bildirimler")) }

    var authRoute by rememberSaveable { mutableStateOf(AuthRoute.LOGIN) }

    /*
     * KAYIT SONRASI KARSILAMA.
     *
     * Webde kayit /app/hosgeldin adresine yonleniyor (AuthPage.jsx:113);
     * mobilde kayit biter bitmez kullanici dogrudan bos bir Kontrol
     * Merkezi ekranina dusuyordu.
     *
     * Bayrak rememberSaveable DEGIL: ekran yalnizca kaydin hemen
     * ardindaki oturumda gorunmeli. Surec olumunden sonra geri gelmesi,
     * uygulamayi her acisinda karsilama gormek demek olurdu.
     */
    var yeniKayit by remember { mutableStateOf(false) }

    /*
     * TEMA. Webdeki `ThemeContext` ile ayni uc durum: kullanici acik ya da
     * koyu secebilir, secmediyse SISTEM tercihi gecerli.
     */
    val themeController = remember { ThemeController(appPreferences) }
    val sistemKoyu = isSystemInDarkTheme()
    val koyuMu = when (themeController.mode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> sistemKoyu
    }

    LocalKararTheme(darkTheme = koyuMu, themeController = themeController) {
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
                    when (authRoute) {
                        AuthRoute.LOGIN -> LoginScreen(
                            viewModel = authViewModel,
                            onNavigateToRegister = {
                                authViewModel.clearErrors()
                                authRoute = AuthRoute.REGISTER
                            },
                            onNavigateToForgotPassword = {
                                authViewModel.clearErrors()
                                authRoute = AuthRoute.FORGOT_PASSWORD
                            }
                        )
                        AuthRoute.REGISTER -> RegisterScreen(
                            viewModel = authViewModel,
                            onRegistered = { yeniKayit = true },
                            onNavigateToLogin = {
                                authViewModel.clearErrors()
                                authRoute = AuthRoute.LOGIN
                            }
                        )
                        AuthRoute.FORGOT_PASSWORD -> ForgotPasswordScreen(
                            viewModel = authViewModel,
                            onNavigateToResetPassword = {
                                authViewModel.clearErrors()
                                authRoute = AuthRoute.RESET_PASSWORD
                            },
                            onNavigateToLogin = {
                                authViewModel.clearErrors()
                                authRoute = AuthRoute.LOGIN
                            }
                        )
                        AuthRoute.RESET_PASSWORD -> ResetPasswordScreen(
                            viewModel = authViewModel,
                            onNavigateToLogin = {
                                authViewModel.clearErrors()
                                authRoute = AuthRoute.LOGIN
                            }
                        )
                    }
                }
                is SessionState.Authenticated -> {
                    if (yeniKayit) {
                        WelcomeScreen(
                            user = state.user,
                            onStart = { yeniKayit = false }
                        )
                    } else {
                    AppShell(
                        user = state.user,
                        homeViewModel = homeViewModel,
                        dashboardRepository = dashboardRepository,
                        courseRepository = courseRepository,
                        decisionRepository = decisionRepository,
                        calculationsRepository = calculationsRepository,
                        activeWorkspaceStore = activeWorkspaceStore,
                        workspaceRepository = workspaceRepository,
                        mentorRepository = mentorRepository,
                        newsRepository = newsRepository,
                        communityRepository = communityRepository,
                        settingsRepository = settingsRepository,
                        documentUploadRepository = documentUploadRepository,
                        accountNotificationsRepository = accountNotificationsRepository,
                        onNewSession = { token, user ->
                            authRepository.applyNewSession(token, user)
                        },
                        onLogout = { 
                            authViewModel.logout()
                            authRoute = AuthRoute.LOGIN
                        }
                    )
                    }
                }
            }
        }
    }
}
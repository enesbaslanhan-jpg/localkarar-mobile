package com.localkarar.app.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.localkarar.app.navigation.Destination
import com.localkarar.app.navigation.NavController
import com.localkarar.app.ui.screens.*
import com.localkarar.app.ui.screens.home.HomeScreen
import com.localkarar.app.ui.theme.*
import kotlinx.coroutines.launch

import com.localkarar.app.home.HomeUiState
import com.localkarar.app.home.HomeViewModel
import com.localkarar.app.courses.CourseRepository
import com.localkarar.app.decision.DecisionRepository
import com.localkarar.app.decision.DecisionToolsViewModel
import com.localkarar.app.decision.DecisionSessionViewModel
import com.localkarar.app.ui.screens.decision.DecisionToolsScreen
import com.localkarar.app.ui.screens.decision.DecisionSessionScreen
import com.localkarar.app.courses.CoursesViewModel
import com.localkarar.app.courses.CourseDetailViewModel
import com.localkarar.app.courses.LessonReaderViewModel
import com.localkarar.app.ui.screens.courses.CoursesScreen
import com.localkarar.app.ui.screens.courses.CourseDetailScreen
import com.localkarar.app.ui.screens.courses.LessonReaderScreen


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppShell(
    firstName: String?,
    homeViewModel: HomeViewModel,
    courseRepository: CourseRepository,
    decisionRepository: DecisionRepository,
    onLogout: () -> Unit
) {
    val navController = remember { NavController(Destination.Home) }
    val backStack by navController.backStack.collectAsState()
    val currentDestination = backStack.last()

    val homeUiState by homeViewModel.uiState.collectAsState()
    LaunchedEffect(homeUiState) {
        if (homeUiState is HomeUiState.Error && (homeUiState as HomeUiState.Error).isAuthError) {
            onLogout()
        }
    }

    val bottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()

    // Clean ASCII identifiers for code symbols; user-facing strings remain Turkish.
    val openMenu = {
        coroutineScope.launch { bottomSheetState.show() }
    }

    val closeMenuAndNavigate = { dest: Destination ->
        coroutineScope.launch {
            bottomSheetState.hide()
            navController.navigateTo(dest)
        }
    }

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetBackgroundColor = Color.Transparent,
        sheetElevation = 0.dp,
        sheetContent = {
            MenuBottomSheet(
                firstName = firstName,
                onNavigate = { closeMenuAndNavigate(it) },
                onLogout = onLogout
            )
        }
    ) {
        Scaffold(
            bottomBar = {
                if (currentDestination !is Destination.LessonReader) {
                    LkBottomNavigation(
                        currentDestination = currentDestination,
                        onNavigate = { navController.navigateTo(it) },
                        onMenuClick = { openMenu() }
                    )
                }
            },
            backgroundColor = LkSurfaceCanvas,
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                ScreenContent(
                    destination = currentDestination,
                    navController = navController,
                    firstName = firstName,
                    homeViewModel = homeViewModel,
                    courseRepository = courseRepository,
                    decisionRepository = decisionRepository
                )
            }
        }
    }
}

@Composable
private fun ScreenContent(
    destination: Destination,
    navController: NavController,
    firstName: String?,
    homeViewModel: HomeViewModel,
    courseRepository: CourseRepository,
    decisionRepository: DecisionRepository
) {
    val onBack = { navController.popBackStack(); Unit }

    when (destination) {
        Destination.Login -> { /* Handled at app root */ }
        Destination.Home -> HomeScreen(
            viewModel = homeViewModel,
            onNavigateToCourses = { navController.navigateTo(Destination.Courses) },
            onNavigateToCourseDetail = { courseId -> navController.navigateTo(Destination.CourseDetail(courseId)) }
        )
        Destination.Courses -> {
            val viewModel = remember { CoursesViewModel(courseRepository) }
            CoursesScreen(
                viewModel = viewModel,
                onNavigateToCourseDetail = { courseId -> navController.navigateTo(Destination.CourseDetail(courseId)) },
                onBack = onBack
            )
        }
        is Destination.CourseDetail -> {
            val viewModel = remember(destination.courseId) { CourseDetailViewModel(courseRepository, destination.courseId) }
            CourseDetailScreen(
                viewModel = viewModel,
                onNavigateToLesson = { cId, lId -> navController.navigateTo(Destination.LessonReader(cId, lId)) },
                onBack = onBack
            )
        }
        is Destination.LessonReader -> {
            val viewModel = remember(destination.courseId, destination.lessonId) {
                LessonReaderViewModel(courseRepository, destination.courseId, destination.lessonId)
            }
            LessonReaderScreen(
                viewModel = viewModel,
                onNavigateToLesson = { cId, lId -> navController.navigateTo(Destination.LessonReader(cId, lId)) },
                onBack = onBack
            )
        }
        Destination.DecisionTools -> {
            val viewModel = remember { DecisionToolsViewModel(decisionRepository) }
            DecisionToolsScreen(
                viewModel = viewModel,
                onNavigateToSession = { sessionId -> navController.navigateTo(Destination.DecisionSession(sessionId)) },
                onBack = onBack
            )
        }
        is Destination.DecisionSession -> {
            val viewModel = remember(destination.sessionId) { DecisionSessionViewModel(destination.sessionId, decisionRepository) }
            DecisionSessionScreen(
                viewModel = viewModel,
                onBack = onBack
            )
        }
        Destination.AiMentor     -> AiMentorScreen()
        Destination.Calculations -> CalculationsScreen(onBack)
        Destination.News         -> NewsScreen(onBack)
        Destination.Updates      -> UpdatesScreen(onBack)
        Destination.Saved        -> SavedScreen(onBack)
        Destination.Progress     -> ProgressScreen(onBack)
        Destination.Profile      -> ProfileScreen(onBack, firstName)
    }
}

// ──────────────────────────────────────────────────────────────────
// Bottom Navigation
// ──────────────────────────────────────────────────────────────────

private data class NavItem(
    val label: String,
    val icon: ImageVector,
    val destination: Destination?,     // null = menu trigger
)

private val NAV_ITEMS = listOf(
    NavItem("Ana Sayfa", Icons.Default.Home,           Destination.Home),
    NavItem("Kurslar",   Icons.Default.School,         Destination.Courses),
    NavItem("Karar",     Icons.Default.AccountBalance,  Destination.DecisionTools),
    NavItem("Mentor",    Icons.Default.Psychology,      Destination.AiMentor),
    NavItem("Menü",      Icons.Default.Menu,            null),  // opens drawer
)

@Composable
private fun LkBottomNavigation(
    currentDestination: Destination,
    onNavigate: (Destination) -> Unit,
    onMenuClick: () -> Unit
) {
    BottomNavigation(
        backgroundColor = LkSurfacePanel,
        contentColor    = LkTextSecondary,
        elevation       = 0.dp,
        modifier        = Modifier
            .border(width = 1.dp, color = LkLineStrong)
    ) {
        NAV_ITEMS.forEach { item ->
            val selected = item.destination != null && currentDestination == item.destination
            BottomNavigationItem(
                selected             = selected,
                onClick              = {
                    if (item.destination != null) onNavigate(item.destination)
                    else onMenuClick()
                },
                icon                 = {
                    Icon(
                        imageVector  = item.icon,
                        contentDescription = item.label,
                        modifier     = Modifier.size(22.dp),
                        tint         = if (selected) LkPrimary else LkTextSecondary
                    )
                },
                label                = {
                    Text(
                        text      = item.label,
                        style     = LkTypography.getMicro(),
                        color     = if (selected) LkPrimary else LkTextSecondary,
                        maxLines  = 1
                    )
                },
                selectedContentColor   = LkPrimary,
                unselectedContentColor = LkTextSecondary
            )
        }
    }
}

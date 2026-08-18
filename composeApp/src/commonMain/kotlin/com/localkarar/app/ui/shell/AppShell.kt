package com.localkarar.app.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    val openMenü = {
        coroutineScope.launch { bottomSheetState.show() }
    }
    
    val closeMenüAndNavigate = { dest: Destination ->
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
                onNavigate = { closeMenüAndNavigate(it) },
                onLogout = onLogout
            )
        }
    ) {
        // We use Scaffold to properly handle window insets automatically
        Scaffold(
            bottomBar = {
                if (currentDestination !is Destination.LessonReader) {
                    LkBottomNavigation(
                        currentDestination = currentDestination,
                        onNavigate = { navController.navigateTo(it) },
                        onMenüClick = { openMenü() }
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
        Destination.AiMentor -> AiMentorScreen()
        Destination.Calculations -> CalculationsScreen(onBack)
        Destination.News -> NewsScreen(onBack)
        Destination.Updates -> UpdatesScreen(onBack)
        Destination.Saved -> SavedScreen(onBack)
        Destination.Progress -> ProgressScreen(onBack)
        Destination.Profile -> ProfileScreen(onBack, firstName)
    }
}

@Composable
private fun LkBottomNavigation(
    currentDestination: Destination,
    onNavigate: (Destination) -> Unit,
    onMenüClick: () -> Unit
) {
    BottomNavigation(
        backgroundColor = LkSurfacePanel,
        contentColor = LkTextSecondary,
        elevation = 0.dp,
        modifier = Modifier.border(width = 1.dp, color = LkLineStrong)
    ) {
        BottomNavItem(
            label = "Ana Sayfa", 
            selected = currentDestination == Destination.Home,
            onClick = { onNavigate(Destination.Home) }
        )
        BottomNavItem(
            label = "Kurslar", 
            selected = currentDestination == Destination.Courses,
            onClick = { onNavigate(Destination.Courses) }
        )
        BottomNavItem(
            label = "Karar", 
            selected = currentDestination == Destination.DecisionTools,
            onClick = { onNavigate(Destination.DecisionTools) }
        )
        BottomNavItem(
            label = "Menüor", 
            selected = currentDestination == Destination.AiMentor,
            onClick = { onNavigate(Destination.AiMentor) }
        )
        BottomNavItem(
            label = "Menü", 
            selected = false,
            onClick = onMenüClick
        )
    }
}

@Composable
private fun RowScope.BottomNavItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    BottomNavigationItem(
        selected = selected,
        onClick = onClick,
        icon = { 
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        if (selected) LkPrimary.copy(alpha = 0.1f) else Color.Transparent,
                        shape = LkShapes.SM
                    )
            )
        },
        label = { 
            Text(
                text = label, 
                style = LkTypography.getMicro(),
                color = if (selected) LkPrimary else LkTextSecondary,
                maxLines = 1
            ) 
        },
        selectedContentColor = LkPrimary,
        unselectedContentColor = LkTextSecondary
    )
}






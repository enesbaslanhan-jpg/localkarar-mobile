package com.localkarar.app.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.localkarar.app.navigation.Destination
import com.localkarar.app.navigation.NavController
import com.localkarar.app.navigation.ScopedViewModelStoreOwner
import com.localkarar.app.navigation.rememberNavController
import com.localkarar.app.ui.screens.home.HomeScreen
import com.localkarar.app.ui.theme.*
import kotlinx.coroutines.launch

import com.localkarar.app.home.HomeUiState
import com.localkarar.app.home.HomeViewModel
import com.localkarar.app.home.DashboardRepository
import com.localkarar.app.courses.CourseRepository
import com.localkarar.app.decision.DecisionRepository
import com.localkarar.app.decision.DecisionToolsViewModel
import com.localkarar.app.decision.DecisionToolViewModel
import com.localkarar.app.decision.DecisionSessionViewModel
import com.localkarar.app.ui.screens.decision.DecisionToolsScreen
import com.localkarar.app.ui.screens.decision.DecisionToolScreen
import com.localkarar.app.ui.screens.decision.DecisionSessionScreen
import com.localkarar.app.navigation.deeplink.PendingDeepLinkStore
import com.localkarar.app.courses.CoursesViewModel
import com.localkarar.app.courses.CourseDetailViewModel
import com.localkarar.app.courses.LessonReaderViewModel
import com.localkarar.app.ui.screens.courses.CoursesScreen
import com.localkarar.app.ui.screens.courses.CourseDetailScreen
import com.localkarar.app.ui.screens.courses.LessonReaderScreen
import com.localkarar.app.calculations.CalculationsRepository
import com.localkarar.app.calculations.CalculationsViewModel
import com.localkarar.app.calculations.FormulaCalculatorViewModel
import com.localkarar.app.calculations.FinancialModelViewModel
import com.localkarar.app.calculations.ModelRunsViewModel
import com.localkarar.app.calculations.RunDetailViewModel
import com.localkarar.app.ui.screens.calculations.CalculationsScreen
import com.localkarar.app.ui.screens.calculations.FormulaDetailScreen
import com.localkarar.app.ui.screens.calculations.FinancialModelScreen
import com.localkarar.app.ui.screens.calculations.ModelRunsScreen
import com.localkarar.app.ui.screens.calculations.RunDetailScreen
import com.localkarar.app.workspaces.ActiveWorkspaceStore
import com.localkarar.app.workspaces.WorkspaceRepository
import com.localkarar.app.workspaces.WorkspacesViewModel
import com.localkarar.app.workspaces.WorkspaceHomeViewModel
import com.localkarar.app.workspaces.RecordsViewModel
import com.localkarar.app.workspaces.RecordDetailViewModel
import com.localkarar.app.workspaces.RecordEditViewModel
import com.localkarar.app.workspaces.CalendarViewModel
import com.localkarar.app.workspaces.DocumentsViewModel
import com.localkarar.app.workspaces.TeamViewModel
import com.localkarar.app.workspaces.ContactsViewModel
import com.localkarar.app.workspaces.NotificationsViewModel
import com.localkarar.app.workspaces.ActivityViewModel
import com.localkarar.app.workspaces.WorkspaceSettingsViewModel
import com.localkarar.app.ui.screens.workspaces.WorkspacesScreen
import com.localkarar.app.ui.screens.workspaces.WorkspaceHomeScreen
import com.localkarar.app.ui.screens.workspaces.WorkspaceSectionSheet
import com.localkarar.app.ui.screens.workspaces.OrdersScreen
import com.localkarar.app.ui.screens.workspaces.ProductsScreen
import com.localkarar.app.ui.screens.workspaces.RecordsScreen
import com.localkarar.app.ui.screens.workspaces.RecordDetailScreen
import com.localkarar.app.ui.screens.workspaces.RecordEditScreen
import com.localkarar.app.ui.screens.workspaces.CalendarScreen
import com.localkarar.app.ui.screens.workspaces.DocumentsScreen
import com.localkarar.app.ui.screens.workspaces.TeamScreen
import com.localkarar.app.ui.screens.workspaces.ContactsScreen
import com.localkarar.app.ui.screens.workspaces.NotificationsScreen
import com.localkarar.app.ui.screens.workspaces.ActivityScreen
import com.localkarar.app.ui.screens.workspaces.WorkspaceSettingsScreen
import com.localkarar.app.mentor.MentorRepository
import com.localkarar.app.mentor.MentorViewModel
import com.localkarar.app.mentor.MemoryViewModel
import com.localkarar.app.mentor.ConversationViewModel
import com.localkarar.app.ui.screens.mentor.AiMentorScreen
import com.localkarar.app.ui.screens.mentor.ConversationScreen
import com.localkarar.app.news.NewsRepository
import com.localkarar.app.news.NewsViewModel
import com.localkarar.app.ui.screens.news.NewsFeedScreen
import com.localkarar.app.ui.screens.news.NewsDetailScreen
import com.localkarar.app.community.CommunityRepository
import com.localkarar.app.community.CommunityViewModel
import com.localkarar.app.community.SocialViewModel
import com.localkarar.app.community.ThreadsViewModel
import com.localkarar.app.community.CommunityNotificationsViewModel
import com.localkarar.app.ui.screens.community.CommunityFeedScreen
import com.localkarar.app.ui.screens.community.CommunityPostDetailScreen
import com.localkarar.app.ui.screens.community.FollowersScreen
import com.localkarar.app.ui.screens.community.ThreadDetailScreen
import com.localkarar.app.ui.screens.community.ProfileScreen as CommunityProfileScreen
import com.localkarar.app.ui.screens.community.NotificationsScreen as CommunityNotificationsScreen
import com.localkarar.app.settings.SettingsRepository
import com.localkarar.app.settings.SettingsViewModel
import com.localkarar.app.ui.screens.settings.SettingsScreen
import com.localkarar.app.ui.screens.settings.ProfileScreen
import com.localkarar.app.ui.screens.settings.PasswordChangeScreen
import com.localkarar.app.ui.screens.settings.EmailChangeScreen
import com.localkarar.app.ui.screens.settings.LegalConsentsScreen
import com.localkarar.app.ui.screens.settings.DeleteAccountScreen
import com.localkarar.app.auth.UserDto

private sealed interface ShellSheetState {
    object Closed : ShellSheetState
    object ProductCenter : ShellSheetState
    data class WorkspaceSections(val workspaceId: String, val currentSectionId: String) : ShellSheetState
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppShell(
    user: UserDto,
    homeViewModel: HomeViewModel,
    dashboardRepository: DashboardRepository,
    courseRepository: CourseRepository,
    decisionRepository: DecisionRepository,
    calculationsRepository: CalculationsRepository,
    activeWorkspaceStore: ActiveWorkspaceStore,
    workspaceRepository: WorkspaceRepository,
    mentorRepository: MentorRepository,
    newsRepository: NewsRepository,
    communityRepository: CommunityRepository,
    settingsRepository: SettingsRepository,
    onNewSession: (String, UserDto) -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController(Destination.Home)
    val backStack by navController.backStack.collectAsState()
    val currentDestination = backStack.last()
    val activeWorkspaceId by activeWorkspaceStore.activeWorkspaceId.collectAsState()
    val activeWorkspaceName by activeWorkspaceStore.activeWorkspaceName.collectAsState()

    val homeUiState by homeViewModel.uiState.collectAsState()
    LaunchedEffect(homeUiState) {
        if (homeUiState is HomeUiState.Error && (homeUiState as HomeUiState.Error).isAuthError) {
            onLogout()
        }
    }

    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val coroutineScope = rememberCoroutineScope()
    var sheetState by remember { mutableStateOf<ShellSheetState>(ShellSheetState.Closed) }

    SystemBackHandler(enabled = backStack.size > 1 && !bottomSheetState.isVisible) {
        navController.popBackStack()
    }

    val openProductCenter = {
        sheetState = ShellSheetState.ProductCenter
        coroutineScope.launch { bottomSheetState.show() }
    }

    val openWorkspaceSections = { wsId: String, sectionId: String ->
        sheetState = ShellSheetState.WorkspaceSections(wsId, sectionId)
        coroutineScope.launch { bottomSheetState.show() }
    }

    val closeSheetAndNavigate = { dest: Destination ->
        coroutineScope.launch {
            bottomSheetState.hide()
            sheetState = ShellSheetState.Closed
            navController.navigateTo(dest)
        }
    }

    val closeSheet = {
        coroutineScope.launch {
            bottomSheetState.hide()
            sheetState = ShellSheetState.Closed
        }
    }

    val sessionStoreOwner = remember(user.id) { ScopedViewModelStoreOwner() }
    DisposableEffect(user.id) {
        onDispose {
            PendingDeepLinkStore.clear()
            sessionStoreOwner.clear()
            navController.clearAllStores()
        }
    }

    val pendingDeepLink by PendingDeepLinkStore.pendingFlow.collectAsState()
    LaunchedEffect(pendingDeepLink) {
        val target = PendingDeepLinkStore.consume()
        if (target != null) {
            navController.navigateTo(target.toDestination())
        }
    }

    CompositionLocalProvider(LocalViewModelStoreOwner provides sessionStoreOwner) {
        ModalBottomSheetLayout(
            sheetState = bottomSheetState,
            sheetBackgroundColor = Color.Transparent,
            sheetElevation = 0.dp,
            sheetContent = {
                when (val state = sheetState) {
                    is ShellSheetState.ProductCenter -> {
                        ProductCenterSheet(
                            activeWorkspaceId = activeWorkspaceId,
                            onNavigate = { closeSheetAndNavigate(it) },
                            onClose = { closeSheet() }
                        )
                    }
                    is ShellSheetState.WorkspaceSections -> {
                        WorkspaceSectionSheet(
                            workspaceId = state.workspaceId,
                            workspaceName = null,
                            currentSectionId = state.currentSectionId,
                            onNavigate = { closeSheetAndNavigate(it) },
                            onOpenAllWorkspaces = { closeSheetAndNavigate(Destination.Workspaces) },
                            onClose = { closeSheet() }
                        )
                    }
                    ShellSheetState.Closed -> {
                        Spacer(modifier = Modifier.height(1.dp))
                    }
                }
            }
        ) {
            Scaffold(
                bottomBar = {
                    if (currentDestination !is Destination.LessonReader) {
                        LkBottomNavigation(
                            currentDestination = currentDestination,
                            activeWorkspaceId = activeWorkspaceId,
                            onNavigate = { navController.navigateTo(it) }
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
                        user = user,
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
                        onOpenProductCenter = { openProductCenter() },
                        onOpenWorkspaceSections = { wsId, secId -> openWorkspaceSections(wsId, secId) },
                        onNewSession = onNewSession,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenContent(
    destination: Destination,
    navController: NavController,
    user: UserDto,
    homeViewModel: HomeViewModel,
    dashboardRepository: DashboardRepository,
    courseRepository: CourseRepository,
    decisionRepository: DecisionRepository,
    calculationsRepository: CalculationsRepository,
    activeWorkspaceStore: ActiveWorkspaceStore,
    workspaceRepository: WorkspaceRepository,
    mentorRepository: MentorRepository,
    newsRepository: NewsRepository,
    communityRepository: CommunityRepository,
    settingsRepository: SettingsRepository,
    onOpenProductCenter: () -> Unit,
    onOpenWorkspaceSections: (String, String) -> Unit,
    onNewSession: (String, UserDto) -> Unit,
    onLogout: () -> Unit
) {
    val onBack = { navController.popBackStack(); Unit }
    val activeWorkspaceId by activeWorkspaceStore.activeWorkspaceId.collectAsState()
    val activeWorkspaceName by activeWorkspaceStore.activeWorkspaceName.collectAsState()

    val communityViewModel = viewModel(key = "community_main") { CommunityViewModel(communityRepository) }
    val socialViewModel = viewModel(key = "community_social") { SocialViewModel(communityRepository) }
    val threadsViewModel = viewModel(key = "community_threads") { ThreadsViewModel(communityRepository) }
    val notificationsViewModel = viewModel(key = "community_notifs") { CommunityNotificationsViewModel(communityRepository) }
    val newsViewModel = viewModel(key = "news_main") { NewsViewModel(newsRepository) }
    val settingsViewModel = viewModel(key = "settings_main") { SettingsViewModel(settingsRepository) }

    val isRootScreen = when (destination) {
        Destination.Login,
        Destination.Home,
        Destination.Workspaces,
        is Destination.Community,
        Destination.Calculations,
        Destination.Settings,
        Destination.Courses,
        is Destination.DecisionTools,
        Destination.AiMentor,
        Destination.News -> true
        else -> false
    }

    val currentStoreOwner = if (isRootScreen) {
        LocalViewModelStoreOwner.current ?: navController.getStoreOwner(destination)
    } else {
        navController.getStoreOwner(destination)
    }

    CompositionLocalProvider(LocalViewModelStoreOwner provides currentStoreOwner) {
        when (destination) {
        Destination.Login -> { /* Handled at app root */ }
        Destination.Home -> HomeScreen(
            viewModel = homeViewModel,
            onNavigateToCourses = { navController.navigateTo(Destination.Courses) },
            onNavigateToCourseDetail = { courseId -> navController.navigateTo(Destination.CourseDetail(courseId)) },
            onNavigateToCalculations = { navController.navigateTo(Destination.Calculations) },
            onNavigateToMentor = { navController.navigateTo(Destination.AiMentor) },
            onNavigateToDecisions = { navController.navigateTo(Destination.DecisionTools()) },
            onNavigateToDecisionDetail = { code -> navController.navigateTo(Destination.DecisionTool(code)) },
            onNavigateToWorkspaces = { navController.navigateTo(Destination.Workspaces) },
            onNavigateToTracker = { workspaceId -> navController.navigateTo(Destination.Records(workspaceId)) },
            onNavigateToEnrollments = { navController.navigateTo(Destination.Courses) },
            onOpenProductCenter = onOpenProductCenter
        )
        Destination.Courses -> {
            val viewModel = viewModel(key = "courses_main") { CoursesViewModel(courseRepository, dashboardRepository) }
            CoursesScreen(
                viewModel = viewModel,
                onNavigateToCourseDetail = { courseId -> navController.navigateTo(Destination.CourseDetail(courseId)) },
                onBack = onBack
            )
        }
        is Destination.CourseDetail -> {
            val viewModel = viewModel(key = "course_detail:${destination.courseId}") {
                CourseDetailViewModel(courseRepository, destination.courseId)
            }
            CourseDetailScreen(
                viewModel = viewModel,
                onNavigateToLesson = { cId, lId -> navController.navigateTo(Destination.LessonReader(cId, lId)) },
                onBack = onBack
            )
        }
        is Destination.LessonReader -> {
            val viewModel = viewModel(key = "lesson_reader:${destination.courseId}:${destination.lessonId}") {
                LessonReaderViewModel(courseRepository, destination.courseId, destination.lessonId)
            }
            LessonReaderScreen(
                viewModel = viewModel,
                onNavigateToLesson = { cId, lId -> navController.navigateTo(Destination.LessonReader(cId, lId)) },
                onBack = onBack
            )
        }
        is Destination.DecisionTools -> {
            val viewModel = viewModel(key = "decision_tools") { DecisionToolsViewModel(decisionRepository) }
            LaunchedEffect(destination.initialFilter) {
                if (viewModel.uiState.value is com.localkarar.app.decision.DecisionToolsUiState.Content) {
                    viewModel.updateStatusFilter(destination.initialFilter)
                } else {
                    viewModel.updateStatusFilter(destination.initialFilter)
                }
            }
            DecisionToolsScreen(
                viewModel = viewModel,
                onNavigateToSession = { sessionId -> navController.navigateTo(Destination.DecisionSession(sessionId)) },
                onBack = onBack
            )
        }
        is Destination.DecisionTool -> {
            val viewModel = viewModel(key = "decision_tool:${destination.code}") {
                DecisionToolViewModel(destination.code, decisionRepository)
            }
            DecisionToolScreen(
                viewModel = viewModel,
                onSessionReady = { sessionId ->
                    navController.navigateTo(Destination.DecisionSession(sessionId))
                },
                onBack = onBack
            )
        }
        is Destination.DecisionSession -> {
            val viewModel = viewModel(key = "decision_session:${destination.sessionId}") {
                DecisionSessionViewModel(destination.sessionId, decisionRepository)
            }
            DecisionSessionScreen(
                viewModel = viewModel,
                onBack = onBack
            )
        }
        Destination.AiMentor -> {
            val viewModel = viewModel(key = "mentor_main") { MentorViewModel(mentorRepository) }
            val memoryViewModel = viewModel(key = "mentor_memory") { MemoryViewModel(mentorRepository) }
            AiMentorScreen(
                viewModel = viewModel,
                memoryViewModel = memoryViewModel,
                onOpenConversation = { conversationId -> navController.navigateTo(Destination.Conversation(conversationId)) },
                onBack = onBack
            )
        }
        is Destination.Conversation -> {
            val viewModel = viewModel(key = "conversation:${destination.conversationId}") {
                ConversationViewModel(mentorRepository, destination.conversationId)
            }
            ConversationScreen(
                conversationId = destination.conversationId,
                viewModel = viewModel,
                onBack = onBack
            )
        }
        Destination.Calculations -> {
            val viewModel = viewModel(key = "calculations:${activeWorkspaceId ?: "none"}") {
                CalculationsViewModel(calculationsRepository, workspaceRepository, activeWorkspaceId)
            }
            CalculationsScreen(
                viewModel = viewModel,
                onCalculationSelected = { item ->
                    if (item.supportsQuickCalculation && item.formula != null) {
                        navController.navigateTo(Destination.FormulaDetail(item.formula!!.id))
                    } else if (item.supportsDetailedAnalysis && item.definition.modelCode != null) {
                        navController.navigateTo(Destination.FinancialModelDetail(item.definition.modelCode!!))
                    }
                },
                onDetailedSelected = { item ->
                    if (item.definition.modelCode != null) {
                        navController.navigateTo(Destination.FinancialModelDetail(item.definition.modelCode!!))
                    }
                },
                onNavigateToWorkspace = {
                    val id = activeWorkspaceId
                    if (id != null) navController.navigateTo(Destination.WorkspaceHome(id))
                },
                onBack = onBack,
                navController = navController
            )
        }
        is Destination.FormulaDetail -> {
            val viewModel = viewModel(key = "formula_calculator:${destination.formulaId}") {
                FormulaCalculatorViewModel(destination.formulaId, calculationsRepository, destination.historicalCalculation)
            }
            FormulaDetailScreen(viewModel = viewModel, onBack = onBack)
        }
        is Destination.FinancialModelDetail -> {
            val viewModel = viewModel(key = "financial_model:${destination.code}:${activeWorkspaceId ?: "none"}") {
                FinancialModelViewModel(destination.code, activeWorkspaceId, calculationsRepository)
            }
            FinancialModelScreen(
                viewModel = viewModel,
                workspaceName = activeWorkspaceName ?: if (activeWorkspaceId != null) "İşletme" else null,
                onOpenWorkspace = {
                    val id = activeWorkspaceId
                    if (id != null) navController.navigateTo(Destination.WorkspaceHome(id))
                    else navController.navigateTo(Destination.Workspaces)
                },
                onBack = onBack
            )
        }
        is Destination.ModelRuns -> {
            val viewModel = viewModel(key = "model_runs:${destination.workspaceId}:${destination.modelCode ?: ""}") {
                ModelRunsViewModel(destination.workspaceId, destination.modelCode, calculationsRepository)
            }
            ModelRunsScreen(
                viewModel = viewModel,
                onRunSelected = { runId -> navController.navigateTo(Destination.RunDetail(destination.workspaceId, runId)) },
                onBack = onBack
            )
        }
        is Destination.RunDetail -> {
            val viewModel = viewModel(key = "run_detail:${destination.workspaceId}:${destination.runId}") {
                RunDetailViewModel(destination.workspaceId, destination.runId, calculationsRepository)
            }
            RunDetailScreen(viewModel = viewModel, onBack = onBack)
        }
        Destination.Workspaces -> {
            val viewModel = viewModel(key = "workspaces_main") {
                WorkspacesViewModel(workspaceRepository, activeWorkspaceStore)
            }
            WorkspacesScreen(
                viewModel = viewModel,
                activeWorkspaceId = activeWorkspaceId,
                onOpenWorkspace = { workspaceId -> navController.navigateTo(Destination.WorkspaceHome(workspaceId)) },
                onBack = onBack
            )
        }
        is Destination.WorkspaceHome -> {
            val viewModel = viewModel(key = "workspace_home:${destination.workspaceId}") {
                WorkspaceHomeViewModel(destination.workspaceId, workspaceRepository)
            }
            WorkspaceHomeScreen(
                viewModel = viewModel,
                onOpenRecords = { navController.navigateTo(Destination.Records(destination.workspaceId)) },
                onOpenOrders = { navController.navigateTo(Destination.Orders(destination.workspaceId)) },
                onOpenProducts = { navController.navigateTo(Destination.Products(destination.workspaceId)) },
                onOpenCalendar = { navController.navigateTo(Destination.Calendar(destination.workspaceId)) },
                onOpenDocuments = { navController.navigateTo(Destination.Documents(destination.workspaceId)) },
                onOpenTeam = { navController.navigateTo(Destination.Team(destination.workspaceId)) },
                onOpenContacts = { navController.navigateTo(Destination.Contacts(destination.workspaceId)) },
                onOpenNotifications = { navController.navigateTo(Destination.Notifications(destination.workspaceId)) },
                onOpenActivity = { navController.navigateTo(Destination.Activity(destination.workspaceId)) },
                onOpenSettings = { navController.navigateTo(Destination.WorkspaceSettings(destination.workspaceId)) },
                onOpenRecord = { recordId -> navController.navigateTo(Destination.RecordDetail(destination.workspaceId, recordId)) },
                onAddRecord = { navController.navigateTo(Destination.RecordEdit(destination.workspaceId, null)) },
                onOpenSectionSelector = { onOpenWorkspaceSections(destination.workspaceId, "overview") },
                onBack = onBack
            )
        }
        is Destination.Orders -> {
            val viewModel = viewModel(key = "orders:${destination.workspaceId}") {
                com.localkarar.app.workspaces.OrdersViewModel(workspaceRepository)
            }
            OrdersScreen(
                workspaceId = destination.workspaceId,
                viewModel = viewModel,
                onNavigateBack = onBack
            )
        }
        is Destination.Products -> {
            val viewModel = viewModel(key = "products:${destination.workspaceId}") {
                com.localkarar.app.workspaces.ProductsViewModel(workspaceRepository)
            }
            ProductsScreen(
                workspaceId = destination.workspaceId,
                viewModel = viewModel,
                onNavigateBack = onBack
            )
        }
        is Destination.Records -> {
            val viewModel = viewModel(key = "records:${destination.workspaceId}") {
                RecordsViewModel(destination.workspaceId, workspaceRepository)
            }
            RecordsScreen(
                viewModel = viewModel,
                onOpenRecord = { recordId -> navController.navigateTo(Destination.RecordDetail(destination.workspaceId, recordId)) },
                onAddRecord = { navController.navigateTo(Destination.RecordEdit(destination.workspaceId, null)) },
                onBack = onBack
            )
        }
        is Destination.RecordDetail -> {
            val viewModel = viewModel(key = "record_detail:${destination.workspaceId}:${destination.recordId}") {
                RecordDetailViewModel(destination.workspaceId, destination.recordId, workspaceRepository)
            }
            RecordDetailScreen(
                viewModel = viewModel,
                onEdit = { navController.navigateTo(Destination.RecordEdit(destination.workspaceId, destination.recordId)) },
                onBack = onBack
            )
        }
        is Destination.RecordEdit -> {
            val viewModel = viewModel(key = "record_edit:${destination.workspaceId}:${destination.recordId ?: "new"}") {
                RecordEditViewModel(destination.workspaceId, destination.recordId, workspaceRepository)
            }
            RecordEditScreen(
                viewModel = viewModel,
                isEdit = destination.recordId != null,
                onSaved = { navController.popBackStack() },
                onBack = onBack
            )
        }
        is Destination.Calendar -> {
            val viewModel = viewModel(key = "calendar:${destination.workspaceId}") {
                CalendarViewModel(destination.workspaceId, workspaceRepository)
            }
            CalendarScreen(
                viewModel = viewModel,
                onOpenRecord = { recordId -> navController.navigateTo(Destination.RecordDetail(destination.workspaceId, recordId)) },
                onBack = onBack
            )
        }
        is Destination.Documents -> {
            val viewModel = viewModel(key = "documents:${destination.workspaceId}") {
                DocumentsViewModel(destination.workspaceId, workspaceRepository)
            }
            DocumentsScreen(viewModel = viewModel, onBack = onBack)
        }
        is Destination.Team -> {
            val viewModel = viewModel(key = "team:${destination.workspaceId}") {
                TeamViewModel(destination.workspaceId, workspaceRepository)
            }
            TeamScreen(viewModel = viewModel, onBack = onBack)
        }
        is Destination.Contacts -> {
            val viewModel = viewModel(key = "contacts:${destination.workspaceId}") {
                ContactsViewModel(destination.workspaceId, workspaceRepository)
            }
            ContactsScreen(viewModel = viewModel, onBack = onBack)
        }
        is Destination.Notifications -> {
            val viewModel = viewModel(key = "notifications:${destination.workspaceId}") {
                NotificationsViewModel(destination.workspaceId, workspaceRepository)
            }
            NotificationsScreen(viewModel = viewModel, onBack = onBack)
        }
        is Destination.Activity -> {
            val viewModel = viewModel(key = "activity:${destination.workspaceId}") {
                ActivityViewModel(destination.workspaceId, workspaceRepository)
            }
            ActivityScreen(viewModel = viewModel, onBack = onBack)
        }
        is Destination.WorkspaceSettings -> {
            val viewModel = viewModel(key = "workspace_settings:${destination.workspaceId}") {
                WorkspaceSettingsViewModel(destination.workspaceId, workspaceRepository)
            }
            WorkspaceSettingsScreen(viewModel = viewModel, onBack = onBack)
        }
        Destination.News -> {
            NewsFeedScreen(
                viewModel = newsViewModel,
                onOpenArticle = { articleId -> navController.navigateTo(Destination.NewsDetail(articleId)) },
                onBack = onBack
            )
        }
        is Destination.NewsDetail -> {
            NewsDetailScreen(
                articleId = destination.articleId,
                viewModel = newsViewModel,
                onBack = onBack
            )
        }
        is Destination.Community -> {
            CommunityFeedScreen(
                communityViewModel = communityViewModel,
                socialViewModel = socialViewModel,
                threadsViewModel = threadsViewModel,
                notificationsViewModel = notificationsViewModel,
                currentUserId = user.id,
                initialTab = destination.initialTab,
                onOpenPost = { postId -> navController.navigateTo(Destination.CommunityPost(postId)) },
                onOpenProfile = { userId -> navController.navigateTo(Destination.CommunityProfile(userId)) },
                onOpenThread = { threadId -> navController.navigateTo(Destination.CommunityThreadDetail(threadId)) },
                onOpenNotifications = { navController.navigateTo(Destination.CommunityNotifications) },
                onOpenFollowers = { userId, mode -> navController.navigateTo(Destination.CommunityFollowers(userId, mode)) },
                onOpenProductCenter = onOpenProductCenter
            )
        }
        is Destination.CommunityPost -> {
            CommunityPostDetailScreen(
                postId = destination.postId,
                viewModel = communityViewModel,
                currentUserId = user.id,
                onBack = onBack,
                onOpenProfile = { userId -> navController.navigateTo(Destination.CommunityProfile(userId)) },
                onOpenPost = { postId -> navController.navigateTo(Destination.CommunityPost(postId)) }
            )
        }
        is Destination.CommunityProfile -> {
            CommunityProfileScreen(
                userId = destination.userId,
                socialViewModel = socialViewModel,
                communityViewModel = communityViewModel,
                onBack = onBack,
                onOpenFollowers = { userId, mode -> navController.navigateTo(Destination.CommunityFollowers(userId, mode)) },
                onOpenPost = { postId -> navController.navigateTo(Destination.CommunityPost(postId)) },
                onOpenProfile = { userId -> navController.navigateTo(Destination.CommunityProfile(userId)) }
            )
        }
        is Destination.CommunityFollowers -> {
            FollowersScreen(
                userId = destination.userId,
                mode = destination.mode,
                viewModel = socialViewModel,
                onBack = onBack,
                onOpenProfile = { userId -> navController.navigateTo(Destination.CommunityProfile(userId)) }
            )
        }
        is Destination.CommunityThreadDetail -> {
            ThreadDetailScreen(
                threadId = destination.threadId,
                viewModel = threadsViewModel,
                currentUserId = user.id,
                onBack = onBack
            )
        }
        Destination.CommunityNotifications -> {
            CommunityNotificationsScreen(
                viewModel = notificationsViewModel,
                onBack = onBack,
                onOpenPost = { postId -> navController.navigateTo(Destination.CommunityPost(postId)) },
                onOpenProfile = { userId -> navController.navigateTo(Destination.CommunityProfile(userId)) },
                onOpenThread = { threadId -> navController.navigateTo(Destination.CommunityThreadDetail(threadId)) }
            )
        }
        Destination.Settings -> {
            SettingsScreen(
                userName = user.name,
                userEmail = user.email,
                userRole = user.role,
                userAvatarUrl = user.avatarUrl,
                activeWorkspaceId = activeWorkspaceId,
                viewModel = settingsViewModel,
                onOpenProfile = { navController.navigateTo(Destination.Profile) },
                onOpenWorkspaces = { navController.navigateTo(Destination.Workspaces) },
                onOpenWorkspaceSettings = { wsId -> navController.navigateTo(Destination.WorkspaceSettings(wsId)) },
                onOpenPassword = { navController.navigateTo(Destination.PasswordChange) },
                onOpenEmail = { navController.navigateTo(Destination.EmailChange) },
                onOpenConsents = { navController.navigateTo(Destination.LegalConsents) },
                onOpenDeleteAccount = { navController.navigateTo(Destination.DeleteAccount) },
                onLogoutAll = { settingsViewModel.logoutAll { t, u -> onNewSession(t, u) } },
                onLogout = onLogout
            )
        }
        Destination.Profile -> {
            ProfileScreen(
                viewModel = settingsViewModel,
                user = user,
                onNewSession = onNewSession,
                onBack = onBack
            )
        }
        Destination.PasswordChange -> {
            PasswordChangeScreen(
                viewModel = settingsViewModel,
                onNewSession = onNewSession,
                onBack = onBack
            )
        }
        Destination.EmailChange -> {
            EmailChangeScreen(
                viewModel = settingsViewModel,
                onNewSession = onNewSession,
                onBack = onBack
            )
        }
        Destination.LegalConsents -> {
            LegalConsentsScreen(
                viewModel = settingsViewModel,
                onBack = onBack
            )
        }
        Destination.DeleteAccount -> {
            DeleteAccountScreen(
                viewModel = settingsViewModel,
                onDeleted = onLogout,
                onBack = onBack
            )
        }
    }
    }
}

// ──────────────────────────────────────────────────────────────────
// LOCKED Primary Bottom Navigation (V1)
// 1. Ana Sayfa
// 2. İşletme Takibi
// 3. Topluluk
// 4. Hesaplamalar
// 5. Ayarlar
// ──────────────────────────────────────────────────────────────────

private data class NavItem(
    val label: String,
    val icon: ImageVector,
    val getTargetDestination: (activeWorkspaceId: String?) -> Destination
)

private val PRIMARY_NAV_ITEMS = listOf(
    NavItem("Ana Sayfa", Icons.Default.Home) { Destination.Home },
    NavItem("İşletme Takibi", Icons.Default.Business) { activeId ->
        if (activeId != null) Destination.WorkspaceHome(activeId) else Destination.Workspaces
    },
    NavItem("Topluluk", Icons.Default.Groups) { Destination.Community() },
    NavItem("Hesaplamalar", Icons.Default.Calculate) { Destination.Calculations },
    NavItem("Ayarlar", Icons.Default.Settings) { Destination.Settings }
)

@Composable
private fun LkBottomNavigation(
    currentDestination: Destination,
    activeWorkspaceId: String?,
    onNavigate: (Destination) -> Unit
) {
    BottomNavigation(
        backgroundColor = LkSurfacePanel,
        contentColor = LkTextSecondary,
        elevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = LkLineStrong)
    ) {
        PRIMARY_NAV_ITEMS.forEach { item ->
            val target = item.getTargetDestination(activeWorkspaceId)
            val selected = isTabSelected(currentDestination, target)

            BottomNavigationItem(
                selected = selected,
                onClick = {
                    onNavigate(target)
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp),
                        tint = if (selected) LkPrimary else LkTextSecondary
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = LkTypography.getMicro(),
                        color = if (selected) LkPrimary else LkTextSecondary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                selectedContentColor = LkPrimary,
                unselectedContentColor = LkTextSecondary
            )
        }
    }
}

private fun isTabSelected(current: Destination, tabTarget: Destination): Boolean {
    return when (tabTarget) {
        is Destination.Home -> current is Destination.Home
        is Destination.WorkspaceHome, Destination.Workspaces -> {
            current is Destination.Workspaces ||
            current is Destination.WorkspaceHome ||
            current is Destination.Records ||
            current is Destination.RecordDetail ||
            current is Destination.RecordEdit ||
            current is Destination.Orders ||
            current is Destination.Products ||
            current is Destination.Documents ||
            current is Destination.Calendar ||
            current is Destination.Team ||
            current is Destination.Contacts ||
            current is Destination.Notifications ||
            current is Destination.Activity ||
            current is Destination.WorkspaceSettings
        }
        is Destination.Community -> {
            current is Destination.Community || current is Destination.CommunityPost
        }
        is Destination.Calculations -> {
            current is Destination.Calculations ||
            current is Destination.FormulaDetail ||
            current is Destination.FinancialModelDetail ||
            current is Destination.ModelRuns ||
            current is Destination.RunDetail
        }
        is Destination.Settings -> {
            current is Destination.Settings ||
            current is Destination.Profile ||
            current is Destination.PasswordChange ||
            current is Destination.EmailChange ||
            current is Destination.DeleteAccount
        }
        else -> false
    }
}

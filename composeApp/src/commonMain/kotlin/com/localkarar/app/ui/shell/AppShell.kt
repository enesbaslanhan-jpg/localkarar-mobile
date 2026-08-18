package com.localkarar.app.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.localkarar.app.navigation.Destination
import com.localkarar.app.navigation.NavController
import com.localkarar.app.ui.screens.home.HomeScreen
import com.localkarar.app.ui.theme.*
import kotlinx.coroutines.launch

import com.localkarar.app.home.HomeUiState
import com.localkarar.app.home.HomeViewModel
import com.localkarar.app.courses.CourseRepository
import com.localkarar.app.decision.DecisionRepository
import com.localkarar.app.decision.DecisionToolsViewModel
import com.localkarar.app.decision.DecisionSessionViewModel
import com.localkarar.app.decision.DecisionHistoryViewModel
import com.localkarar.app.ui.screens.decision.DecisionToolsScreen
import com.localkarar.app.ui.screens.decision.DecisionSessionScreen
import com.localkarar.app.ui.screens.decision.DecisionHistoryScreen
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
import com.localkarar.app.ui.screens.community.CommunityFeedScreen
import com.localkarar.app.ui.screens.community.CommunityPostDetailScreen
import com.localkarar.app.settings.SettingsRepository
import com.localkarar.app.settings.SettingsViewModel
import com.localkarar.app.ui.screens.settings.SettingsScreen
import com.localkarar.app.ui.screens.settings.ProfileScreen
import com.localkarar.app.ui.screens.settings.PasswordChangeScreen
import com.localkarar.app.ui.screens.settings.EmailChangeScreen
import com.localkarar.app.ui.screens.settings.DeleteAccountScreen
import com.localkarar.app.auth.UserDto

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppShell(
    user: UserDto,
    homeViewModel: HomeViewModel,
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
                firstName = user.name,
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
                    user = user,
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
                    onNewSession = onNewSession
                )
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
    courseRepository: CourseRepository,
    decisionRepository: DecisionRepository,
    calculationsRepository: CalculationsRepository,
    activeWorkspaceStore: ActiveWorkspaceStore,
    workspaceRepository: WorkspaceRepository,
    mentorRepository: MentorRepository,
    newsRepository: NewsRepository,
    communityRepository: CommunityRepository,
    settingsRepository: SettingsRepository,
    onNewSession: (String, UserDto) -> Unit
) {
    val onBack = { navController.popBackStack(); Unit }
    val activeWorkspaceId by activeWorkspaceStore.activeWorkspaceId.collectAsState()

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
        Destination.DecisionHistory -> {
            val viewModel = remember { DecisionHistoryViewModel(decisionRepository) }
            DecisionHistoryScreen(
                viewModel = viewModel,
                onOpenSession = { sessionId -> navController.navigateTo(Destination.DecisionSession(sessionId)) }
            )
        }
        Destination.AiMentor -> {
            val viewModel = remember { MentorViewModel(mentorRepository) }
            val memoryViewModel = remember { MemoryViewModel(mentorRepository) }
            AiMentorScreen(
                viewModel = viewModel,
                memoryViewModel = memoryViewModel,
                onOpenConversation = { conversationId -> navController.navigateTo(Destination.Conversation(conversationId)) }
            )
        }
        is Destination.Conversation -> {
            val viewModel = remember(destination.conversationId) {
                ConversationViewModel(mentorRepository, destination.conversationId)
            }
            ConversationScreen(
                conversationId = destination.conversationId,
                viewModel = viewModel
            )
        }
        Destination.Calculations -> {
            val viewModel = remember { CalculationsViewModel(calculationsRepository) }
            CalculationsScreen(
                viewModel = viewModel,
                onFormulaSelected = { formula -> navController.navigateTo(Destination.FormulaDetail(formula)) },
                onModelSelected = { code -> navController.navigateTo(Destination.FinancialModelDetail(code)) },
                onRunsSelected = {
                    val id = activeWorkspaceId
                    if (id != null) navController.navigateTo(Destination.ModelRuns(id))
                },
                onBack = onBack
            )
        }
        is Destination.FormulaDetail -> {
            val viewModel = remember(destination.formula.id) {
                FormulaCalculatorViewModel(destination.formula, calculationsRepository)
            }
            FormulaDetailScreen(viewModel = viewModel, onBack = onBack)
        }
        is Destination.FinancialModelDetail -> {
            val viewModel = remember(destination.code) {
                FinancialModelViewModel(destination.code, activeWorkspaceId, calculationsRepository)
            }
            FinancialModelScreen(
                viewModel = viewModel,
                workspaceName = null,
                onOpenWorkspace = {
                    val id = activeWorkspaceId
                    if (id != null) navController.navigateTo(Destination.WorkspaceHome(id))
                },
                onBack = onBack
            )
        }
        is Destination.ModelRuns -> {
            val viewModel = remember(destination.workspaceId, destination.modelCode) {
                ModelRunsViewModel(destination.workspaceId, destination.modelCode, calculationsRepository)
            }
            ModelRunsScreen(
                viewModel = viewModel,
                onRunSelected = { runId -> navController.navigateTo(Destination.RunDetail(destination.workspaceId, runId)) },
                onBack = onBack
            )
        }
        is Destination.RunDetail -> {
            val viewModel = remember(destination.workspaceId, destination.runId) {
                RunDetailViewModel(destination.workspaceId, destination.runId, calculationsRepository)
            }
            RunDetailScreen(viewModel = viewModel, onBack = onBack)
        }
        Destination.Workspaces -> {
            val viewModel = remember { WorkspacesViewModel(workspaceRepository, activeWorkspaceStore) }
            WorkspacesScreen(
                viewModel = viewModel,
                activeWorkspaceId = activeWorkspaceId,
                onOpenWorkspace = { workspaceId -> navController.navigateTo(Destination.WorkspaceHome(workspaceId)) },
                onBack = onBack
            )
        }
        is Destination.WorkspaceHome -> {
            val viewModel = remember(destination.workspaceId) {
                WorkspaceHomeViewModel(destination.workspaceId, workspaceRepository)
            }
            WorkspaceHomeScreen(
                viewModel = viewModel,
                onOpenRecords = { navController.navigateTo(Destination.Records(destination.workspaceId)) },
                onOpenCalendar = { navController.navigateTo(Destination.Calendar(destination.workspaceId)) },
                onOpenDocuments = { navController.navigateTo(Destination.Documents(destination.workspaceId)) },
                onOpenTeam = { navController.navigateTo(Destination.Team(destination.workspaceId)) },
                onOpenContacts = { navController.navigateTo(Destination.Contacts(destination.workspaceId)) },
                onOpenNotifications = { navController.navigateTo(Destination.Notifications(destination.workspaceId)) },
                onOpenActivity = { navController.navigateTo(Destination.Activity(destination.workspaceId)) },
                onOpenSettings = { navController.navigateTo(Destination.WorkspaceSettings(destination.workspaceId)) },
                onOpenRecord = { recordId -> navController.navigateTo(Destination.RecordDetail(destination.workspaceId, recordId)) },
                onAddRecord = { navController.navigateTo(Destination.RecordEdit(destination.workspaceId, null)) },
                onBack = onBack
            )
        }
        is Destination.Records -> {
            val viewModel = remember(destination.workspaceId) {
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
            val viewModel = remember(destination.workspaceId, destination.recordId) {
                RecordDetailViewModel(destination.workspaceId, destination.recordId, workspaceRepository)
            }
            RecordDetailScreen(
                viewModel = viewModel,
                onEdit = { navController.navigateTo(Destination.RecordEdit(destination.workspaceId, destination.recordId)) },
                onBack = onBack
            )
        }
        is Destination.RecordEdit -> {
            val viewModel = remember(destination.workspaceId, destination.recordId) {
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
            val viewModel = remember(destination.workspaceId) {
                CalendarViewModel(destination.workspaceId, workspaceRepository)
            }
            CalendarScreen(
                viewModel = viewModel,
                onOpenRecord = { recordId -> navController.navigateTo(Destination.RecordDetail(destination.workspaceId, recordId)) },
                onBack = onBack
            )
        }
        is Destination.Documents -> {
            val viewModel = remember(destination.workspaceId) {
                DocumentsViewModel(destination.workspaceId, workspaceRepository)
            }
            DocumentsScreen(viewModel = viewModel, onBack = onBack)
        }
        is Destination.Team -> {
            val viewModel = remember(destination.workspaceId) {
                TeamViewModel(destination.workspaceId, workspaceRepository)
            }
            TeamScreen(viewModel = viewModel, onBack = onBack)
        }
        is Destination.Contacts -> {
            val viewModel = remember(destination.workspaceId) {
                ContactsViewModel(destination.workspaceId, workspaceRepository)
            }
            ContactsScreen(viewModel = viewModel, onBack = onBack)
        }
        is Destination.Notifications -> {
            val viewModel = remember(destination.workspaceId) {
                NotificationsViewModel(destination.workspaceId, workspaceRepository)
            }
            NotificationsScreen(viewModel = viewModel, onBack = onBack)
        }
        is Destination.Activity -> {
            val viewModel = remember(destination.workspaceId) {
                ActivityViewModel(destination.workspaceId, workspaceRepository)
            }
            ActivityScreen(viewModel = viewModel, onBack = onBack)
        }
        is Destination.WorkspaceSettings -> {
            val viewModel = remember(destination.workspaceId) {
                WorkspaceSettingsViewModel(destination.workspaceId, workspaceRepository)
            }
            WorkspaceSettingsScreen(viewModel = viewModel, onBack = onBack)
        }
        Destination.News -> {
            val viewModel = remember { NewsViewModel(newsRepository) }
            NewsFeedScreen(
                viewModel = viewModel,
                onOpenArticle = { articleId -> navController.navigateTo(Destination.NewsDetail(articleId)) }
            )
        }
        is Destination.NewsDetail -> {
            val viewModel = remember { NewsViewModel(newsRepository) }
            NewsDetailScreen(articleId = destination.articleId, viewModel = viewModel)
        }
        Destination.Community -> {
            val viewModel = remember { CommunityViewModel(communityRepository) }
            CommunityFeedScreen(
                viewModel = viewModel,
                onOpenPost = { postId -> navController.navigateTo(Destination.CommunityPost(postId)) }
            )
        }
        is Destination.CommunityPost -> {
            val viewModel = remember { CommunityViewModel(communityRepository) }
            CommunityPostDetailScreen(postId = destination.postId, viewModel = viewModel)
        }
        Destination.Settings -> {
            val viewModel = remember { SettingsViewModel(settingsRepository) }
            SettingsScreen(
                userName = user.name,
                userEmail = user.email,
                onOpenProfile = { navController.navigateTo(Destination.Profile) },
                onOpenPassword = { navController.navigateTo(Destination.PasswordChange) },
                onOpenEmail = { navController.navigateTo(Destination.EmailChange) },
                onOpenDeleteAccount = { navController.navigateTo(Destination.DeleteAccount) },
                onLogout = onLogout
            )
        }
        Destination.Profile -> {
            val viewModel = remember { SettingsViewModel(settingsRepository) }
            ProfileScreen(
                viewModel = viewModel,
                user = user,
                onNewSession = onNewSession
            )
        }
        Destination.PasswordChange -> {
            val viewModel = remember { SettingsViewModel(settingsRepository) }
            PasswordChangeScreen(viewModel = viewModel)
        }
        Destination.EmailChange -> {
            val viewModel = remember { SettingsViewModel(settingsRepository) }
            EmailChangeScreen(viewModel = viewModel, onNewSession = onNewSession)
        }
        Destination.DeleteAccount -> {
            val viewModel = remember { SettingsViewModel(settingsRepository) }
            DeleteAccountScreen(viewModel = viewModel, onDeleted = onLogout)
        }
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
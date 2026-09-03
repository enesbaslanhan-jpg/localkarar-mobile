package com.localkarar.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object DestinationCodec {
    fun encode(destination: Destination): String {
        return when (destination) {
            Destination.Login -> "login"
            Destination.Home -> "home"
            Destination.Courses -> "courses"
            is Destination.CourseDetail -> "course_detail:${destination.courseId}"
            is Destination.LessonReader -> "lesson_reader:${destination.courseId}:${destination.lessonId}"
            is Destination.DecisionTools -> "decision_tools:${destination.initialFilter}"
            is Destination.DecisionTool -> "decision_tool:${destination.code}"
            is Destination.DecisionSession -> "decision_session:${destination.sessionId}"
            Destination.AiMentor -> "ai_mentor"
            is Destination.Conversation -> "conversation:${destination.conversationId}"
            Destination.News -> "news"
            is Destination.NewsDetail -> "news_detail:${destination.articleId}"
            Destination.Calculations -> "calculations"
            is Destination.FormulaDetail -> "formula_detail:${destination.formulaId}"
            is Destination.FinancialModelDetail -> "financial_model:${destination.code}"
            is Destination.ModelRuns -> "model_runs:${destination.workspaceId}:${destination.modelCode ?: ""}"
            is Destination.RunDetail -> "run_detail:${destination.workspaceId}:${destination.runId}"
            Destination.Workspaces -> "workspaces"
            is Destination.WorkspaceHome -> "workspace_home:${destination.workspaceId}"
            is Destination.Records -> "records:${destination.workspaceId}"
            is Destination.RecordDetail -> "record_detail:${destination.workspaceId}:${destination.recordId}"
            is Destination.RecordEdit -> "record_edit:${destination.workspaceId}:${destination.recordId ?: ""}"
            is Destination.Orders -> "orders:${destination.workspaceId}"
            is Destination.Products -> "products:${destination.workspaceId}"
            is Destination.Documents -> "documents:${destination.workspaceId}"
            is Destination.Notifications -> "notifications:${destination.workspaceId}"
            is Destination.Calendar -> "calendar:${destination.workspaceId}"
            is Destination.Team -> "team:${destination.workspaceId}"
            is Destination.Contacts -> "contacts:${destination.workspaceId}"
            is Destination.Activity -> "activity:${destination.workspaceId}"
            is Destination.WorkspaceSettings -> "workspace_settings:${destination.workspaceId}"
            is Destination.WorkspaceIntegrations -> "workspace_integrations:${destination.workspaceId}"
            is Destination.Community -> if (destination.initialTab == "feed") "community" else "community:${destination.initialTab}"
            is Destination.CommunityPost -> "community_post:${destination.postId}"
            is Destination.CommunityProfile -> "community_profile:${destination.userId}"
            is Destination.CommunityFollowers -> "community_followers:${destination.userId}:${destination.mode}"
            is Destination.CommunityThreadDetail -> "community_thread:${destination.threadId}"
            Destination.CommunityNotifications -> "community_notifications"
            Destination.Settings -> "settings"
            Destination.Profile -> "profile"
            Destination.PasswordChange -> "password_change"
            Destination.EmailChange -> "email_change"
            Destination.LegalConsents -> "legal_consents"
            Destination.DeleteAccount -> "delete_account"
            Destination.Support -> "support"
            Destination.About -> "about"
            Destination.Guide -> "guide"
            Destination.AccountNotifications -> "account_notifications"
        }
    }

    fun decode(encoded: String): Destination {
        val parts = encoded.split(":")
        return try {
            when (parts[0]) {
                "login" -> Destination.Login
                "home" -> Destination.Home
                "courses" -> Destination.Courses
                "course_detail" -> Destination.CourseDetail(parts[1].toInt())
                "lesson_reader" -> Destination.LessonReader(parts[1].toInt(), parts[2].toInt())
                "decision_tools" -> Destination.DecisionTools(parts.getOrElse(1) { "all" })
                "decision_tool" -> Destination.DecisionTool(parts[1])
                "decision_session" -> Destination.DecisionSession(parts[1])
                "ai_mentor" -> Destination.AiMentor
                "conversation" -> Destination.Conversation(parts[1].toInt())
                "news" -> Destination.News
                "news_detail" -> Destination.NewsDetail(parts[1])
                "calculations" -> Destination.Calculations
                "formula_detail" -> Destination.FormulaDetail(parts[1])
                "financial_model" -> Destination.FinancialModelDetail(parts[1])
                "model_runs" -> Destination.ModelRuns(parts[1], parts.getOrNull(2)?.ifBlank { null })
                "run_detail" -> Destination.RunDetail(parts[1], parts[2])
                "workspaces" -> Destination.Workspaces
                "workspace_home" -> Destination.WorkspaceHome(parts[1])
                "records" -> Destination.Records(parts[1])
                "record_detail" -> Destination.RecordDetail(parts[1], parts[2])
                "record_edit" -> Destination.RecordEdit(parts[1], parts.getOrNull(2)?.ifBlank { null })
                "orders" -> Destination.Orders(parts[1])
                "products" -> Destination.Products(parts[1])
                "documents" -> Destination.Documents(parts[1])
                "notifications" -> Destination.Notifications(parts[1])
                "calendar" -> Destination.Calendar(parts[1])
                "team" -> Destination.Team(parts[1])
                "contacts" -> Destination.Contacts(parts[1])
                "activity" -> Destination.Activity(parts[1])
                "workspace_settings" -> Destination.WorkspaceSettings(parts[1])
                "workspace_integrations" -> Destination.WorkspaceIntegrations(parts[1])
                "community" -> if (parts.size > 1 && parts[1].isNotBlank()) Destination.Community(parts[1]) else Destination.Community("feed")
                "community_post" -> Destination.CommunityPost(parts[1])
                "community_profile" -> Destination.CommunityProfile(parts[1].toInt())
                "community_followers" -> Destination.CommunityFollowers(parts[1].toInt(), parts[2])
                "community_thread" -> Destination.CommunityThreadDetail(parts[1])
                "community_notifications" -> Destination.CommunityNotifications
                "settings" -> Destination.Settings
                "profile" -> Destination.Profile
                "password_change" -> Destination.PasswordChange
                "email_change" -> Destination.EmailChange
                "legal_consents" -> Destination.LegalConsents
                "delete_account" -> Destination.DeleteAccount
                "support" -> Destination.Support
                "about" -> Destination.About
                "guide" -> Destination.Guide
                "account_notifications" -> Destination.AccountNotifications
                else -> Destination.Home
            }
        } catch (_: Exception) {
            Destination.Home
        }
    }
}

class ScopedViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()

    fun clear() {
        viewModelStore.clear()
    }
}

class NavController(initialStack: List<Destination> = listOf(Destination.Home)) {
    private val _backStack = MutableStateFlow(if (initialStack.isNotEmpty()) initialStack else listOf(Destination.Home))
    val backStack: StateFlow<List<Destination>> = _backStack.asStateFlow()

    private val destinationStores = mutableMapOf<String, ScopedViewModelStoreOwner>()

    val currentDestination: Destination
        get() = _backStack.value.last()

    fun getStoreOwner(destination: Destination): ScopedViewModelStoreOwner {
        val key = DestinationCodec.encode(destination)
        return destinationStores.getOrPut(key) { ScopedViewModelStoreOwner() }
    }

    private fun pruneStores(remainingStack: List<Destination>) {
        val activeKeys = remainingStack.map { DestinationCodec.encode(it) }.toSet()
        val removedKeys = destinationStores.keys - activeKeys
        for (k in removedKeys) {
            destinationStores.remove(k)?.clear()
        }
    }

    fun navigateTo(destination: Destination) {
        val currentStack = _backStack.value
        if (currentStack.lastOrNull() == destination) return
        
        val isPrimaryRoot = when (destination) {
            Destination.Home,
            Destination.Workspaces,
            is Destination.WorkspaceHome,
            is Destination.Community,
            Destination.Calculations,
            Destination.Settings -> true
            else -> false
        }
        
        if (isPrimaryRoot) {
            val newStack = listOf(destination)
            _backStack.value = newStack
            pruneStores(newStack)
        } else {
            _backStack.value = currentStack + destination
        }
    }

    fun popBackStack(): Boolean {
        val currentStack = _backStack.value
        if (currentStack.size > 1) {
            val newStack = currentStack.dropLast(1)
            _backStack.value = newStack
            pruneStores(newStack)
            return true
        }
        return false
    }

    fun resetTo(destination: Destination) {
        val newStack = listOf(destination)
        _backStack.value = newStack
        pruneStores(newStack)
    }

    fun clearAllStores() {
        destinationStores.values.forEach { it.clear() }
        destinationStores.clear()
    }

    companion object {
        val Saver: Saver<NavController, ArrayList<String>> = Saver(
            save = { controller ->
                ArrayList(controller.backStack.value.map { DestinationCodec.encode(it) })
            },
            restore = { savedList ->
                val restored = savedList.map { DestinationCodec.decode(it) }
                NavController(if (restored.isNotEmpty()) restored else listOf(Destination.Home))
            }
        )
    }
}

@Composable
fun rememberNavController(initialDestination: Destination = Destination.Home): NavController {
    return rememberSaveable(saver = NavController.Saver) {
        NavController(listOf(initialDestination))
    }
}

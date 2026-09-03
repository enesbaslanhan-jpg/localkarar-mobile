package com.localkarar.app.navigation

import com.localkarar.app.network.dto.FormulaCalculationDto
import com.localkarar.app.network.dto.FormulaDto

sealed interface Destination {
    object Login : Destination
    object Home : Destination
    object Courses : Destination
    data class DecisionTools(val initialFilter: String = "all") : Destination
    data class DecisionTool(val code: String) : Destination
    object AiMentor : Destination
    object Calculations : Destination
    object News : Destination
    data class Community(val initialTab: String = "feed") : Destination
    object Workspaces : Destination
    object Settings : Destination

    data class CourseDetail(val courseId: Int) : Destination
    data class LessonReader(val courseId: Int, val lessonId: Int) : Destination
    data class DecisionSession(val sessionId: String) : Destination

    data class FormulaDetail(
        val formulaId: String,
        val historicalCalculation: FormulaCalculationDto? = null
    ) : Destination
    data class FinancialModelDetail(val code: String) : Destination
    data class ModelRuns(val workspaceId: String, val modelCode: String? = null) : Destination
    data class RunDetail(val workspaceId: String, val runId: String) : Destination

    data class WorkspaceHome(val workspaceId: String) : Destination
    data class Records(val workspaceId: String) : Destination
    data class RecordDetail(val workspaceId: String, val recordId: String) : Destination
    data class RecordEdit(val workspaceId: String, val recordId: String?) : Destination
    data class Orders(val workspaceId: String) : Destination
    data class Products(val workspaceId: String) : Destination
    data class Documents(val workspaceId: String) : Destination
    data class Notifications(val workspaceId: String) : Destination
    data class Calendar(val workspaceId: String) : Destination
    data class Team(val workspaceId: String) : Destination
    data class Contacts(val workspaceId: String) : Destination
    data class Activity(val workspaceId: String) : Destination
    data class WorkspaceSettings(val workspaceId: String) : Destination

    /**
     * Pazaryeri entegrasyonlari (baglama/kesme/esitleme).
     *
     * Bu hedef YOKTU: kullanici mobilden hicbir pazaryeri baglayamiyordu ve
     * bu yuzden Siparisler/Urunler ekranlarina gercek veri hic gelmiyordu.
     */
    data class WorkspaceIntegrations(val workspaceId: String) : Destination

    data class Conversation(val conversationId: Int) : Destination
    data class NewsDetail(val articleId: String) : Destination
    data class CommunityPost(val postId: String) : Destination
    data class CommunityProfile(val userId: Int) : Destination
    data class CommunityFollowers(val userId: Int, val mode: String) : Destination
    data class CommunityThreadDetail(val threadId: String) : Destination
    object CommunityNotifications : Destination

    object Profile : Destination
    object PasswordChange : Destination
    object EmailChange : Destination
    object LegalConsents : Destination
    object DeleteAccount : Destination

    /**
     * Destek / yardim formu.
     *
     * Webdeki `/yardim` sayfasinin karsiligi; mobilde HIC YOKTU. Onemi:
     * `POST /support/contact` uyelik kapisindan muaf, yani salt okunur moda
     * dusmus kullanicinin kalan tek yazma yolu.
     */
    object Support : Destination
    object About : Destination
    object Guide : Destination

    /**
     * Bilgi Kutuphanesi. Webdeki `/app/knowledge` ve konu detayinin karsiligi;
     * mobilde HIC YOKTU.
     */

    /** Hesap bildirimleri (webdeki `/app/bildirimler`). Mobilde HIC YOKTU. */
    object AccountNotifications : Destination
}

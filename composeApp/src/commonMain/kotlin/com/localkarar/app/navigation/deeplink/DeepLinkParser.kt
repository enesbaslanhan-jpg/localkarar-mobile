package com.localkarar.app.navigation.deeplink

sealed interface DeepLinkResult {
    data class Success(val target: DeepLinkTarget) : DeepLinkResult
    data object Unsupported : DeepLinkResult
    data class Malformed(val reason: String) : DeepLinkResult
}

object DeepLinkParser {
    const val CANONICAL_HOST = "localkarar.com"
    const val CANONICAL_SCHEME = "https"

    fun parse(rawUrl: String?): DeepLinkResult {
        if (rawUrl.isNullOrBlank()) {
            return DeepLinkResult.Malformed("URL is empty or blank")
        }

        // Basic sanity check to prevent malicious injections or unbounded strings
        if (rawUrl.length > 2048) {
            return DeepLinkResult.Malformed("URL length exceeds safe limit")
        }

        return try {
            // Strip fragment and query parameters safely
            val urlWithoutQuery = rawUrl.substringBefore('?').substringBefore('#').trim()

            // Scheme check
            if (!urlWithoutQuery.startsWith("$CANONICAL_SCHEME://", ignoreCase = true)) {
                return DeepLinkResult.Unsupported
            }

            val schemeStripped = urlWithoutQuery.substring("$CANONICAL_SCHEME://".length)
            val host = schemeStripped.substringBefore('/').substringBefore(':').lowercase()

            if (host != CANONICAL_HOST) {
                return DeepLinkResult.Unsupported
            }

            val pathPart = if (schemeStripped.contains('/')) {
                "/" + schemeStripped.substringAfter('/')
            } else {
                "/"
            }

            // Path must begin with /app/
            if (pathPart != "/app" && !pathPart.startsWith("/app/")) {
                return DeepLinkResult.Unsupported
            }

            // Split into non-empty segments
            val segments = pathPart.split('/').filter { it.isNotEmpty() }
            // segments[0] is "app"
            if (segments.size < 2) {
                return DeepLinkResult.Unsupported
            }

            val appSegments = segments.drop(1)
            val target = matchSegments(appSegments)
            if (target != null) {
                DeepLinkResult.Success(target)
            } else {
                DeepLinkResult.Unsupported
            }
        } catch (e: Exception) {
            DeepLinkResult.Malformed(e.message ?: "Failed to parse URI")
        }
    }

    private fun matchSegments(segments: List<String>): DeepLinkTarget? {
        if (segments.isEmpty()) return null

        return when (segments[0]) {
            "community" -> {
                when (segments.size) {
                    1 -> DeepLinkTarget.NewsRoot
                    2 -> when (segments[1]) {
                        "topluluk" -> DeepLinkTarget.CommunityFeedRoot
                        "kisiler" -> DeepLinkTarget.CommunityPeopleRoot
                        "sohbetler" -> DeepLinkTarget.CommunityThreadsRoot
                        else -> null
                    }
                    3 -> if (segments[1] == "gonderi" && segments[2].isNotBlank()) {
                        DeepLinkTarget.CommunityPost(segments[2])
                    } else null
                    else -> null
                }
            }
            "profil" -> {
                when (segments.size) {
                    1 -> DeepLinkTarget.SelfProfile
                    2 -> {
                        val userId = segments[1].toIntOrNull()
                        if (userId != null && userId > 0) {
                            DeepLinkTarget.UserProfile(userId)
                        } else null
                    }
                    else -> null
                }
            }
            "bildirimler" -> {
                if (segments.size == 1) DeepLinkTarget.NotificationsRoot else null
            }
            /*
             * BILGI KUTUPHANESI, BILGI NESNESI ve OGRENME YOLU
             * DESTEKLENMIYOR.
             *
             * Urun sahibi karari (03.09.2026): bu yuzeyler deneme amacliydi
             * ve KALDIRILDI -- webde de rotalari silindi. Urunun ogrenme
             * yuzeyi 38 kanonik kurs.
             *
             * Buraya bir dal eklemek, olmayan bir ekrana yonlendirmek olur.
             * `DeepLinkParserTest.bilgiVeOgrenmeYoluYollariEslesmez` bunu
             * bekcilik ediyor.
             */
            "mentor" -> {
                if (segments.size == 1) DeepLinkTarget.MentorRoot else null
            }
            "courses" -> {
                when (segments.size) {
                    1 -> DeepLinkTarget.CoursesRoot
                    3 -> {
                        // /app/courses/:courseId/learn
                        if (segments[2] == "learn") {
                            val courseId = segments[1].toIntOrNull()
                            if (courseId != null && courseId > 0) {
                                DeepLinkTarget.CourseDetail(courseId)
                            } else null
                        } else null
                    }
                    4 -> {
                        // /app/courses/:courseId/learn/:lessonId
                        if (segments[2] == "learn") {
                            val courseId = segments[1].toIntOrNull()
                            val lessonId = segments[3].toIntOrNull()
                            if (courseId != null && courseId > 0 && lessonId != null && lessonId > 0) {
                                DeepLinkTarget.CourseLesson(courseId, lessonId)
                            } else null
                        } else null
                    }
                    else -> null
                }
            }
            "decision-checks" -> {
                when (segments.size) {
                    1 -> DeepLinkTarget.DecisionToolsRoot
                    2 -> {
                        val code = segments[1].trim()
                        if (code.isNotEmpty() && code.length <= 100) {
                            DeepLinkTarget.DecisionTool(code)
                        } else null
                    }
                    else -> null
                }
            }
            "finance" -> {
                if (segments.size == 3 && segments[1] == "models") {
                    val code = segments[2].trim()
                    if (code.isNotEmpty() && code.length <= 100) {
                        DeepLinkTarget.FinancialModel(code)
                    } else null
                } else null
            }
            "workspaces" -> {
                when (segments.size) {
                    1 -> DeepLinkTarget.WorkspacesRoot
                    2 -> {
                        val wsId = segments[1].trim()
                        if (wsId.isNotEmpty()) DeepLinkTarget.WorkspaceHome(wsId) else null
                    }
                    3 -> {
                        val wsId = segments[1].trim()
                        if (wsId.isEmpty()) return null
                        when (segments[2]) {
                            "overview" -> DeepLinkTarget.WorkspaceHome(wsId)
                            "tracker" -> DeepLinkTarget.WorkspaceRecords(wsId)
                            "orders" -> DeepLinkTarget.WorkspaceOrders(wsId)
                            "products" -> DeepLinkTarget.WorkspaceProducts(wsId)
                            "calendar" -> DeepLinkTarget.WorkspaceCalendar(wsId)
                            "documents" -> DeepLinkTarget.WorkspaceDocuments(wsId)
                            "notifications" -> DeepLinkTarget.WorkspaceNotifications(wsId)
                            "team" -> DeepLinkTarget.WorkspaceTeam(wsId)
                            "contacts" -> DeepLinkTarget.WorkspaceContacts(wsId)
                            "settings" -> DeepLinkTarget.WorkspaceSettings(wsId)
                            "activity" -> DeepLinkTarget.WorkspaceActivity(wsId)
                            else -> null
                        }
                    }
                    else -> null
                }
            }
            "settings" -> {
                if (segments.size == 1) DeepLinkTarget.SettingsRoot else null
            }
            else -> null
        }
    }
}

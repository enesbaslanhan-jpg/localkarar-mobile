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

            /*
             * DAVET BAGLANTISI — `/app/` DISINDAKI TEK ISTISNA.
             *
             * 🔴 MOBILDE HIC CALISMIYORDU. Ekip daveti e-postasi
             * `https://localkarar.com/davet?token=...` gonderiyor
             * (`mail-templates.ts:182`). Davetli kisi bunu TELEFONUNDA
             * aciyor -- ekip davetinin dogal yeri orasi. Mobil yalniz
             * `/app/` yollarini tanidigi icin baglanti tarayiciya
             * dusuyor, uygulamada davet kabul etmenin HICBIR yolu yoktu.
             *
             * ⚠️ TOKEN SORGU DIZESINDE ve bu ayristirici sorgu dizesini
             * en basta BILEREK atiyor (`substringBefore('?')`) --
             * sorgudan gelen degerlerin yonlendirmeye karismasi bir
             * saldiri yuzeyi. O karar KORUNUYOR: sorgu yalnizca bu tek
             * yolda, yalnizca `token` anahtari icin ve KATI bir bicim
             * dogrulamasindan gecerek okunuyor.
             */
            if (pathPart == "/davet") {
                return davetiCoz(rawUrl)
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
            val target = matchSegments(appSegments, rawUrl)
            if (target != null) {
                DeepLinkResult.Success(target)
            } else {
                DeepLinkResult.Unsupported
            }
        } catch (e: Exception) {
            DeepLinkResult.Malformed(e.message ?: "Failed to parse URI")
        }
    }

    /*
     * Davet jetonunu sorgu dizesinden cikarir.
     *
     * Sunucu jetonu `hashToken` ile karsilastiriyor (`workspace.ts:1047`)
     * ve uretimi rastgele bir onaltilik dize. Burada bicim KATI
     * dogrulaniyor: yalnizca harf, rakam, tire ve alt tire; 16-256
     * karakter. Amaç sunucunun isini yapmak degil, ekrana ve oradan
     * istege GECERSIZ bir seyin girmemesi.
     */
    private fun davetiCoz(rawUrl: String): DeepLinkResult {
        val sorgu = rawUrl.substringAfter('?', "").substringBefore('#')
        if (sorgu.isBlank()) return DeepLinkResult.Malformed("Davet bağlantısında kod yok")

        val jeton = sorgu.split('&')
            .firstOrNull { it.startsWith("token=", ignoreCase = true) }
            ?.substringAfter('=')
            ?.trim()

        if (jeton.isNullOrBlank()) {
            return DeepLinkResult.Malformed("Davet bağlantısında kod yok")
        }
        if (!jeton.matches(JETON_BICIMI)) {
            return DeepLinkResult.Malformed("Davet kodu geçersiz")
        }
        return DeepLinkResult.Success(DeepLinkTarget.Invitation(jeton))
    }

    private val JETON_BICIMI = Regex("^[A-Za-z0-9_-]{16,256}$")

    /*
     * AYARLAR BOLUMU — sorgu dizesinden okunan IKINCI ve son deger.
     *
     * Web ayarlar sayfasini tek adres altinda bolumlere ayiriyor
     * (`SettingsPage.jsx` -> `bolumSec`): hem `?bolum=` hem `#bolum`.
     * Mobilde bu bolumlerin cogu zaten Ayarlar listesinde yan yana
     * duruyor, o yuzden hepsi icin ayri bir hedef YOK.
     *
     * ⚠️ YALNIZ `integrations` ceviriliyor: mobilde karsiligi ayri bir
     * EKRAN (`WorkspaceIntegrations`) ve web dort yerden bu adrese
     * gonderiyor. Diger degerler (`uyelik`, `profile`, `security`...)
     * Ayarlar kokune dusuyor -- bugunku davranis, gerileme degil.
     *
     * ⚠️ Sorgudan okunan her sey bir saldiri yuzeyi; bu yuzden deger
     * yonlendirmeye ham girmiyor, TEK bir sabitle karsilastiriliyor.
     */
    private fun ayarlarBolumu(rawUrl: String): String? {
        val sorguVeParca = rawUrl.substringAfter('?', "")
        val sorgu = sorguVeParca.substringBefore('#')
        val sorgudan = sorgu.split('&')
            .firstOrNull { it.startsWith("bolum=", ignoreCase = true) }
            ?.substringAfter('=')
            ?.trim()
        if (!sorgudan.isNullOrBlank()) return sorgudan
        /* Sorgu onceligi korunuyor; web de once sorguya bakiyor. */
        return rawUrl.substringAfter('#', "").trim().takeIf { it.isNotBlank() }
    }

    private fun matchSegments(segments: List<String>, rawUrl: String): DeepLinkTarget? {
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
            "onboarding" -> if (segments.size == 1) DeepLinkTarget.OnboardingRoot else null
            "assessment" -> if (segments.size == 1) DeepLinkTarget.AssessmentRoot else null
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
                when {
                    segments.size != 1 -> null
                    ayarlarBolumu(rawUrl) == "integrations" -> DeepLinkTarget.IntegrationsRoot
                    else -> DeepLinkTarget.SettingsRoot
                }
            }
            else -> null
        }
    }
}

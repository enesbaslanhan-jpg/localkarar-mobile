package com.localkarar.app.ui.screens

import androidx.compose.runtime.Composable
import com.localkarar.app.ui.components.LkEmptyState
import com.localkarar.app.ui.components.LkPageLayout




@Composable
fun AiMentorScreen() {
    LkPageLayout(title = "AI Mentor") {
        LkEmptyState(
            title = "Yapay Zeka Asistanı",
            description = "Size özel AI Mentor ile konuşma arayüzü."
        )
    }
}

@Composable
fun CalculationsScreen(onBack: () -> Unit) {
    LkPageLayout(title = "Hesaplamalar", onBack = onBack) {
        LkEmptyState(
            title = "Hesaplamalar",
            description = "Geçmiş hesaplamalarınız ve taslaklarınız."
        )
    }
}

@Composable
fun NewsScreen(onBack: () -> Unit) {
    LkPageLayout(title = "Haberler", onBack = onBack) {
        LkEmptyState(
            title = "Finansal Haberler",
            description = "Günün önemli gelişmeleri ve piyasa haberleri."
        )
    }
}

@Composable
fun UpdatesScreen(onBack: () -> Unit) {
    LkPageLayout(title = "Güncellemeler", onBack = onBack) {
        LkEmptyState(
            title = "Platform Güncellemeleri",
            description = "LocalKarar sürüm notları ve yenilikler."
        )
    }
}

@Composable
fun SavedScreen(onBack: () -> Unit) {
    LkPageLayout(title = "Kaydedilenler", onBack = onBack) {
        LkEmptyState(
            title = "Kaydedilen İçerikler",
            description = "Daha sonra okumak üzere kaydettiğiniz öğeler."
        )
    }
}

@Composable
fun ProgressScreen(onBack: () -> Unit) {
    LkPageLayout(title = "Öğrenme İlerlemesi", onBack = onBack) {
        LkEmptyState(
            title = "Gelişim Raporu",
            description = "Kurs ve modül tamamlama istatistikleriniz."
        )
    }
}

@Composable
fun ProfileScreen(onBack: () -> Unit, firstName: String?) {
    LkPageLayout(title = "Profil", onBack = onBack) {
        LkEmptyState(
            title = "Profil ve Hesap",
            description = "Kullanıcı: ${firstName ?: "Bilinmiyor"}\n\nHesap ayarları burada yer alacak."
        )
    }
}



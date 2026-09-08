package com.localkarar.app.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Reply
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.community.CommunityNotificationsViewModel
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.navigation.Destination
import com.localkarar.app.navigation.deeplink.DeepLinkParser
import com.localkarar.app.navigation.deeplink.DeepLinkResult
import com.localkarar.app.network.dto.CommunityNotificationDto
import com.localkarar.app.settings.AccountNotificationDto
import com.localkarar.app.settings.AccountNotificationsUiState
import com.localkarar.app.settings.AccountNotificationsViewModel
import com.localkarar.app.ui.components.LkCard
import com.localkarar.app.ui.components.LkEmptyState
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.components.LkLoadingDesen
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.theme.*

/**
 * BILDIRIMLER — TEK EKRAN, IKI BOLUM.
 *
 * 🔴 MOBILDE IKI AYRI EKRAN VARDI ve ikisinin de basligi "Bildirimler"di:
 * biri Topluluk sekmesindeki zil simgesinden, digeri Ayarlar listesinden
 * aciliyordu. Kullanici "bildirimlerim" derken tek bir yer bekliyor;
 * ayni adi tasiyan iki ekran, birinde gordugu seyi digerinde
 * bulamamasi demekti.
 *
 * Web tek sayfada iki bolum sunuyor (`NotificationsPage.jsx`) ve bu
 * ekran onun karsiligi.
 *
 * ⚠️ SIRA WEBDEKIYLE AYNI: once HESAP (uyelik, odeme), sonra TOPLULUK.
 * Webin gerekcesi kod icinde yaziyor: "üyeliğin doluyor" uyarisini
 * begeni bildirimlerinin arasinda kaybetmemek. Tek listede
 * KARISTIRILMIYOR.
 *
 * ⚠️ IKI KAYNAK BAGIMSIZ YUKLENIYOR. Biri duserse digeri yine
 * gosteriliyor; hata ekrani ancak IKISI birden duserse cikiyor. Web de
 * `Promise.allSettled` ile ayni sekilde davraniyor -- tek bir ucun
 * duşmesi butun bildirimleri kaybettirmemeli.
 */
@Composable
fun BildirimlerScreen(
    hesapViewModel: AccountNotificationsViewModel,
    toplulukViewModel: CommunityNotificationsViewModel,
    onBack: () -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenProfile: (Int) -> Unit,
    onOpenThread: (String) -> Unit,
    onNavigate: (Destination) -> Unit,
    /** Calisma alani kimligi tasimayan baglar icin (orn. entegrasyonlar). */
    aktifCalismaAlaniId: String?
) {
    val hesapDurum by hesapViewModel.uiState.collectAsState()
    val toplulukDurum by toplulukViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        hesapViewModel.yukle()
        toplulukViewModel.loadNotifications()
    }

    val hesapBildirimleri = (hesapDurum as? AccountNotificationsUiState.Content)?.bildirimler.orEmpty()
    val toplulukBildirimleri =
        (toplulukDurum as? CommunityNotificationsViewModel.NotificationsUiState.Content)?.items.orEmpty()

    val hesapOkunmamis = (hesapDurum as? AccountNotificationsUiState.Content)?.okunmamis ?: 0
    val okunmamisToplam = hesapOkunmamis + toplulukViewModel.unreadCount

    val yukleniyor = hesapDurum is AccountNotificationsUiState.Loading ||
        toplulukDurum is CommunityNotificationsViewModel.NotificationsUiState.Loading
    val ikisiDeDustu = hesapDurum is AccountNotificationsUiState.Error &&
        toplulukDurum is CommunityNotificationsViewModel.NotificationsUiState.Error

    LkHeroPage(
        title = "Bildirimler",
        onBack = onBack,
        actions = {
            /* Dugme "tumunu okundu isaretle" diyor: IKISINI birden
               isaretliyor. Yalniz birini isaretlemek sayaci
               sifirlamaz ve dugme calismiyor gorunurdu (web de ikisini
               birden cagiriyor). */
            if (okunmamisToplam > 0) {
                TextButton(
                    onClick = {
                        hesapViewModel.tumunuOkunduIsaretle()
                        toplulukViewModel.markAllRead()
                    }
                ) {
                    Text("Tümünü oku", style = LkTypography.getMicro(), color = LkHero.OnHero)
                }
            }
        },
        heroExtra = {
            if (okunmamisToplam > 0) {
                Text(
                    text = "$okunmamisToplam okunmamış",
                    style = LkTypography.getMetadata(),
                    color = LkHero.OnHeroSecondary,
                    modifier = Modifier.padding(start = LkSpacing.Space5, top = LkSpacing.Space2)
                )
            }
        }
    ) {
        when {
            yukleniyor && hesapBildirimleri.isEmpty() && toplulukBildirimleri.isEmpty() ->
                LkLoadingState(desen = LkLoadingDesen.LISTE)

            ikisiDeDustu -> LkErrorState(
                message = (hesapDurum as AccountNotificationsUiState.Error).mesaj,
                onRetry = {
                    hesapViewModel.yukle()
                    toplulukViewModel.loadNotifications()
                }
            )

            hesapBildirimleri.isEmpty() && toplulukBildirimleri.isEmpty() -> LkEmptyState(
                title = "Bildirim yok",
                description = "Hesabınla ya da toplulukla ilgili bir gelişme olduğunda burada görürsün.",
                icon = Icons.Outlined.NotificationsNone
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(LkSpacing.Space4),
                verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)
            ) {
                if (hesapBildirimleri.isNotEmpty()) {
                    item { BolumBasligi("HESAP") }
                    items(hesapBildirimleri, key = { "hesap_${it.id}" }) { bildirim ->
                        HesapBildirimSatiri(
                            bildirim = bildirim,
                            onClick = hedefeGit(bildirim.linkTo, aktifCalismaAlaniId, onNavigate)
                        )
                    }
                }

                if (toplulukBildirimleri.isNotEmpty()) {
                    /* Baslik ancak hesap bolumu de VARSA yaziliyor: tek
                       bolum varken "Topluluk" basligi bilgi tasimaz.
                       Web de ayni kosulu koyuyor. */
                    if (hesapBildirimleri.isNotEmpty()) {
                        item { BolumBasligi("TOPLULUK") }
                    }
                    items(toplulukBildirimleri, key = { "topluluk_${it.id}" }) { bildirim ->
                        ToplulukBildirimSatiri(
                            bildirim = bildirim,
                            onClick = {
                                when (bildirim.type) {
                                    "follow" -> bildirim.actor?.id?.let(onOpenProfile)
                                    "like", "reply", "quote" -> bildirim.postId?.let(onOpenPost)
                                    "message", "thread_invite" -> bildirim.threadId?.let(onOpenThread)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BolumBasligi(metin: String) {
    Text(
        text = metin,
        style = LkTypography.getMicro(),
        color = LkTextMuted,
        modifier = Modifier.padding(top = LkSpacing.Space2, bottom = LkSpacing.Space1)
    )
}

/**
 * Hesap bildiriminin hedefi.
 *
 * 🔴 Mobil `linkTo` alanini HIC KULLANMIYORDU: satirlar dokunulamazdi.
 * Web tikladiginda oraya gidiyor ("Ödemeniz alındı" → üyelik bölümü).
 *
 * ⚠️ Sunucu GORECELI bir yol gonderiyor (`/app/settings#uyelik`).
 * Kendi ayristiricimiza veriliyor: hem hedefi cozuyor hem de tanimadigi
 * bir yolu SESSIZCE reddediyor. Boylece sunucudan gelen bir dize
 * dogrudan yonlendirmeye girmiyor.
 *
 * ⚠️ Cozulemeyen bagda satir dokunulamaz kaliyor -- kullaniciyi bos bir
 * ekrana goturmek, dokunulamaz olmasindan kotudur (webin kendi notu).
 */
private fun hedefeGit(
    linkTo: String?,
    aktifCalismaAlaniId: String?,
    onNavigate: (Destination) -> Unit
): (() -> Unit)? {
    if (linkTo.isNullOrBlank()) return null
    val tamAdres = if (linkTo.startsWith("/")) {
        "${DeepLinkParser.CANONICAL_SCHEME}://${DeepLinkParser.CANONICAL_HOST}$linkTo"
    } else {
        linkTo
    }
    val sonuc = DeepLinkParser.parse(tamAdres)
    if (sonuc !is DeepLinkResult.Success) return null
    return { onNavigate(sonuc.target.toDestination(aktifCalismaAlaniId)) }
}

/*
 * Hesap bildirimi ikonlari. Uyari tonundakiler AYRI ikon aliyor;
 * hepsine kart ikonu koymak "ödeme başarısız" ile "ödeme alındı"yi
 * gorsel olarak esitlerdi (webin kendi gerekcesi).
 */
private fun hesapIkonu(tur: String?): ImageVector = when (tur) {
    "trial_ending", "trial_ended", "payment_failed" -> Icons.Outlined.WarningAmber
    "payment_succeeded", "renewal_upcoming", "membership_cancelled" -> Icons.Outlined.CreditCard
    else -> Icons.Outlined.Notifications
}

@Composable
private fun HesapBildirimSatiri(
    bildirim: AccountNotificationDto,
    onClick: (() -> Unit)?
) {
    val okunmamis = bildirim.readAt == null

    LkCard(
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
        padding = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (okunmamis) LkPrimarySoft else LkSurfacePanel),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    hesapIkonu(bildirim.type),
                    contentDescription = null,
                    tint = if (okunmamis) LkPrimary else LkTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = bildirim.title,
                    style = LkTypography.getBodySmall(),
                    color = LkTextPrimary,
                    fontWeight = if (okunmamis) FontWeight.Bold else FontWeight.Normal
                )
                if (!bildirim.body.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(bildirim.body, style = LkTypography.getMicro(), color = LkTextSecondary)
                }
                /*
                 * 🔴 TARIH HAM GELIYORDU: satirda sunucunun ISO dizesi
                 * yaziyordu ("2026-09-08T11:04:12.000Z"). Topluluk
                 * bildirimleri ayni ekranda bicimlenmis tarih
                 * gosterirken hesap bildirimleri ham dize gosteriyordu.
                 */
                bildirim.createdAt?.takeIf { it.isNotBlank() }?.let { ts ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        LkDateUtils.formatDateTime(ts),
                        style = LkTypography.getMicro(),
                        color = LkTextMuted
                    )
                }
            }

            /* Okunmamis: renk DEGIL, dolu nokta. Topluluk satiriyla ayni
               isaret -- iki bolum ayni ekranda, iki farkli "okunmadi"
               dili konusmamali. */
            if (okunmamis) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(LkPrimary)
                )
            }
        }
    }
}

@Composable
private fun ToplulukBildirimSatiri(
    bildirim: CommunityNotificationDto,
    onClick: () -> Unit
) {
    val okunmamis = bildirim.readAt == null

    val (ikon, eylemMetni) = when (bildirim.type) {
        "follow" -> Icons.Outlined.PersonAdd to "seni takip etmeye başladı"
        "like" -> Icons.Outlined.Favorite to "gönderini beğendi"
        "reply" -> Icons.Outlined.Reply to "gönderine yanıt verdi"
        "quote" -> Icons.Outlined.FormatQuote to "gönderini alıntıladı"
        "message" -> Icons.Outlined.Chat to "sohbette mesaj gönderdi"
        "thread_invite" -> Icons.Outlined.GroupAdd to "seni bir gruba davet etti"
        else -> Icons.Outlined.Notifications to "bir etkileşimde bulundu"
    }

    LkCard(
        modifier = Modifier.clickable(onClick = onClick),
        padding = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (okunmamis) LkPrimarySoft else LkSurfacePanel),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    ikon,
                    contentDescription = null,
                    tint = if (okunmamis) LkPrimary else LkTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = "${bildirim.actor?.name ?: "Biri"} $eylemMetni",
                    style = LkTypography.getBodySmall(),
                    color = LkTextPrimary,
                    fontWeight = if (okunmamis) FontWeight.Bold else FontWeight.Normal
                )
                bildirim.post?.ozet?.let { ozet ->
                    Spacer(Modifier.height(2.dp))
                    Text("\"$ozet\"", style = LkTypography.getMicro(), color = LkTextMuted, maxLines = 1)
                }
                bildirim.createdAt?.let { ts ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        LkDateUtils.formatDateTime(ts),
                        style = LkTypography.getMicro(),
                        color = LkTextMuted
                    )
                }
            }

            if (okunmamis) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(LkPrimary)
                )
            }
        }
    }
}

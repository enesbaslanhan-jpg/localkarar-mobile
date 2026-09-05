package com.localkarar.app.ui.screens.community

import com.localkarar.app.ui.components.LkHairline
import com.localkarar.app.ui.components.LkListRow
import com.localkarar.app.ui.components.LkRowGroup
import com.localkarar.app.ui.components.LkCard
import com.localkarar.app.ui.components.LkRemoteImage
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import com.localkarar.app.ui.components.LkAvatar
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import com.localkarar.app.ui.components.LkHeroBlock
import com.localkarar.app.ui.theme.LkSpacing
import com.localkarar.app.ui.components.LkTabStyle
import com.localkarar.app.ui.components.LkTabs
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.localkarar.app.community.CommunityNotificationsViewModel
import com.localkarar.app.community.CommunityViewModel
import com.localkarar.app.community.SocialViewModel
import com.localkarar.app.community.ThreadsViewModel
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.network.dto.CommunityPostDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.theme.*

/**
 * Topluluk alt bolumleri — SIRA VE ADLAR WEBDEKI ILE AYNI.
 *
 * Web kenar cubugu (`Sidebar.jsx:189-192`): Akis → Profil → Takip ve
 * engelleme → Sohbetler.
 *
 * 🔴 IKI SAPMA DUZELTILDI:
 *   1. Sira farkliydi (Akis, Kisiler, Sohbetler, Profil).
 *   2. "Kisiler" webde "Takip ve engelleme" (`nav.followingAndBlocking`).
 *      Ayni bolume iki platformda iki ad vermek, kullaniciya ayni seyi
 *      arattirirken farkli kelime ogretiyordu.
 */
enum class CommunityInternalTab(val title: String) {
    FEED("Akış"),
    PROFILE("Profil"),
    CHATS("Sohbetler");

    companion object {
        /*
         * 🔴 "Takip ve engelleme" UST SEKMEDEN CIKARILDI (urun sahibi karari).
         *
         * Ust seritteki en uzun etiket oydu ve dort sekme ekranin ustunu
         * kaplıyordu. Ait oldugu yer profil: takip ettiklerin ve
         * engellediklerin SENIN hesabina ait seyler, toplulugun ayri bir
         * bolumu degil.
         *
         * ⚠️ ERISIM KAYBI YOK: Profil sekmesinin icinden aciliyor
         * (`PROFILE` dalindaki `onOpenPeople`) ve derin baglanti da
         * calismaya devam ediyor — `initialTab = "people"` hala Profil'e
         * dusup listeyi aciyor.
         */
        fun fromDeepLink(raw: String): CommunityInternalTab = when (raw.lowercase()) {
            "threads", "sohbetler", "chats" -> CHATS
            "profile", "profil", "people", "kisiler", "members" -> PROFILE
            else -> FEED
        }
    }
}

@Composable
fun CommunityFeedScreen(
    communityViewModel: CommunityViewModel,
    socialViewModel: SocialViewModel,
    threadsViewModel: ThreadsViewModel,
    notificationsViewModel: CommunityNotificationsViewModel,
    currentUserId: Int? = null,
    initialTab: String = "feed",
    onOpenPost: (String) -> Unit,
    onOpenProfile: (Int) -> Unit,
    onOpenThread: (String) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenFollowers: (Int, String) -> Unit,
    onOpenProductCenter: (() -> Unit)? = null
) {
    val startingTab = CommunityInternalTab.fromDeepLink(initialTab)
    var currentSubTab by remember(initialTab) { mutableStateOf(startingTab) }

    /*
     * Profil sekmesinin icinden acilan "Takip ve engelleme" listesi.
     * Derin baglanti "people" ile gelindiyse dogrudan acik baslar.
     */
    var kisilerAcik by remember(initialTab) {
        mutableStateOf(initialTab.lowercase() in listOf("people", "kisiler", "members"))
    }
    val unreadNotifs = notificationsViewModel.unreadCount

    /* §24.6 — hero baslik blogu + binen yuzey. */
    Column(Modifier.fillMaxSize()) {

        LkHeroBlock {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = LkSpacing.Space5,
                        end = LkSpacing.Space4,
                        top = LkSpacing.Space4,
                        bottom = LkSpacing.Space6
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Topluluk",
                    style = LkTypography.getTitleL(),
                    color = LkHero.OnHero,
                    modifier = Modifier.weight(1f)
                )
                Box {
                    IconButton(onClick = onOpenNotifications) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = "Bildirimler",
                            tint = LkHero.OnHero
                        )
                    }
                    if (unreadNotifs > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 6.dp, end = 6.dp)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(LkDanger),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (unreadNotifs > 9) "9+" else "$unreadNotifs",
                                style = LkTypography.getMicro(),
                                color = LkOnPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                if (onOpenProductCenter != null) {
                    IconButton(onClick = onOpenProductCenter) {
                        Icon(
                            Icons.Outlined.Apps,
                            contentDescription = "Ürünler",
                            tint = LkHero.OnHero
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .offset(y = (-22).dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(LkSurfaceCanvas)
        ) {
            Spacer(Modifier.height(LkSpacing.Space4))
            // §11: sekmeler ortak bilesenden.
            //
            // 🔴 ONCEDEN ELLE YAZILMISTI ve secili sekme `LkPrimary` zemin +
            // beyaz yazi kullaniyordu. Koyu temada `LkPrimary` = brand-300
            // (#7BA2B3); uzerine beyaz 1.9:1 veriyor -- §19'un 4.5:1 esiginin
            // altinda. `LkTabs` secili segmentte `primaryFill` kullaniyor
            // (brand-500), beyazla her iki modda 4.6:1.
            /*
             * §24 — HAP SEKMELER, alt cizgi degil.
             *
             * ⚠️ Daha once `LkTabs`in SEGMENTED bicimi denenmis ve
             * birakilmisti: dort sekme esit sutunlara bolununce
             * "Takip ve engelleme" iki satira kirilip segmenti tasiriyordu.
             *
             * Cozum esit sutun DEGIL, KAYDIRILABILIR hap seridi: her hap
             * kendi metni kadar genis, uzun etiket kirilmiyor, sigmayan
             * yana kayiyor. Ayni desen cekmecedeki durum haplarinda da var.
             */
            val sekmeler = CommunityInternalTab.values().toList()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space3),
                horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)
            ) {
                sekmeler.forEach { sekme ->
                    val secili = sekme == currentSubTab
                    Text(
                        text = sekme.title,
                        style = LkTypography.getLabelM(),
                        /* Secili hapta `primaryFill` (brand-500) + beyaz:
                           her iki temada 4.6:1. `LkPrimary` koyu temada
                           brand-300 ve beyazla 1.9:1 verirdi. */
                        color = if (secili) LkOnPrimary else LkTextSecondary,
                        maxLines = 1,
                        modifier = Modifier
                            .clip(LkShapes.FULL)
                            .background(if (secili) LkPrimaryFill else LkSurfaceTile)
                            .clickable { currentSubTab = sekme }
                            .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space2)
                    )
                }
            }

            when (currentSubTab) {
                CommunityInternalTab.FEED -> {
                    FeedTabContent(
                        viewModel = communityViewModel,
                        onOpenPost = onOpenPost,
                        onOpenProfile = onOpenProfile
                    )
                }
                CommunityInternalTab.CHATS -> {
                    ThreadsScreen(
                        viewModel = threadsViewModel,
                        currentUserId = currentUserId,
                        onOpenThread = onOpenThread
                    )
                }
                CommunityInternalTab.PROFILE -> {
                    if (kisilerAcik) {
                        /* Profil icindeki "Takip ve engelleme" listesi. */
                        Column(Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = LkSpacing.Space3,
                                        end = LkSpacing.Space4,
                                        top = LkSpacing.Space2
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { kisilerAcik = false }) {
                                    Icon(
                                        Icons.Outlined.ArrowBack,
                                        contentDescription = "Profile dön",
                                        tint = LkTextPrimary
                                    )
                                }
                                Text(
                                    "Takip ve engelleme",
                                    style = LkTypography.getTitleS(),
                                    color = LkTextPrimary
                                )
                            }
                            PeopleScreen(
                                viewModel = socialViewModel,
                                onOpenProfile = onOpenProfile
                            )
                        }
                    } else {
                        Column(Modifier.fillMaxSize()) {
                            /*
                             * Profilin icinden "Takip ve engelleme"ye giris.
                             * Ust sekmeden buraya tasindi; takip ettiklerin ve
                             * engellediklerin kullanicinin KENDI hesabina ait.
                             */
                            LkRowGroup(
                                modifier = Modifier.padding(
                                    horizontal = LkSpacing.Space4,
                                    vertical = LkSpacing.Space2
                                )
                            ) {
                                LkListRow(
                                    baslik = "Takip ve engelleme",
                                    altBaslik = "Takip ettiklerin ve engellediklerin",
                                    onClick = { kisilerAcik = true },
                                    ikon = {
                                        Icon(
                                            Icons.Outlined.PeopleOutline,
                                            contentDescription = null,
                                            tint = LkTileInk,
                                            modifier = Modifier.size(21.dp)
                                        )
                                    },
                                    sag = {
                                        Icon(
                                            Icons.Outlined.ChevronRight,
                                            contentDescription = null,
                                            tint = LkTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }

                            ProfileScreen(
                                userId = null, // Own profile
                                socialViewModel = socialViewModel,
                                communityViewModel = communityViewModel,
                                onBack = null,
                                onOpenFollowers = onOpenFollowers,
                                onOpenPost = onOpenPost,
                                onOpenProfile = onOpenProfile
                            )
                        }
                    }
                }
            }
        }
    }

    if (communityViewModel.composing) {
        ComposePostSheet(viewModel = communityViewModel)
    }
}

@Composable
private fun FeedTabContent(
    viewModel: CommunityViewModel,
    onOpenPost: (String) -> Unit,
    onOpenProfile: (Int) -> Unit
) {
    val feedState by viewModel.feedState.collectAsState()
    var reportingPostId by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize()) {
        /*
         * 🔴 "Tümü / Resmi / Topluluk" HAP SATIRI KALDIRILDI (urun sahibi
         * karari). Ust sekmelerin hemen altinda ikinci bir hap seridi
         * vardi ve ekranin ustu iki sira dugmeye donusuyordu; hangisinin
         * neyi filtreledigi de belirsizdi.
         *
         * Filtreleme kaybolmadi: `viewModel.selectType` duruyor ve derin
         * baglantidan hala kullanilabiliyor. Yalnizca akisin ustunden
         * kalkti.
         */

        when (val s = feedState) {
            is CommunityViewModel.FeedUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LkPrimary)
                }
            }
            is CommunityViewModel.FeedUiState.Error -> {
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(s.message, color = LkDanger, style = LkTypography.getBody())
                    Spacer(Modifier.height(12.dp))
                    LkButton(text = "Tekrar Dene", variant = LkButtonVariant.SECONDARY, onClick = { viewModel.refreshFeed() })
                }
            }
            is CommunityViewModel.FeedUiState.Content -> {
                if (s.posts.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Henüz gönderi bulunmuyor", style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "İlk gönderini paylaş, topluluğa katkıda bulun.",
                                style = LkTypography.getBodySmall(),
                                color = LkTextSecondary
                            )
                            Spacer(Modifier.height(16.dp))
                            LkButton(text = "Paylaşım Yap", onClick = { viewModel.startCompose() })
                        }
                    }
                } else {
                    Box(Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            /*
                             * GÜNDEMDE — webin sag serit kartinin karsiligi
                             * (`CommunityPage.jsx:1082`, `feed.rail.trending`).
                             *
                             * Webde de sunucu ucu YOK: akisin ilk dort gonderisi
                             * numaralanip gosteriliyor (`posts.slice(0,4)`).
                             * Mobilde de ayni sekilde turetiliyor — uydurma bir
                             * "populerlik" siralamasi hesaplanmiyor.
                             *
                             * Dort gonderiden az varsa hic gosterilmiyor: iki
                             * gonderilik bir "gundem" listesi, altindaki akisin
                             * kopyasindan baska bir sey olmazdi.
                             */
                            if (s.posts.size >= 4) {
                                item {
                                    GundemdeKarti(
                                        posts = s.posts.take(4),
                                        onOpenPost = onOpenPost
                                    )
                                }
                            }

                            items(s.posts, key = { it.id }) { post ->
                                PostFeedCard(
                                    post = post,
                                    onClick = { onOpenPost(post.id) },
                                    onAuthorClick = { post.author?.id?.let(onOpenProfile) },
                                    onLike = { viewModel.toggleLike(post.id, post.begendim) },
                                    onBookmark = { viewModel.toggleBookmark(post.id, post.kaydettim) },
                                    onQuote = { viewModel.startCompose(quoteOf = post) },
                                    onReply = { viewModel.startCompose(replyTo = post) },
                                    onReport = { reportingPostId = post.id }
                                )
                            }
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (s.loadingMore) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = LkPrimary)
                                    } else {
                                        LkButton(
                                            text = "Daha Fazla Yükle",
                                            variant = LkButtonVariant.SECONDARY,
                                            onClick = { viewModel.loadMore() },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }

                        // FAB to create post
                        FloatingActionButton(
                            onClick = { viewModel.startCompose() },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(20.dp),
                            backgroundColor = LkPrimary
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Gönderi Oluştur",
                                tint = LkOnPrimary
                            )
                        }
                    }
                }
            }
        }
    }

    // Report dialog
    reportingPostId?.let { postId ->
        PostReportDialog(
            onDismiss = { reportingPostId = null },
            onSubmit = { reason, details ->
                viewModel.reportPost(postId, reason, details)
                reportingPostId = null
            }
        )
    }
}

@Composable
fun PostFeedCard(
    post: CommunityPostDto,
    onClick: () -> Unit,
    onAuthorClick: () -> Unit,
    onLike: () -> Unit,
    onBookmark: () -> Unit,
    onQuote: () -> Unit,
    onReply: () -> Unit,
    onReport: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        backgroundColor = LkSurfacePanel,
        shape = LkShapes.MD,
        elevation = 0.dp
    ) {
        Column(Modifier.padding(14.dp)) {
            // Author Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                /*
                 * Resmi gonderilerde marka isareti "LK" kaliyor ve fotograf
                 * cizilmiyor: o gonderiler bir kisiye degil urune ait.
                 */
                val resmi = post.postType == "official"
                LkAvatar(
                    ad = post.author?.name,
                    avatarUrl = if (resmi) null else post.author?.avatarUrl,
                    boyut = 36.dp,
                    zemin = if (resmi) LkPrimary else LkPrimarySoft,
                    harfRengi = if (resmi) LkOnPrimary else LkPrimary,
                    harfOverride = if (resmi) "LK" else null,
                    modifier = Modifier.clickable(onClick = onAuthorClick)
                )

                Spacer(Modifier.width(10.dp))

                Column(Modifier.weight(1f).clickable(onClick = onAuthorClick)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (post.postType == "official") "Resmi Duyuru" else (post.author?.name ?: "Bilinmeyen"),
                            style = LkTypography.getBodyStrong(),
                            color = LkTextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (post.postType == "official") {
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Outlined.Verified, contentDescription = "Resmi", tint = LkPrimary, modifier = Modifier.size(14.dp))
                        }
                    }
                    Text(
                        post.publishedAt?.let { LkDateUtils.formatDateTime(it) } ?: "",
                        style = LkTypography.getMicro(),
                        color = LkTextMuted
                    )
                }

                if (onReport != null && post.postType != "official") {
                    IconButton(onClick = onReport, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "Seçenekler", tint = LkTextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Post Text
            Text(
                post.summary,
                style = LkTypography.getBody(),
                color = LkTextPrimary,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )

            /*
             * EKLI MEDYA.
             *
             * 🔴 GORSEL HIC CIZILMIYORDU: `CommunityMediaDto.url` sunucudan
             * geliyor ama ekranda yalnizca dosya ADI gosteriliyordu, cunku
             * projede goruntu yukleme kutuphanesi yoktu. Coil 3 eklendikten
             * sonra (bkz. LkRemoteImage) gorsel gercekten cizilebiliyor.
             *
             * Gorsel olmayan ek (PDF vb.) icin dosya seridi KALIYOR — onu
             * cizecek bir sey yok, adi tek anlamli bilgi.
             */
            post.media?.let { media ->
                Spacer(Modifier.height(10.dp))
                if (media.kind == "image" && !media.url.isNullOrBlank()) {
                    LkRemoteImage(
                        url = media.url,
                        contentDescription = media.originalName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 10f)
                            .clip(LkShapes.Card)
                    ) {
                        /* Yuklenirken / gelmezse: bos kutu degil, desenli zemin. */
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(LkSurfaceTile),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Image,
                                contentDescription = null,
                                tint = LkTileInk,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(LkShapes.MD)
                            .background(LkSurfaceTile)
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.InsertDriveFile,
                                contentDescription = null,
                                tint = LkTileInk,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                media.originalName ?: "Ekli dosya",
                                style = LkTypography.getMetadata(),
                                color = LkTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Quoted Post Card
            post.quotedPost?.let { q ->
                Spacer(Modifier.height(8.dp))
                QuotedPostCard(quotedPost = q, onClick = onClick)
            }

            Spacer(Modifier.height(12.dp))

            // Interaction Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like
                Row(
                    modifier = Modifier
                        .clip(LkShapes.SM)
                        .clickable(onClick = onLike)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (post.begendim) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Beğen",
                        tint = if (post.begendim) LkDanger else LkTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${post.begeniSayisi}",
                        style = LkTypography.getMicro(),
                        color = if (post.begendim) LkDanger else LkTextSecondary
                    )
                }

                // Reply
                Row(
                    modifier = Modifier
                        .clip(LkShapes.SM)
                        .clickable(onClick = onReply)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Yanıtla",
                        tint = LkTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${post.yanitSayisi}",
                        style = LkTypography.getMicro(),
                        color = LkTextSecondary
                    )
                }

                // Quote
                Row(
                    modifier = Modifier
                        .clip(LkShapes.SM)
                        .clickable(onClick = onQuote)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FormatQuote,
                        contentDescription = "Alıntıla",
                        tint = LkTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${post.alintiSayisi}",
                        style = LkTypography.getMicro(),
                        color = LkTextSecondary
                    )
                }

                // Bookmark
                IconButton(onClick = onBookmark, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (post.kaydettim) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Kaydet",
                        tint = if (post.kaydettim) LkPrimary else LkTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
/**
 * Gundemde karti — webin sag seridindeki `trending` kartinin karsiligi.
 *
 * Sira numarasi gonderinin ONEMINI degil, listedeki YERINI gosteriyor;
 * webde de oyle. Bir "populerlik puani" hesaplanmiyor cunku sunucu boyle
 * bir sey vermiyor ve uydurmak kullaniciya yanlis bir sinyal olurdu.
 */
@Composable
private fun GundemdeKarti(
    posts: List<CommunityPostDto>,
    onOpenPost: (String) -> Unit
) {
    LkCard(padding = LkSpacing.Space4) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.TrendingUp,
                contentDescription = null,
                tint = LkTileInk,
                modifier = Modifier.size(19.dp)
            )
            Spacer(Modifier.width(LkSpacing.Space2))
            Text("Gündemde", style = LkTypography.getTitleS(), color = LkTextPrimary)
        }
        Spacer(Modifier.height(LkSpacing.Space3))

        posts.forEachIndexed { i, post ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenPost(post.id) }
                    .padding(vertical = LkSpacing.Space2),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = post.title?.takeIf { it.isNotBlank() }
                        ?: post.summary.takeIf { it.isNotBlank() }
                        ?: "Paylaşım",
                    style = LkTypography.getBodySmall(),
                    color = LkTextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(LkSpacing.Space3))
                Text(
                    text = "${i + 1}",
                    style = LkTypography.getTitleS(),
                    color = LkTextMuted
                )
            }
            if (i != posts.lastIndex) LkHairline()
        }
    }
}

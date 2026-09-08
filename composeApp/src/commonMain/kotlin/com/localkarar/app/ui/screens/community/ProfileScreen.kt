package com.localkarar.app.ui.screens.community

import com.localkarar.app.ui.components.LkLoadingDesen
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkCoverHeader
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.auth.UserDto
import com.localkarar.app.community.CommunityViewModel
import com.localkarar.app.community.SocialViewModel
import com.localkarar.app.core.openExternalUrl
import com.localkarar.app.network.dto.CommunityPostDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkButtonSize
import com.localkarar.app.ui.theme.*

/**
 * SOSYAL PROFIL — kendi profilim ve baskasininki.
 *
 * 🔴 UC AYRI KUSUR AYNI EKRANDAYDI:
 *
 * 1. IKI MAVI BANT. Ekran `LkHeroPage` icindeydi: once hero'nun mavi
 *    basligi, hemen altinda kapak basliginin mavi bandi. Ustelik hero
 *    "Profil" yaziyordu, kapak da adi — ayni sey iki kere.
 *    Cozum: profilde BASLIK KAPAKTIR. Geri oku kapagin uzerinde yuzuyor.
 *
 * 2. KENDI PROFILIMDE KAPAK DA AVATAR DA YOKTU. `OwnProfileContent`
 *    dogrudan sayac satiriyla basliyordu; kimlik hicbir yerde
 *    gorunmuyordu. Web (`ProfilePage.jsx`) kendi profilinde de ayni
 *    kapak/avatar/ad/bio blogunu ciziyor ve bilgileri oturumdaki
 *    `user`dan aliyor — mobil de artik oyle yapiyor.
 *
 * 3. KENDI KIMLIGIMLE ACILAN PROFIL "BASKASI" SAYILIYORDU. Akista kendi
 *    adima dokununca `CommunityProfile(kendi id)` aciliyor ve ekran
 *    yabanci profil dalina giriyordu: takip/engelle dugmeleri kendime
 *    cikiyordu. Web `benimMi = !userId || userId === user.id` diyor;
 *    mobil de artik ayni karari veriyor.
 */
@Composable
fun ProfileScreen(
    userId: Int? = null, // null = own profile, non-null = other user profile
    currentUser: UserDto,
    socialViewModel: SocialViewModel,
    communityViewModel: CommunityViewModel,
    onBack: (() -> Unit)? = null,
    onOpenFollowers: (Int, String) -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenProfile: (Int) -> Unit,
    onEditProfile: () -> Unit = {},
    /** Kendi profilimde kimlik blogunun ALTINA giren ek satirlar. */
    altBlok: (@Composable () -> Unit)? = null
) {
    val isOwnProfile = userId == null || userId == currentUser.id
    val ownState by socialViewModel.ownProfileState.collectAsState()
    val otherState by socialViewModel.otherProfileState.collectAsState()

    var showReportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (isOwnProfile) {
            socialViewModel.loadOwnProfile(currentUser.id)
        } else {
            socialViewModel.loadOtherProfile(userId!!)
        }
    }

    Box(Modifier.fillMaxSize().background(LkSurfaceCanvas)) {
        if (isOwnProfile) {
            when (val s = ownState) {
                is SocialViewModel.OwnProfileUiState.Loading -> {
                    LkLoadingState(desen = LkLoadingDesen.DETAY)
                }
                is SocialViewModel.OwnProfileUiState.Error -> {
                    ProfilHatasi(s.message) { socialViewModel.loadOwnProfile(currentUser.id) }
                }
                is SocialViewModel.OwnProfileUiState.Content -> {
                    OwnProfileContent(
                        state = s,
                        user = currentUser,
                        activeTab = socialViewModel.ownProfileTab,
                        onTabSelect = { socialViewModel.ownProfileTab = it },
                        onOpenFollowers = { onOpenFollowers(currentUser.id, it) },
                        onOpenPost = onOpenPost,
                        onEditProfile = onEditProfile,
                        altBlok = altBlok,
                        onLike = { pId, liked -> communityViewModel.toggleLike(pId, liked) },
                        onBookmark = { pId, saved -> communityViewModel.toggleBookmark(pId, saved) },
                        onQuote = { p -> communityViewModel.startCompose(quoteOf = p) },
                        onReply = { p -> communityViewModel.startCompose(replyTo = p) }
                    )
                }
            }
        } else {
            when (val s = otherState) {
                is SocialViewModel.OtherProfileUiState.Loading, SocialViewModel.OtherProfileUiState.Idle -> {
                    LkLoadingState(desen = LkLoadingDesen.DETAY)
                }
                is SocialViewModel.OtherProfileUiState.Error -> {
                    ProfilHatasi(s.message) { socialViewModel.loadOtherProfile(userId!!) }
                }
                is SocialViewModel.OtherProfileUiState.Content -> {
                    OtherProfileContent(
                        state = s,
                        isFollowing = socialViewModel.followingIds.contains(s.profile.id),
                        isBlocked = socialViewModel.blockedIds.contains(s.profile.id),
                        activeTab = socialViewModel.otherProfileTab,
                        onTabSelect = { socialViewModel.otherProfileTab = it },
                        onToggleFollow = { socialViewModel.toggleFollow(s.profile.id) },
                        onToggleBlock = { socialViewModel.toggleBlock(s.profile.id) },
                        onReport = { showReportDialog = true },
                        onOpenFollowers = { onOpenFollowers(s.profile.id, it) },
                        onOpenPost = onOpenPost,
                        onLike = { pId, liked -> communityViewModel.toggleLike(pId, liked) },
                        onBookmark = { pId, saved -> communityViewModel.toggleBookmark(pId, saved) },
                        onQuote = { p -> communityViewModel.startCompose(quoteOf = p) },
                        onReply = { p -> communityViewModel.startCompose(replyTo = p) }
                    )
                }
            }
        }

        /*
         * Geri oku kapagin UZERINDE. Kendi basina bir baslik cubugu
         * kurmuyoruz: kapak zaten baslik. Koyu yari saydam daire, acik
         * fotograflarda da okunur kalmasi icin.
         */
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .padding(LkSpacing.Space2)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x59000000))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.ArrowBack,
                    contentDescription = "Geri",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (showReportDialog && userId != null) {
        UserReportDialog(
            onDismiss = { showReportDialog = false },
            onSubmit = { reason, details ->
                socialViewModel.reportUser(userId, reason, details)
                showReportDialog = false
            }
        )
    }
}

@Composable
private fun ProfilHatasi(mesaj: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(mesaj, color = LkDanger, style = LkTypography.getBody())
        Spacer(Modifier.height(12.dp))
        LkButton(text = "Tekrar Dene", variant = LkButtonVariant.SECONDARY, onClick = onRetry)
    }
}

/**
 * Kapak + avatar + kimlik blogu. Iki profil dali da AYNI bileşeni
 * kullaniyor; fark yalniz altindaki eylem satiri.
 */
@Composable
private fun ProfilBasligi(
    ad: String,
    kapakUrl: String?,
    avatarUrl: String?,
    rol: String?,
    bio: String?,
    konum: String?,
    site: String?,
    paylasim: Int,
    takipci: Int,
    takipEdilen: Int,
    onSayacPaylasim: () -> Unit,
    onOpenFollowers: (String) -> Unit,
    /**
     * Sayaclarin altindaki ikincil eylemler (engelle, sikayet).
     *
     * ⚠️ `eylemler`den ONCE duruyor: sondaki lambda parametresi Kotlin'de
     * her zaman SON parametreye baglanir, bu yuzden birincil eylem en
     * sonda olmali.
     */
    altEylemler: (@Composable () -> Unit)? = null,
    /** Adin yanindaki BIRINCIL eylem (duzenle / takip et). */
    eylemler: @Composable () -> Unit
) {
    LkCoverHeader(ad = ad, kapakUrl = kapakUrl, avatarUrl = avatarUrl) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            /*
             * Ad ve BIRINCIL EYLEM AYNI SATIRDA — foydeki "Profil"
             * ekraninda "Profili düzenle" adin sagindaki dugme. Onceden
             * eylem en altta tam genislikte duruyordu ve kimlik blogunu
             * ikiye boluyordu.
             */
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(ad, style = LkTypography.getTitleL(), color = LkTextPrimary)
                    rol?.let { Text(it, style = LkTypography.getMicro(), color = LkPrimary) }
                }
                Spacer(Modifier.width(12.dp))
                eylemler()
            }

            if (!bio.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(bio, style = LkTypography.getBodySmall(), color = LkTextSecondary)
            }

            if (!konum.isNullOrBlank() || !site.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!konum.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = LkTextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(konum, style = LkTypography.getMicro(), color = LkTextMuted)
                        }
                        Spacer(Modifier.width(12.dp))
                    }
                    if (!site.isNullOrBlank()) {
                        Row(
                            modifier = Modifier.clickable { openExternalUrl(site) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Link,
                                contentDescription = null,
                                tint = LkPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            /* Webdeki gibi sema kirpiliyor; adres yine tam
                               haliyle aciliyor. */
                            Text(
                                site.replace(Regex("^https?://", RegexOption.IGNORE_CASE), ""),
                                style = LkTypography.getMicro(),
                                color = LkPrimary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            /*
             * SAYACLAR TEK SATIRDA VE METIN GIBI — foydeki gibi
             * "31 paylaşım · 248 takipçi · 86 takip". Onceden uc sutuna
             * yayilmis buyuk rakamlardi; profilin hakim ogesi kimlik
             * olmali, sayilar degil.
             */
            Row(verticalAlignment = Alignment.CenterVertically) {
                SayacMetni(paylasim, "paylaşım", onSayacPaylasim)
                SayacAyraci()
                SayacMetni(takipci, "takipçi") { onOpenFollowers("followers") }
                SayacAyraci()
                SayacMetni(takipEdilen, "takip") { onOpenFollowers("following") }
            }

            if (altEylemler != null) {
                Spacer(Modifier.height(12.dp))
                altEylemler()
            }
        }
    }
}

/** "31 paylaşım" — kalin sayi + sonuk etiket, tek dokunma hedefi. */
@Composable
private fun SayacMetni(sayi: Int, etiket: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(LkShapes.SM)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = sayi.toString(),
            style = LkTypography.getBodyStrong(),
            color = LkTextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(4.dp))
        Text(etiket, style = LkTypography.getBodySmall(), color = LkTextSecondary)
    }
}

@Composable
private fun SayacAyraci() {
    Text(
        text = "·",
        style = LkTypography.getBodySmall(),
        color = LkTextMuted,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@Composable
private fun OwnProfileContent(
    state: SocialViewModel.OwnProfileUiState.Content,
    user: UserDto,
    activeTab: String,
    onTabSelect: (String) -> Unit,
    onOpenFollowers: (String) -> Unit,
    onOpenPost: (String) -> Unit,
    onEditProfile: () -> Unit,
    altBlok: (@Composable () -> Unit)? = null,
    onLike: (String, Boolean) -> Unit,
    onBookmark: (String, Boolean) -> Unit,
    onQuote: (CommunityPostDto) -> Unit,
    onReply: (CommunityPostDto) -> Unit
) {
    val posts = when (activeTab) {
        "media" -> state.media
        "likes" -> state.likes
        "bookmarks" -> state.bookmarks
        else -> state.posts
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            ProfilBasligi(
                ad = user.name,
                kapakUrl = user.coverUrl,
                avatarUrl = user.avatarUrl,
                rol = null,
                bio = user.bio,
                konum = user.location,
                site = user.websiteUrl,
                paylasim = state.summary.paylasim,
                takipci = state.summary.takipci,
                takipEdilen = state.summary.takipEdilen,
                onSayacPaylasim = { onTabSelect("posts") },
                onOpenFollowers = onOpenFollowers
            ) {
                /* Duzenleme webde de ayri bir panel; mobilde zaten var olan
                   Ayarlar > Profil ekranina goturuyor (kapak ve avatar
                   degistirme orada). Ikinci bir duzenleme formu acmak ayni
                   isi iki yerde tutmak olurdu. */
                LkButton(
                    text = "Profili düzenle",
                    variant = LkButtonVariant.SECONDARY,
                    size = LkButtonSize.SM,
                    icon = Icons.Outlined.Edit,
                    onClick = onEditProfile
                )
            }

            altBlok?.invoke()

            ProfilSekmeleri(
                sekmeler = listOf(
                    ProfilSekme("posts", "Paylaşımlarım", state.summary.paylasim),
                    ProfilSekme("media", "Medya", null),
                    ProfilSekme("likes", "Beğenilerim", state.summary.begeni),
                    ProfilSekme("bookmarks", "Kaydettiklerim", state.summary.kayit)
                ),
                aktif = activeTab,
                onSecim = onTabSelect
            )
        }

        if (posts.isEmpty()) {
            item {
                BosListe(
                    when (activeTab) {
                        "media" -> "Görsel içeren bir paylaşımınız yok."
                        "likes" -> "Henüz beğendiğiniz bir gönderi yok."
                        "bookmarks" -> "Henüz kaydettiğiniz bir gönderi yok."
                        else -> "Henüz bir paylaşım yapmadınız."
                    }
                )
            }
        } else {
            items(posts, key = { it.id }) { post ->
                Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    PostFeedCard(
                        post = post,
                        onClick = { onOpenPost(post.id) },
                        onAuthorClick = {},
                        onLike = { onLike(post.id, post.begendim) },
                        onBookmark = { onBookmark(post.id, post.kaydettim) },
                        onQuote = { onQuote(post) },
                        onReply = { onReply(post) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OtherProfileContent(
    state: SocialViewModel.OtherProfileUiState.Content,
    isFollowing: Boolean,
    isBlocked: Boolean,
    activeTab: String,
    onTabSelect: (String) -> Unit,
    onToggleFollow: () -> Unit,
    onToggleBlock: () -> Unit,
    onReport: () -> Unit,
    onOpenFollowers: (String) -> Unit,
    onOpenPost: (String) -> Unit,
    onLike: (String, Boolean) -> Unit,
    onBookmark: (String, Boolean) -> Unit,
    onQuote: (CommunityPostDto) -> Unit,
    onReply: (CommunityPostDto) -> Unit
) {
    val posts = if (activeTab == "media") state.mediaPosts else state.posts
    val profile = state.profile
    val sayilar = state.sayilar

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            ProfilBasligi(
                ad = profile.name,
                kapakUrl = profile.coverUrl,
                avatarUrl = profile.avatarUrl,
                rol = profile.role,
                bio = profile.bio,
                konum = profile.location,
                site = profile.websiteUrl,
                paylasim = sayilar.paylasim,
                takipci = sayilar.takipci,
                takipEdilen = sayilar.takipEdilen,
                onSayacPaylasim = { onTabSelect("posts") },
                onOpenFollowers = onOpenFollowers,
                /*
                 * Engelleme ve sikayet IKINCIL: ad satirinda uc dugme yan
                 * yana sigmiyordu ve "Engelle" takip dugmesi kadar one
                 * cikiyordu.
                 */
                altEylemler = if (isBlocked) null else {
                    {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LkButton(
                                text = "Engelle",
                                variant = LkButtonVariant.GHOST,
                                size = LkButtonSize.SM,
                                onClick = onToggleBlock
                            )
                            LkButton(
                                text = "Şikayet et",
                                variant = LkButtonVariant.GHOST,
                                size = LkButtonSize.SM,
                                icon = Icons.Outlined.Flag,
                                onClick = onReport
                            )
                        }
                    }
                }
            ) {
                /* Adin yanindaki BIRINCIL eylem: takip. */
                LkButton(
                    text = when {
                        isBlocked -> "Engeli Kaldır"
                        isFollowing -> "Takip Ediliyor"
                        else -> "Takip Et"
                    },
                    variant = if (isFollowing || isBlocked) LkButtonVariant.SECONDARY
                    else LkButtonVariant.PRIMARY,
                    size = LkButtonSize.SM,
                    onClick = if (isBlocked) onToggleBlock else onToggleFollow
                )
            }

            ProfilSekmeleri(
                sekmeler = listOf(
                    ProfilSekme("posts", "Paylaşımlar", sayilar.paylasim),
                    ProfilSekme("media", "Medya", null)
                ),
                aktif = activeTab,
                onSecim = onTabSelect
            )
        }

        if (posts.isEmpty()) {
            item {
                BosListe(
                    if (activeTab == "media") "Görsel içeren paylaşım yok."
                    else "Henüz paylaşım bulunmuyor."
                )
            }
        } else {
            items(posts, key = { it.id }) { post ->
                Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    PostFeedCard(
                        post = post,
                        onClick = { onOpenPost(post.id) },
                        onAuthorClick = {},
                        onLike = { onLike(post.id, post.begendim) },
                        onBookmark = { onBookmark(post.id, post.kaydettim) },
                        onQuote = { onQuote(post) },
                        onReply = { onReply(post) }
                    )
                }
            }
        }
    }
}

private data class ProfilSekme(val anahtar: String, val etiket: String, val sayi: Int?)

/**
 * Sekme seridi.
 *
 * ⚠️ `LazyRow` — `Row` DEGIL. Kendi profilimde dort sekme var; sabit bir
 * satirda en dar telefonda son sekme ekran disinda kaliyordu.
 */
@Composable
private fun ProfilSekmeleri(
    sekmeler: List<ProfilSekme>,
    aktif: String,
    onSecim: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sekmeler) { sekme ->
            ProfileTabPill(
                title = sekme.etiket,
                count = sekme.sayi,
                isSelected = aktif == sekme.anahtar,
                onClick = { onSecim(sekme.anahtar) }
            )
        }
    }
}

@Composable
private fun BosListe(mesaj: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(mesaj, style = LkTypography.getBodySmall(), color = LkTextMuted)
    }
}

@Composable
private fun StatCounter(
    count: Int,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(LkShapes.SM)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("$count", style = LkTypography.getSectionTitle(), color = LkTextPrimary, fontWeight = FontWeight.Bold)
        Text(label, style = LkTypography.getMicro(), color = LkTextSecondary)
    }
}

@Composable
private fun ProfileTabPill(
    title: String,
    count: Int? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val text = if (count != null) "$title ($count)" else title
    Box(
        modifier = Modifier
            .clip(LkShapes.MD)
            .background(if (isSelected) LkPrimarySoft else LkSurfaceSunken)
            .border(1.dp, if (isSelected) LkPrimary else LkLineSoft, LkShapes.MD)
            .clickable(onClick = onClick)
            .heightIn(min = 44.dp)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = LkTypography.getMicro(),
            color = if (isSelected) LkPrimary else LkTextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

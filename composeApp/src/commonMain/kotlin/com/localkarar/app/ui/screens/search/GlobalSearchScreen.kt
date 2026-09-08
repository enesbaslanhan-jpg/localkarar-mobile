package com.localkarar.app.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Rule
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.localkarar.app.search.GlobalSearchViewModel
import com.localkarar.app.ui.components.LkAvatar
import com.localkarar.app.ui.components.LkCard
import com.localkarar.app.ui.components.LkEmptyState
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.components.LkLoadingDesen
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*

/**
 * GENEL ARAMA EKRANI.
 *
 * 🔴 Mobilde YOKTU. Web ust cubuktan tek kutuyla arattiriyor; telefonda
 * ust cubuk yok, o yuzden Ana Sayfa'daki arama simgesinden acilan TAM
 * EKRAN bir arama var. Bulunanlar webdeki GRUPLARLA ve webdeki SIRAYLA
 * listeleniyor: kisiler → paylasimlar → kurslar → karar araclari →
 * hesaplamalar → haberler.
 *
 * Webin sirasindaki gerekce kodunda yaziyor: arama kutusuna bir AD
 * yazan kullanici once kisiyi gormeli.
 *
 * ⚠️ Haber sonucu HABERLER LISTESINE goturuyor, tek habere degil --
 * web de oyle yapiyor (`go('/app/community')`). Sunucu arama
 * sonucunda haberin kimligini donuyor ama webin gittigi yer liste;
 * mobilde tek habere gitmek webde olmayan bir davranis olurdu.
 */
@Composable
fun GlobalSearchScreen(
    viewModel: GlobalSearchViewModel,
    onBack: () -> Unit,
    onOpenProfile: (Int) -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenCourse: (Int) -> Unit,
    onOpenDecisionTool: (String) -> Unit,
    onOpenFormula: (String) -> Unit,
    onOpenNews: () -> Unit
) {
    val durum by viewModel.durum.collectAsState()
    var metin by remember { mutableStateOf(viewModel.terim) }

    LkHeroPage(title = "Ara", onBack = onBack) {
        Column(Modifier.fillMaxSize()) {
            LkTextField(
                value = metin,
                onValueChange = {
                    metin = it
                    viewModel.terimDegisti(it)
                },
                placeholder = "Kişi, paylaşım, kurs, araç ya da haber",
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(LkSpacing.Space4)
            )

            /*
             * ⚠️ `weight(1f)` — `fillMaxSize()` DEGIL.
             *
             * `Column` icinde `fillMaxSize()` KALAN degil TUM
             * yuksekligi ister; arama kutusunun altindaki liste ekrandan
             * tasar ve tasan kisim kaydirilarak da getirilemez, cunku
             * listenin kendi gorunum alani zaten ekrandan buyuktur.
             * Ayni tuzak Ana Sayfa'da bir kez yasandi (§24 notu).
             */
            Box(Modifier.weight(1f).fillMaxWidth()) {
            when (val d = durum) {
                is GlobalSearchViewModel.Durum.Bekliyor -> LkEmptyState(
                    title = "Ne arıyorsun?",
                    /* Iki harf kurali SOYLENIYOR: kullanici tek harf
                       yazip "arama bozuk" diye dusunmesin. */
                    description = "En az iki harf yaz; kişiler, paylaşımlar, kurslar, karar araçları, hesaplamalar ve haberler birlikte aranır.",
                    icon = Icons.Outlined.SearchOff
                )

                is GlobalSearchViewModel.Durum.Araniyor ->
                    LkLoadingState(desen = LkLoadingDesen.LISTE)

                is GlobalSearchViewModel.Durum.Hata -> LkErrorState(
                    message = d.mesaj,
                    onRetry = { viewModel.tekrarDene() }
                )

                is GlobalSearchViewModel.Durum.Icerik -> {
                    val s = d.sonuclar
                    if (s.bosMu) {
                        LkEmptyState(
                            title = "Sonuç yok",
                            description = "Başka bir kelime deneyebilirsin.",
                            icon = Icons.Outlined.SearchOff
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = LkSpacing.Space4,
                                end = LkSpacing.Space4,
                                bottom = 88.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)
                        ) {
                            if (s.sunucu.people.isNotEmpty()) {
                                item { GrupBasligi(Icons.Outlined.People, "KİŞİLER") }
                                items(s.sunucu.people.size) { i ->
                                    val kisi = s.sunucu.people[i]
                                    SonucSatiri(
                                        baslik = kisi.name,
                                        altBaslik = kisi.bio?.takeIf { it.isNotBlank() }
                                            ?: "LocalKarar üyesi",
                                        avatarAdi = kisi.name,
                                        avatarUrl = kisi.avatarUrl,
                                        onClick = { onOpenProfile(kisi.id) }
                                    )
                                }
                            }

                            if (s.sunucu.posts.isNotEmpty()) {
                                item { GrupBasligi(Icons.Outlined.ChatBubbleOutline, "PAYLAŞIMLAR") }
                                items(s.sunucu.posts.size) { i ->
                                    val gonderi = s.sunucu.posts[i]
                                    SonucSatiri(
                                        baslik = gonderi.ozet,
                                        altBaslik = gonderi.author?.name ?: "LocalKarar kullanıcısı",
                                        onClick = { onOpenPost(gonderi.id) }
                                    )
                                }
                            }

                            if (s.sunucu.courses.isNotEmpty()) {
                                item { GrupBasligi(Icons.Outlined.MenuBook, "KURSLAR") }
                                items(s.sunucu.courses.size) { i ->
                                    val kurs = s.sunucu.courses[i]
                                    SonucSatiri(
                                        baslik = kurs.title,
                                        /* Alan yoksa TIRE degil, olan alan
                                           yaziliyor; ikisi de yoksa satir
                                           alt basliksiz kaliyor. */
                                        altBaslik = listOfNotNull(kurs.category, kurs.level)
                                            .filter { it.isNotBlank() }
                                            .joinToString(" · ")
                                            .ifBlank { null },
                                        onClick = { onOpenCourse(kurs.id) }
                                    )
                                }
                            }

                            if (s.sunucu.decisionChecks.isNotEmpty()) {
                                item { GrupBasligi(Icons.Outlined.Rule, "KARAR ARAÇLARI") }
                                items(s.sunucu.decisionChecks.size) { i ->
                                    val arac = s.sunucu.decisionChecks[i]
                                    SonucSatiri(
                                        baslik = arac.title,
                                        altBaslik = arac.description?.takeIf { it.isNotBlank() }
                                            ?: arac.code,
                                        onClick = { onOpenDecisionTool(arac.code) }
                                    )
                                }
                            }

                            if (s.hesaplamalar.isNotEmpty()) {
                                item { GrupBasligi(Icons.Outlined.Calculate, "HESAPLAMALAR") }
                                items(s.hesaplamalar.size) { i ->
                                    val formul = s.hesaplamalar[i]
                                    SonucSatiri(
                                        baslik = formul.name,
                                        altBaslik = formul.category?.takeIf { it.isNotBlank() },
                                        onClick = { onOpenFormula(formul.id) }
                                    )
                                }
                            }

                            if (s.sunucu.news.isNotEmpty()) {
                                item { GrupBasligi(Icons.Outlined.Newspaper, "HABERLER") }
                                items(s.sunucu.news.size) { i ->
                                    val haber = s.sunucu.news[i]
                                    SonucSatiri(
                                        baslik = haber.title,
                                        altBaslik = haber.category?.takeIf { it.isNotBlank() },
                                        onClick = onOpenNews
                                    )
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun GrupBasligi(ikon: ImageVector, metin: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = LkSpacing.Space3, bottom = LkSpacing.Space1)
    ) {
        Icon(ikon, contentDescription = null, tint = LkTextMuted, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(LkSpacing.Space2))
        Text(metin, style = LkTypography.getMicro(), color = LkTextMuted)
    }
}

@Composable
private fun SonucSatiri(
    baslik: String,
    altBaslik: String? = null,
    avatarAdi: String? = null,
    avatarUrl: String? = null,
    onClick: () -> Unit
) {
    LkCard(modifier = Modifier.clickable(onClick = onClick), padding = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                /* §19: dokunma hedefi en az 44dp. */
                .heightIn(min = 44.dp)
                .padding(LkSpacing.Space3),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (avatarAdi != null) {
                LkAvatar(ad = avatarAdi, avatarUrl = avatarUrl, boyut = 32.dp)
                Spacer(Modifier.width(LkSpacing.Space3))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = baslik,
                    style = LkTypography.getBodySmall(),
                    color = LkTextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (altBaslik != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = altBaslik,
                        style = LkTypography.getMicro(),
                        color = LkTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

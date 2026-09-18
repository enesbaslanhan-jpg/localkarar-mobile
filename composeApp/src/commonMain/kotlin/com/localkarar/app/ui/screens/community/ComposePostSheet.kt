package com.localkarar.app.ui.screens.community

import com.localkarar.app.ui.components.LkLoadingSpinner
import androidx.compose.ui.layout.ContentScale
import com.localkarar.app.ui.components.LkRemoteImage
import androidx.compose.foundation.layout.padding
import com.localkarar.app.ui.components.altBoslukla
import com.localkarar.app.ui.components.LocalAltBosluk
import com.localkarar.app.core.rememberCameraCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.localkarar.app.community.CommunityViewModel
import com.localkarar.app.auth.UserDto
import com.localkarar.app.ui.components.LkHairline
import com.localkarar.app.ui.components.LkAvatar
import com.localkarar.app.core.rememberFilePicker
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonSize
import com.localkarar.app.ui.theme.*

/*
 * PAYLASIM YAZMA — TAM EKRAN (urun sahibi, 18.09.2026: "Threads gibi acilmali").
 *
 * Onceden kucuk bir AlertDialog'du: 140dp'lik kutu, altinda dugmeler. Simdi
 * tam ekran bir sayfa: ustte Iptal / baslik, govdede avatar + ad + kenarliksiz
 * metin alani (odak otomatik, klavye acik), metnin altinda gorsel/belge ve
 * etiket simgeleri; en altta (klavyenin ustunde) sayac ve "Paylas" hap dugmesi.
 * Yanit ve alinti seritleri ayni yerde durur.
 *
 * Dialog kullanilmaya devam ediyor (gezinme yiginina girmiyor; kapaninca
 * kaldigin akis/detay ekrani oldugu gibi duruyor). usePlatformDefaultWidth=false
 * ile tam ekrana yayilir.
 */
@Composable
fun ComposePostSheet(
    viewModel: CommunityViewModel,
    currentUser: UserDto? = null
) {
    /* Fotograf cek (urun sahibi, 18.09.2026): dosya seciminin SOLUNDA; kamera yoksa cizilmez. */
    val fotografCek = rememberCameraCapture { cekilen ->
        if (cekilen != null) viewModel.onMediaSelected(cekilen.name, cekilen.bytes, "image/jpeg")
    }
    val filePicker = rememberFilePicker { picked ->
        if (picked != null) {
            val ext = picked.name.substringAfterLast('.', "").lowercase()
            val mimeType = when (ext) {
                "png" -> "image/png"
                "jpg", "jpeg" -> "image/jpeg"
                "mp4" -> "video/mp4"
                "webm" -> "video/webm"
                "pdf" -> "application/pdf"
                "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                else -> "application/octet-stream"
            }
            viewModel.onMediaSelected(picked.name, picked.bytes, mimeType)
        }
    }

    val replyTarget = viewModel.replyTargetPost
    val quoteTarget = viewModel.quoteTargetPost

    /* Etiket listesi akistan turetiliyor; webde de ayni kaynak. */
    val feedState by viewModel.feedState.collectAsState()
    val katkicilar = remember(feedState) {
        (feedState as? CommunityViewModel.FeedUiState.Content)
            ?.let { katkicilariCikar(it.posts) }
            .orEmpty()
    }
    var etiketAcik by remember { mutableStateOf(false) }
    val odak = remember { FocusRequester() }
    LaunchedEffect(Unit) { odak.requestFocus() }

    val title = when {
        replyTarget != null -> "Yanıt yaz"
        quoteTarget != null -> "Alıntı yap"
        else -> "Yeni paylaşım"
    }
    val gonderilebilir = viewModel.metinInput.isNotBlank() || viewModel.attachedMedia != null

    Dialog(
        onDismissRequest = { viewModel.dismissCompose() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LkSurfaceCanvas)
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
        ) {
            /* Ust cubuk: Iptal — baslik. Paylas altta (Threads duzeni). */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space3)
            ) {
                Text(
                    "İptal",
                    style = LkTypography.getBody(),
                    color = LkTextPrimary,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable { viewModel.dismissCompose() }
                        .padding(vertical = 6.dp)
                )
                Text(
                    title,
                    style = LkTypography.getBodyStrong(),
                    color = LkTextPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            LkHairline()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                .padding(bottom = LocalAltBosluk.current.calculateBottomPadding())
                    .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space4)
            ) {
                if (replyTarget != null) {
                    Text(
                        "Yanıtlanan: @${replyTarget.author?.name ?: "kullanıcı"}",
                        style = LkTypography.getMicro(),
                        color = LkPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(LkSpacing.Space3))
                }
                if (quoteTarget != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(LkShapes.MD)
                            .border(1.dp, LkLineSoft, LkShapes.MD)
                            .background(LkSurfaceSunken)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                "@${quoteTarget.author?.name ?: "kullanıcı"}",
                                style = LkTypography.getMicro(),
                                color = LkPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(quoteTarget.summary, style = LkTypography.getBodySmall(), color = LkTextSecondary, maxLines = 3)
                        }
                    }
                    Spacer(Modifier.height(LkSpacing.Space3))
                }

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    LkAvatar(ad = currentUser?.name ?: "", avatarUrl = currentUser?.avatarUrl, boyut = 40.dp)
                    Spacer(Modifier.width(LkSpacing.Space3))
                    Column(Modifier.weight(1f)) {
                        Text(
                            currentUser?.name?.ifBlank { null } ?: "Sen",
                            style = LkTypography.getBodyStrong(),
                            color = LkTextPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        /* Kenarliksiz alan: Threads gibi metin dogrudan sayfada. */
                        Box(Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)) {
                            if (viewModel.metinInput.isEmpty()) {
                                Text(
                                    if (quoteTarget != null) "Düşüncelerini ekle…" else "Ne paylaşmak istersin?",
                                    style = LkTypography.getBody(),
                                    color = LkTextMuted
                                )
                            }
                            BasicTextField(
                                value = viewModel.metinInput,
                                onValueChange = { if (it.length <= 500) viewModel.onMetinChange(it) },
                                textStyle = LkTypography.getBody().copy(color = LkTextPrimary),
                                cursorBrush = SolidColor(LkPrimary),
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                                modifier = Modifier.fillMaxWidth().focusRequester(odak)
                            )
                        }
                        Spacer(Modifier.height(LkSpacing.Space2))

                        /* Ekleme simgeleri — metnin hemen altinda. */
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val eklenebilir = !viewModel.isUploadingMedia && viewModel.attachedMedia == null
                            if (fotografCek != null) {
                                IconButton(onClick = { fotografCek() }, enabled = eklenebilir) {
                                    Icon(Icons.Outlined.PhotoCamera, contentDescription = "Fotoğraf çek", tint = LkTextSecondary)
                                }
                            }
                            IconButton(onClick = { filePicker() }, enabled = eklenebilir) {
                                if (viewModel.isUploadingMedia) LkLoadingSpinner(size = 18.dp)
                                else Icon(Icons.Outlined.Image, contentDescription = "Görsel / video / belge ekle", tint = LkTextSecondary)
                            }
                            /*
                             * Etiket dugmesi HER ZAMAN gorunur (urun sahibi, 18.09.2026). Onceden
                             * akista baskasi yoksa hic cizilmiyordu ve "etiket yok" saniliyordu.
                             * Kisi listesi bossa metne "@" eklenir; kullanici adi kendisi yazar.
                             */
                            IconButton(onClick = {
                                if (katkicilar.isNotEmpty()) etiketAcik = !etiketAcik
                                else viewModel.onMetinChange(viewModel.metinInput.let { if (it.isEmpty() || it.endsWith(" ")) it + "@" else "$it @" })
                            }) {
                                Icon(
                                    Icons.Outlined.AlternateEmail,
                                    contentDescription = "Etiketle",
                                    tint = if (etiketAcik) LkPrimary else LkTextSecondary
                                )
                            }
                        }

                        if (viewModel.isUploadingMedia) {
                            Column(Modifier.fillMaxWidth().clip(LkShapes.SM).background(LkSurfaceSunken).padding(10.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Yükleniyor · %${(viewModel.mediaUploadProgress * 100).toInt()}", style = LkTypography.getMicro(), color = LkTextSecondary)
                                    TextButton(onClick = { viewModel.cancelMediaUpload() }) { Text("İptal et", color = LkDanger) }
                                }
                                LinearProgressIndicator(
                                    progress = viewModel.mediaUploadProgress,
                                    modifier = Modifier.fillMaxWidth(),
                                    color = LkPrimary,
                                    backgroundColor = LkLineSoft
                                )
                            }
                        }

                        if (etiketAcik && katkicilar.isNotEmpty()) {
                            Column(Modifier.fillMaxWidth().clip(LkShapes.SM).background(LkSurfaceSunken).padding(vertical = 4.dp)) {
                                katkicilar.forEach { kisi ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.onMetinChange(metneEtiketEkle(viewModel.metinInput, kisi.ad))
                                                etiketAcik = false
                                            }
                                            .heightIn(min = 44.dp)
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        LkAvatar(ad = kisi.ad, avatarUrl = kisi.avatarUrl, boyut = 24.dp)
                                        Spacer(Modifier.width(8.dp))
                                        Text(kisi.ad, style = LkTypography.getBodySmall(), color = LkTextPrimary, maxLines = 1)
                                    }
                                }
                            }
                        }

                        viewModel.attachedMedia?.let { media ->
                            /* Gorsel ONIZLEME (urun sahibi, 19.09.2026): eklenen fotograf paylasimdan
                               once gorunur; yukleme bitince sunucu URL'i var. */
                            if (media.kind == "image" && !media.url.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 320.dp)
                                        .clip(LkShapes.MD)
                                        .background(LkSurfaceSunken)
                                ) {
                                    LkRemoteImage(
                                        url = media.url,
                                        contentDescription = "Eklenen görsel",
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp, max = 320.dp),
                                        contentScale = ContentScale.Fit,
                                        yedek = {}
                                    )
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Kaldır",
                                        tint = LkOnPrimary,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .size(28.dp)
                                            .clip(LkShapes.FULL)
                                            .background(LkTextPrimary.copy(alpha = 0.55f))
                                            .clickable { viewModel.removeAttachedMedia() }
                                            .padding(5.dp)
                                    )
                                }
                                Spacer(Modifier.height(LkSpacing.Space2))
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(LkShapes.SM)
                                    .background(LkSurfaceSunken)
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (media.kind == "image") Icons.Outlined.Image else Icons.Outlined.InsertDriveFile,
                                        contentDescription = null,
                                        tint = LkPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(media.originalName ?: "Ekli dosya", style = LkTypography.getMicro(), color = LkTextPrimary, maxLines = 1)
                                }
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Kaldır",
                                    tint = LkDanger,
                                    modifier = Modifier.size(16.dp).clickable { viewModel.removeAttachedMedia() }
                                )
                            }
                        }
                    }
                }
            }

            /* Alt cubuk: sayac / uyari solda, Paylas sagda. Klavyenin ustunde durur. */
            LkHairline()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space3),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    viewModel.notice?.let { n -> Text(n, style = LkTypography.getMicro(), color = LkDanger) }
                    Text(
                        "${viewModel.metinInput.length}/500",
                        style = LkTypography.getMicro(),
                        color = if (viewModel.metinInput.length >= 480) LkDanger else LkTextMuted
                    )
                }
                LkButton(
                    text = if (viewModel.isSubmittingPost) "Paylaşılıyor…" else "Paylaş",
                    onClick = { viewModel.submitPost() },
                    enabled = gonderilebilir && !viewModel.isSubmittingPost && !viewModel.isUploadingMedia,
                    size = LkButtonSize.MD,
                    shape = LkShapes.FULL
                )
            }
        }
    }
}

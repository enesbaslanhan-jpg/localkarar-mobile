package com.localkarar.app.ui.screens.community

import com.localkarar.app.ui.components.LkLoadingSpinner
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.localkarar.app.community.CommunityViewModel
import com.localkarar.app.ui.components.LkAvatar
import com.localkarar.app.core.rememberFilePicker
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.theme.*

@Composable
fun ComposePostSheet(
    viewModel: CommunityViewModel
) {
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

    val title = when {
        replyTarget != null -> "Yanıt Yaz"
        quoteTarget != null -> "Alıntı Yap"
        else -> "Yeni Paylaşım"
    }

    AlertDialog(
        onDismissRequest = { viewModel.dismissCompose() },
        backgroundColor = LkSurfacePanel,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = LkTypography.getSectionTitle(), color = LkTextPrimary)
                IconButton(onClick = { viewModel.dismissCompose() }) {
                    Icon(Icons.Outlined.Close, contentDescription = "Kapat", tint = LkTextSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Reply target banner
                if (replyTarget != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(LkShapes.SM)
                            .background(LkSurfaceSunken)
                            .padding(8.dp)
                    ) {
                        Text(
                            "Yanıtlanan: @${replyTarget.author?.name ?: "kullanıcı"}",
                            style = LkTypography.getMicro(),
                            color = LkPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // Quote target card
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
                            Text(
                                quoteTarget.summary,
                                style = LkTypography.getBodySmall(),
                                color = LkTextSecondary,
                                maxLines = 2
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // Text field
                /* §0: ham Material alan degil sistem alani (LkTextField). */
                LkTextField(
                    value = viewModel.metinInput,
                    onValueChange = { viewModel.onMetinChange(it) },
                    placeholder = if (quoteTarget != null) "Düşüncelerini ekle..."
                        else "Toplulukla bir şeyler paylaş...",
                    singleLine = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                )

                Spacer(Modifier.height(6.dp))

                // Char counter & Attachment button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { filePicker() },
                            enabled = !viewModel.isUploadingMedia && viewModel.attachedMedia == null
                        ) {
                            if (viewModel.isUploadingMedia) {
                                LkLoadingSpinner(size = 18.dp)
                            } else {
                                Icon(Icons.Outlined.AttachFile, contentDescription = "Medya Ekle", tint = LkPrimary)
                            }
                        }
                        Text(
                            if (viewModel.attachedMedia != null) "Medya eklendi" else "Görsel / Belge",
                            style = LkTypography.getMicro(),
                            color = LkTextSecondary
                        )

                        /*
                         * ETIKETLE — webin `EtiketSecici`si
                         * (`CommunityPage.jsx:587`, `feed.mention.pick`).
                         *
                         * 🔴 Mobilde YOKTU: kullanici birinden bahsetmek
                         * icin adini elle, dogru yazmak zorundaydi.
                         *
                         * ⚠️ Liste webdekiyle AYNI kaynaktan: akista en
                         * cok paylasan dort kisi. Sunucuda kisi arama ucu
                         * yok; "tum kullanicilar" diye bir liste
                         * uydurulmuyor.
                         *
                         * ⚠️ Kimse yoksa dugme HIC cizilmiyor -- web de
                         * oyle yapiyor (`if (!kisiler.length) return null`).
                         */
                        if (katkicilar.isNotEmpty()) {
                            IconButton(onClick = { etiketAcik = !etiketAcik }) {
                                Icon(
                                    Icons.Outlined.AlternateEmail,
                                    contentDescription = "Etiketle",
                                    tint = if (etiketAcik) LkPrimary else LkTextSecondary
                                )
                            }
                        }
                    }

                    Text(
                        "${viewModel.metinInput.length}/500",
                        style = LkTypography.getMicro(),
                        color = if (viewModel.metinInput.length >= 480) LkDanger else LkTextMuted
                    )
                }

                /* Etiketlenecek kisiler — dugmenin hemen altinda aciliyor. */
                if (etiketAcik && katkicilar.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(LkShapes.SM)
                            .background(LkSurfaceSunken)
                            .padding(vertical = 4.dp)
                    ) {
                        katkicilar.forEach { kisi ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.onMetinChange(
                                            metneEtiketEkle(viewModel.metinInput, kisi.ad)
                                        )
                                        etiketAcik = false
                                    }
                                    /* §19: dokunma hedefi en az 44dp. */
                                    .heightIn(min = 44.dp)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LkAvatar(ad = kisi.ad, avatarUrl = kisi.avatarUrl, boyut = 24.dp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = kisi.ad,
                                    style = LkTypography.getBodySmall(),
                                    color = LkTextPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Attached media preview pill
                viewModel.attachedMedia?.let { media ->
                    Spacer(Modifier.height(6.dp))
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
                            Text(
                                media.originalName ?: "Ekli dosya",
                                style = LkTypography.getMicro(),
                                color = LkTextPrimary,
                                maxLines = 1
                            )
                        }
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Kaldır",
                            tint = LkDanger,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { viewModel.removeAttachedMedia() }
                        )
                    }
                }

                viewModel.notice?.let { n ->
                    Spacer(Modifier.height(6.dp))
                    Text(n, style = LkTypography.getMicro(), color = LkDanger)
                }
            }
        },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { viewModel.dismissCompose() }) {
                    Text("Vazgeç", color = LkTextSecondary)
                }
                Spacer(Modifier.width(8.dp))
                LkButton(
                    text = if (viewModel.isSubmittingPost) "Paylaşılıyor..." else "Paylaş",
                    onClick = { viewModel.submitPost() },
                    enabled = !viewModel.isSubmittingPost && !viewModel.isUploadingMedia
                )
            }
        }
    )
}

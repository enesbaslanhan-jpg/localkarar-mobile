package com.localkarar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localkarar.app.ui.theme.*

/** "1. Baslik" bicimindeki bolum numarasi. */
private val BOLUM_NUMARASI = Regex("""^(\d+)\.\s+""")

private sealed interface MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class CodeBlock(val language: String?, val code: String) : MarkdownBlock
    data class BulletItem(val text: String) : MarkdownBlock
    data class NumberedItem(val number: String, val text: String) : MarkdownBlock
    data class BlockQuote(val text: String) : MarkdownBlock
    /** GFM tablosu — mentor yanitlarinda da geliyor. */
    data class Table(val tablo: LkMdTablo) : MarkdownBlock
    data object DividerBlock : MarkdownBlock
}

@Composable
fun LkMarkdown(
    content: String,
    modifier: Modifier = Modifier,
    textColor: Color = LkTextPrimary,
    /**
     * OKUMA KIPI — ders govdesi gibi uzun metinler icin.
     *
     * Govde 15sp / satir yuksekligi 25,5sp (mockup "Akademi 3"in okuma
     * olcusu), basliklar sayfa olceginde, bloklar arasi bosluk iki kati.
     * Mentor balonu varsayilan (siki) kipte kaliyor.
     */
    okuma: Boolean = false
) {
    val blocks = remember(content) { parseMarkdownBlocks(content) }
    val govdeStili =
        if (okuma) LkTypography.getBody().copy(fontSize = 15.sp, lineHeight = 25.5.sp)
        else LkTypography.getBody().copy(lineHeight = 22.sp)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(if (okuma) 12.dp else 6.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Header -> {
                    val style = when {
                        okuma && block.level == 1 -> LkTypography.getPageTitle().copy(color = textColor)
                        okuma && block.level == 2 -> LkTypography.getSectionTitle().copy(color = textColor)
                        okuma -> LkTypography.getCardTitle().copy(color = textColor)
                        block.level == 1 -> LkTypography.getSectionTitle().copy(color = textColor, fontWeight = FontWeight.Bold)
                        block.level == 2 -> LkTypography.getBodyStrong().copy(color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        else -> LkTypography.getBodyStrong().copy(color = textColor, fontWeight = FontWeight.SemiBold)
                    }
                    /*
                     * DERSTE BOLUM BASLIKLARI NUMARALI GELIYOR:
                     * "1. Ciro İllüzyonu ve İndirimin Kâr Üzerindeki
                     * Etkisi". Duz metin olarak basildiginda ders, arasi
                     * ayrilmamis tek bir yazi blogu gibi okunuyordu.
                     *
                     * Okuma kipinde numara basliktan AYRILIP kendi
                     * rozetine giriyor ve bolumun ustune ince bir ayrac
                     * konuyor: sayfa "bolumleri olan bir ders" gibi
                     * gorunuyor. Numarayi ekleyen biz degiliz — icerikte
                     * zaten var, yalniz bicimi degisiyor.
                     */
                    val numara = if (okuma) BOLUM_NUMARASI.find(block.text)?.groupValues?.get(1) else null
                    if (numara != null) {
                        Column(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Divider(color = LkLineSoft, thickness = 1.dp)
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(26.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(LkSurfaceTile),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = numara,
                                        style = LkTypography.getMicro(),
                                        color = LkTileInk,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = parseInlineMarkdown(
                                        block.text.replaceFirst(BOLUM_NUMARASI, ""),
                                        textColor
                                    ),
                                    style = style,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = parseInlineMarkdown(block.text, textColor),
                            style = style,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                    }
                }
                is MarkdownBlock.Paragraph -> {
                    /*
                     * Paragrafta MATEMATIK olabilir.
                     *
                     * Once matematik var mi diye bakiliyor: icerigin buyuk
                     * cogunlugunda `$` yok ve o durumda ESKI YOL korunuyor
                     * (tek bir AnnotatedString) -- kalin/italik gibi satir ici
                     * bicimler orada calisiyor ve bozulmamali.
                     *
                     * Formul iceren paragraflarda metin ve formuller sirayla
                     * ciziliyor. Onceden `$$\text{...}$$` kullaniciya HAM
                     * olarak gorunuyordu.
                     */
                    MetinVeMatematik(block.text, govdeStili, textColor)
                }
                is MarkdownBlock.CodeBlock -> {
                    LkCodeBlockView(language = block.language, code = block.code)
                }
                is MarkdownBlock.BulletItem -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "• ",
                            style = LkTypography.getBody().copy(color = LkPrimary, fontWeight = FontWeight.Bold)
                        )
                        MetinVeMatematik(
                            metin = block.text,
                            stil = govdeStili,
                            renk = textColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is MarkdownBlock.NumberedItem -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${block.number}. ",
                            style = LkTypography.getBody().copy(color = LkPrimary, fontWeight = FontWeight.Bold)
                        )
                        MetinVeMatematik(
                            metin = block.text,
                            stil = govdeStili,
                            renk = textColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is MarkdownBlock.BlockQuote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(IntrinsicSize.Min)
                                .background(LkPrimary.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = parseInlineMarkdown(block.text, textColor.copy(alpha = 0.85f)),
                            style = LkTypography.getBodySmall().copy(fontStyle = FontStyle.Italic, lineHeight = 18.sp)
                        )
                    }
                }
                is MarkdownBlock.Table -> {
                    LkMarkdownTablo(
                        tablo = block.tablo,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                is MarkdownBlock.DividerBlock -> {
                    Divider(color = LkLineSoft, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

/**
 * Metin + LaTeX.
 *
 * 🔴 MATEMATIK YALNIZ PARAGRAFTA CIZILIYORDU. Ders govdelerinde hesap
 * satirlarinin cogu MADDE ICINDE: "**İşçilik Saat Ücreti:** $48.000
 * \text{ TL} \div 160 = 300$". Madde dali formulu hic islemedigi icin
 * ekranda yalniz "İşçilik Saat Ücreti:" kaliyor, SAYI KAYBOLUYORDU —
 * dersin anlatmak istedigi tek sey oydu.
 */
@Composable
private fun MetinVeMatematik(
    metin: String,
    stil: androidx.compose.ui.text.TextStyle,
    renk: Color,
    modifier: Modifier = Modifier
) {
    val parcalar = remember(metin) { matematikAyir(metin) }
    if (parcalar.size == 1 && parcalar[0] is MetinParcasi.Duz) {
        Text(
            text = parseInlineMarkdown(metin, renk),
            style = stil.copy(color = renk),
            modifier = modifier
        )
        return
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        parcalar.forEach { parca ->
            when (parca) {
                is MetinParcasi.Duz -> {
                    val duz = parca.value.trim()
                    if (duz.isNotEmpty()) {
                        Text(
                            text = parseInlineMarkdown(duz, renk),
                            style = stil.copy(color = renk)
                        )
                    }
                }
                is MetinParcasi.Matematik -> LkMath(
                    latex = parca.latex,
                    blok = parca.blok,
                    renk = renk
                )
            }
        }
    }
}

@Composable
private fun LkCodeBlockView(language: String?, code: String) {
    val clipboardManager = LocalClipboardManager.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, LkLineSoft, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            if (!language.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = language.uppercase(),
                        style = LkTypography.getMicro().copy(
                            color = LkPrimary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    IconButton(
                        onClick = { clipboardManager.setText(AnnotatedString(code)) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Outlined.ContentCopy,
                            contentDescription = "Kodu Kopyala",
                            tint = LkTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
            Text(
                text = code,
                style = LkTypography.getBodySmall().copy(
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFE2E8F0),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            )
        }
    }
}

/**
 * Once TABLOLAR ayriliyor, kalan metin satir satir cozumleniyor.
 *
 * 🔴 Tablo satirlari duz paragraf sayiliyordu: "| Baslik | Deger |"
 * satirlari bir araya yapisip okunmaz bir blok haline geliyordu.
 */
private fun parseMarkdownBlocks(raw: String): List<MarkdownBlock> {
    if (raw.isBlank()) return emptyList()
    return lkMarkdownParcala(raw).flatMap { parca ->
        when (parca) {
            is LkMdParca.Tablo -> listOf(MarkdownBlock.Table(parca.tablo))
            is LkMdParca.Metin -> parseMarkdownTextBlocks(parca.icerik)
        }
    }
}

private fun parseMarkdownTextBlocks(raw: String): List<MarkdownBlock> {
    if (raw.isBlank()) return emptyList()

    val blocks = mutableListOf<MarkdownBlock>()
    val lines = raw.lines()
    var inCodeBlock = false
    var codeLanguage: String? = null
    val codeBuilder = StringBuilder()
    val paragraphBuilder = StringBuilder()

    fun flushParagraph() {
        if (paragraphBuilder.isNotEmpty()) {
            val text = paragraphBuilder.toString().trim()
            if (text.isNotEmpty()) {
                blocks.add(MarkdownBlock.Paragraph(text))
            }
            paragraphBuilder.clear()
        }
    }

    var i = 0
    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()

        if (trimmed.startsWith("```")) {
            if (inCodeBlock) {
                // Close code block
                blocks.add(MarkdownBlock.CodeBlock(codeLanguage, codeBuilder.toString().trimEnd()))
                codeBuilder.clear()
                codeLanguage = null
                inCodeBlock = false
            } else {
                // Open code block
                flushParagraph()
                inCodeBlock = true
                codeLanguage = trimmed.removePrefix("```").trim().ifEmpty { null }
            }
            i++
            continue
        }

        if (inCodeBlock) {
            if (codeBuilder.isNotEmpty()) codeBuilder.append('\n')
            codeBuilder.append(line)
            i++
            continue
        }

        when {
            trimmed.startsWith("# ") -> {
                flushParagraph()
                blocks.add(MarkdownBlock.Header(1, trimmed.removePrefix("# ").trim()))
            }
            trimmed.startsWith("## ") -> {
                flushParagraph()
                blocks.add(MarkdownBlock.Header(2, trimmed.removePrefix("## ").trim()))
            }
            trimmed.startsWith("### ") -> {
                flushParagraph()
                blocks.add(MarkdownBlock.Header(3, trimmed.removePrefix("### ").trim()))
            }
            trimmed.startsWith("#### ") -> {
                flushParagraph()
                blocks.add(MarkdownBlock.Header(4, trimmed.removePrefix("#### ").trim()))
            }
            trimmed.startsWith("---") || trimmed.startsWith("***") || trimmed.startsWith("___") -> {
                flushParagraph()
                blocks.add(MarkdownBlock.DividerBlock)
            }
            trimmed.startsWith("> ") -> {
                flushParagraph()
                blocks.add(MarkdownBlock.BlockQuote(trimmed.removePrefix("> ").trim()))
            }
            trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                flushParagraph()
                blocks.add(MarkdownBlock.BulletItem(trimmed.substring(2).trim()))
            }
            trimmed.matches(Regex("^\\d+\\.\\s+.*")) -> {
                flushParagraph()
                val dotIndex = trimmed.indexOf('.')
                val num = trimmed.substring(0, dotIndex)
                val rest = trimmed.substring(dotIndex + 1).trim()
                blocks.add(MarkdownBlock.NumberedItem(num, rest))
            }
            trimmed.isEmpty() -> {
                flushParagraph()
            }
            else -> {
                if (paragraphBuilder.isNotEmpty()) paragraphBuilder.append(' ')
                paragraphBuilder.append(trimmed)
            }
        }
        i++
    }

    if (inCodeBlock && codeBuilder.isNotEmpty()) {
        blocks.add(MarkdownBlock.CodeBlock(codeLanguage, codeBuilder.toString().trimEnd()))
    }
    flushParagraph()

    return blocks
}

@Composable
fun parseInlineMarkdown(text: String, defaultColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val length = text.length

        while (cursor < length) {
            // Bold **text**
            if (cursor + 1 < length && text[cursor] == '*' && text[cursor + 1] == '*') {
                val end = text.indexOf("**", cursor + 2)
                if (end != -1) {
                    val inner = text.substring(cursor + 2, end)
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = defaultColor))
                    append(inner)
                    pop()
                    cursor = end + 2
                    continue
                }
            }

            // Italic *text* or _text_
            if (text[cursor] == '*' || text[cursor] == '_') {
                val marker = text[cursor]
                val end = text.indexOf(marker, cursor + 1)
                if (end != -1 && end > cursor + 1) {
                    val inner = text.substring(cursor + 1, end)
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic, color = defaultColor))
                    append(inner)
                    pop()
                    cursor = end + 1
                    continue
                }
            }

            // Inline code `code`
            if (text[cursor] == '`') {
                val end = text.indexOf('`', cursor + 1)
                if (end != -1) {
                    val inner = text.substring(cursor + 1, end)
                    pushStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = LkPrimary,
                            background = LkPrimary.copy(alpha = 0.12f)
                        )
                    )
                    append(" $inner ")
                    pop()
                    cursor = end + 1
                    continue
                }
            }

            // Markdown Links [text](url)
            if (text[cursor] == '[') {
                val closingBracket = text.indexOf(']', cursor + 1)
                if (closingBracket != -1 && closingBracket + 1 < length && text[closingBracket + 1] == '(') {
                    val closingParen = text.indexOf(')', closingBracket + 2)
                    if (closingParen != -1) {
                        val label = text.substring(cursor + 1, closingBracket)
                        pushStyle(
                            SpanStyle(
                                color = LkPrimary,
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                        append(label)
                        pop()
                        cursor = closingParen + 1
                        continue
                    }
                }
            }

            append(text[cursor])
            cursor++
        }
    }
}

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
import androidx.compose.material.icons.filled.ContentCopy
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

private sealed interface MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class CodeBlock(val language: String?, val code: String) : MarkdownBlock
    data class BulletItem(val text: String) : MarkdownBlock
    data class NumberedItem(val number: String, val text: String) : MarkdownBlock
    data class BlockQuote(val text: String) : MarkdownBlock
    data object DividerBlock : MarkdownBlock
}

@Composable
fun LkMarkdown(
    content: String,
    modifier: Modifier = Modifier,
    textColor: Color = LkTextPrimary
) {
    val blocks = remember(content) { parseMarkdownBlocks(content) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Header -> {
                    val style = when (block.level) {
                        1 -> LkTypography.getSectionTitle().copy(color = textColor, fontWeight = FontWeight.Bold)
                        2 -> LkTypography.getBodyStrong().copy(color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        else -> LkTypography.getBodyStrong().copy(color = textColor, fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        text = parseInlineMarkdown(block.text, textColor),
                        style = style,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = parseInlineMarkdown(block.text, textColor),
                        style = LkTypography.getBody().copy(color = textColor, lineHeight = 22.sp)
                    )
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
                        Text(
                            text = parseInlineMarkdown(block.text, textColor),
                            style = LkTypography.getBody().copy(color = textColor, lineHeight = 20.sp),
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
                        Text(
                            text = parseInlineMarkdown(block.text, textColor),
                            style = LkTypography.getBody().copy(color = textColor, lineHeight = 20.sp),
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
                is MarkdownBlock.DividerBlock -> {
                    Divider(color = LkLineSoft, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                }
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
                            Icons.Default.ContentCopy,
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

private fun parseMarkdownBlocks(raw: String): List<MarkdownBlock> {
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

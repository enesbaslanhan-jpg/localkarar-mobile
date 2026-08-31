package com.localkarar.app.core

import androidx.compose.runtime.*
import kotlinx.cinterop.*
import platform.Foundation.*
import platform.UIKit.*
import platform.darwin.NSObject
import platform.posix.memcpy

actual fun openExternalUrl(url: String) {
    try {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return
        val nsUrl = NSURL.URLWithString(trimmed) ?: return
        val app = UIApplication.sharedApplication
        if (app.canOpenURL(nsUrl)) {
            app.openURL(nsUrl, options = emptyMap<Any?, Any?>(), completionHandler = null)
        }
    } catch (e: Exception) {
        println("openExternalUrl failed: ${e.message}")
    }
}

private class DocumentPickerDelegate(
    private val onResult: (PickedFile?) -> Unit,
    private val onDismiss: () -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {

    @OptIn(ExperimentalForeignApi::class)
    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        if (url == null) {
            onResult(null)
            onDismiss()
            return
        }

        val isSecurityScoped = url.startAccessingSecurityScopedResource()
        try {
            val data = NSData.dataWithContentsOfURL(url)
            val fileName = url.lastPathComponent ?: "dosya"
            if (data != null) {
                val length = data.length.toInt()
                val bytes = ByteArray(length)
                if (length > 0) {
                    bytes.usePinned { pinned ->
                        memcpy(pinned.addressOf(0), data.bytes, data.length)
                    }
                }
                onResult(PickedFile(name = fileName, bytes = bytes))
            } else {
                onResult(null)
            }
        } catch (e: Exception) {
            println("Error reading picked file: ${e.message}")
            onResult(null)
        } finally {
            if (isSecurityScoped) {
                url.stopAccessingSecurityScopedResource()
            }
            onDismiss()
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onResult(null)
        onDismiss()
    }
}

@Composable
actual fun rememberFilePicker(onFilePicked: (PickedFile?) -> Unit): () -> Unit {
    var activeDelegate by remember { mutableStateOf<DocumentPickerDelegate?>(null) }

    return {
        try {
            val delegate = DocumentPickerDelegate(
                onResult = onFilePicked,
                onDismiss = { activeDelegate = null }
            )
            activeDelegate = delegate

            val picker = UIDocumentPickerViewController(
                documentTypes = listOf("public.item", "public.content", "public.data"),
                inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
            ).apply {
                this.delegate = delegate
                this.allowsMultipleSelection = false
            }

            val window = UIApplication.sharedApplication.keyWindow
                ?: UIApplication.sharedApplication.windows.firstOrNull { (it as? UIWindow)?.isKeyWindow() == true } as? UIWindow
                ?: UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow

            var topVc = window?.rootViewController
            while (topVc?.presentedViewController != null) {
                topVc = topVc.presentedViewController
            }

            if (topVc != null) {
                topVc.presentViewController(picker, animated = true, completion = null)
            } else {
                println("No rootViewController found to present document picker")
                onFilePicked(null)
                activeDelegate = null
            }
        } catch (e: Exception) {
            println("rememberFilePicker error: ${e.message}")
            onFilePicked(null)
            activeDelegate = null
        }
    }
}
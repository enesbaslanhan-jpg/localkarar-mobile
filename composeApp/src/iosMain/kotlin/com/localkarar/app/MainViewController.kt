package com.localkarar.app

import androidx.compose.ui.window.ComposeUIViewController
import com.localkarar.app.auth.SecureStorage
import com.localkarar.app.core.AppPreferences
import com.localkarar.app.ui.shell.KenarGeriKaydi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIGestureRecognizerStateEnded
import platform.UIKit.UIRectEdgeLeft
import platform.UIKit.UIScreenEdgePanGestureRecognizer
import platform.UIKit.UIViewController
import platform.darwin.NSObject

/*
 * SOL KENARDAN KAYDIRARAK GERI (19.09.2026).
 *
 * UIKit'in kendi kenar jesti tanıyıcısı Compose gorunumune eklenir. Tanindiginda
 * (parmak sol kenardan basladi, saga yeterince gitti ve kaldirildi) Compose
 * tarafindaki `SystemBackHandler` kaydi tetiklenir. Tanıyıcı `cancelsTouchesInView`
 * varsayilaniyla calisir: jest kabul edilince Compose'a dokunus iptali gider,
 * alttaki liste kaymaz. Hedef nesne (delegate) ARC'nin erken toplamamasi icin
 * dosya seviyesinde tutulur.
 */
private class KenarJestiHedefi : NSObject() {
    @OptIn(ExperimentalForeignApi::class)
    @ObjCAction
    fun kaydirildi(taniyici: UIScreenEdgePanGestureRecognizer) {
        if (taniyici.state != UIGestureRecognizerStateEnded) return
        val yeterli = taniyici.translationInView(taniyici.view).useContents { x > 40.0 }
        if (yeterli) KenarGeriKaydi.tetikle()
    }
}

private var kenarJestiHedefi: KenarJestiHedefi? = null

fun MainViewController(): UIViewController {
    val denetleyici = ComposeUIViewController {
        val secureStorage = SecureStorage()
        App(secureStorage = secureStorage, appPreferences = AppPreferences())
    }
    val hedef = KenarJestiHedefi()
    kenarJestiHedefi = hedef
    val taniyici = UIScreenEdgePanGestureRecognizer(target = hedef, action = NSSelectorFromString("kaydirildi:"))
    taniyici.edges = UIRectEdgeLeft
    denetleyici.view.addGestureRecognizer(taniyici)
    return denetleyici
}

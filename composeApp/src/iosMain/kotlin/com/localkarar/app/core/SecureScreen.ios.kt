package com.localkarar.app.core

import androidx.compose.runtime.Composable

/**
 * iOS'ta karsiligi YOK.
 *
 * Platform, FLAG_SECURE benzeri bir pencere bayragi sunmuyor; ekran
 * goruntusunu uygulama duzeyinde engellemenin desteklenen bir yolu yok.
 * (UITextField.isSecureTextEntry katmani ile yapilan bilinen hile belgelenmis
 * bir API degil ve iOS surumleri arasinda sessizce bozuluyor.)
 *
 * Islev bilerek bos: cagri yerleri iki platformda ayni kalsin, ekranlar
 * platform kontrolu yapmak zorunda kalmasin.
 */
@Composable
actual fun SecureScreen(enabled: Boolean) {
    // Kasitli olarak bos - yukaridaki gerekce.
}

package com.localkarar.app.core

import androidx.compose.runtime.Composable

/**
 * Bu ekran acikken ekran goruntusu ve ekran kaydini engeller; ayrica "son
 * uygulamalar" kucuk resminde icerigi gizler.
 *
 * NEDEN HER EKRANDA DEGIL:
 *
 * Uygulamanin tamaminda acmak cazip ama YANLIS olurdu. Turkiye'de kullanicilar
 * ekran goruntusunu yaygin bicimde paylasiyor: bir hesaplama sonucunu
 * muhasebeciye, bir siparis listesini tedarikciye gondermek gunluk kullanim.
 * Hepsini engellemek urunu isini yapamaz hale getirir.
 *
 * O yuzden yalniz kimlik bilgisi girilen ekranlarda aciliyor: giris, kayit,
 * sifre degistirme/sifirlama ve pazaryeri kimlik bilgisi formu. Bu ekranlarda
 * korunan sey kullanicinin KENDI verisi degil, hesabina erisim.
 *
 * iOS'ta karsiligi yok: platform FLAG_SECURE benzeri bir API sunmuyor ve
 * ekran goruntusunu uygulama duzeyinde engellemenin desteklenen yolu
 * bulunmuyor. Orada islev bilerek bos.
 */
@Composable
expect fun SecureScreen(enabled: Boolean = true)

package com.localkarar.app.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android baglanti gozlemcisi — ConnectivityManager geri cagrimi.
 *
 * `NET_CAPABILITY_VALIDATED` de araniyor: telefonun bir Wi-Fi'ye bagli olmasi
 * internet oldugu anlamina gelmiyor (otel/kafe giris sayfalari, kotasi bitmis
 * hat). Yalniz NET_CAPABILITY_INTERNET'e bakmak "baglisin" deyip her istegin
 * dusmesine yol aciyor -- kullanici acisindan en kafa karistirici durum.
 */
actual class ConnectivityMonitor actual constructor() {

    private val _cevrimici = MutableStateFlow(true)
    actual val cevrimici: StateFlow<Boolean> = _cevrimici.asStateFlow()

    private var yonetici: ConnectivityManager? = null
    private var geriCagrim: ConnectivityManager.NetworkCallback? = null

    /**
     * 🔴 `NET_CAPABILITY_VALIDATED` ARANMIYOR — ve bu bilincli.
     *
     * Ilk yazimda VALIDATED de sart kosulmustu. Emulatorde uctan uca turda
     * (02.09.2026) YANLIS POZITIF verdi: belge yukleme BASARIYLA tamamlandi
     * ama ekranda "Internet baglantisi yok" bandi duruyordu.
     *
     * Sebebi genel: VALIDATED, sistemin bir yoklama istegiyle interneti
     * DOGRULAMASINDAN sonra geliyor. Ag yeni baglandiginda, yoklama sunucusu
     * engelliyse ya da kurumsal aglarda bu bayrak gecikiyor veya hic gelmiyor.
     * O aralikta uygulama calisiyor ama band "cevrimdisisiniz" diyor.
     *
     * Yanlis pozitif burada yanlis negatiften DAHA KOTU: kullanici calisan
     * uygulamaya guvenmemeye baslar. Gercek cevrimdisilik zaten istek
     * dustugunde hata ekraninda goruluyor; bu band yalnizca "hic ag yok"
     * durumunu bildiriyor.
     */
    private fun agUygunMu(yonetici: ConnectivityManager): Boolean {
        val ag = yonetici.activeNetwork ?: return false
        val yetenekler = yonetici.getNetworkCapabilities(ag) ?: return false
        return yetenekler.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    actual fun basla() {
        if (geriCagrim != null) return
        val ctx = AppContextHolder.appContext ?: return
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
        yonetici = cm

        _cevrimici.value = agUygunMu(cm)

        val cagrim = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _cevrimici.value = agUygunMu(cm)
            }

            override fun onLost(network: Network) {
                _cevrimici.value = agUygunMu(cm)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                _cevrimici.value = agUygunMu(cm)
            }
        }

        return try {
            val istek = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(istek, cagrim)
            geriCagrim = cagrim
        } catch (e: Exception) {
            // Kayit basarisiz olursa uygulama calismaya devam etmeli:
            // cevrimdisi bandi gorunmez, o kadar. Iyimser varsayiliyor.
            AppLog.e("Connectivity", "Ag geri cagrimi kaydedilemedi", e)
            _cevrimici.value = true
        }
    }

    actual fun dur() {
        val cagrim = geriCagrim ?: return
        try {
            yonetici?.unregisterNetworkCallback(cagrim)
        } catch (e: Exception) {
            AppLog.e("Connectivity", "Ag geri cagrimi birakilamadi", e)
        }
        geriCagrim = null
    }
}

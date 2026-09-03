package com.localkarar.app.core

import kotlinx.coroutines.flow.StateFlow

/**
 * Ag baglantisi gozlemcisi.
 *
 * NEDEN VAR:
 *
 * Uygulamada baglanti farkindaligi HIC YOKTU. Cevrimdisi olundugu ancak bir
 * istek basarisiz olunca anlasiliyordu ve o zaman da kullaniciya diger tum
 * hatalarla AYNI ekran gosteriliyordu ("Bir Hata Olustu" + "Tekrar Dene").
 * Yani ucakta olan kullanici ile sunucusu coken kullanici ayni seyi goruyordu,
 * ikisi de aynı ise yaramaz dugmeye basiyordu.
 *
 * Baglanti geri geldiginde de hicbir sey olmuyordu: kullanicinin elle
 * yenilemesi gerekiyordu.
 */
expect class ConnectivityMonitor() {
    /** Su an ag var mi. Baslangic degeri iyimser (true) -- ilk olcume kadar. */
    val cevrimici: StateFlow<Boolean>

    /** Gozlemeyi baslatir. Birden fazla cagri zararsizdir. */
    fun basla()

    /** Gozlemeyi durdurur ve platform kaydini birakir. */
    fun dur()
}

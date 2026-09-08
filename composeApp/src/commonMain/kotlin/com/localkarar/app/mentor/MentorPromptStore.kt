package com.localkarar.app.mentor

/**
 * MENTORA TASINACAK BAGLAM — tek atislik.
 *
 * 🔴 KARAR MAKBUZUNDAKI "MENTORA SOR" BAGLAMI KAYBEDIYORDU.
 *
 * Webde dugme mentor adresine `?prompt=` ile gidiyor
 * (`DecisionReceipt.jsx` -> `askMentor`), yani kullanicinin sorusu
 * hazir geliyor. Mobilde mentor ekrani disaridan metin almiyordu;
 * dugme yalnizca mentoru aciyor ve kullanici kararini bastan yazmak
 * zorunda kaliyordu -- o hâlde dugmenin bir anlami kalmiyordu.
 *
 * ⚠️ SOHBET KENDILIGINDEN ACILMIYOR ve MESAJ KENDILIGINDEN
 * GONDERILMIYOR. Baglam yalnizca yazma kutusuna DUSUYOR; kullanici
 * okuyup duzenleyip gonderiyor. Otomatik gondermek, kullanicinin
 * yazmadigi bir soruyu onun agzindan sormak olurdu.
 *
 * ⚠️ TEK ATISLIK (`al()` okuyunca siliyor): baglam bir sonraki
 * sohbette de karsisina cikmamali.
 */
object MentorPromptStore {
    private var bekleyen: String? = null

    fun koy(metin: String) {
        bekleyen = metin.trim().takeIf { it.isNotBlank() }
    }

    /** Bekleyen baglami dondurur ve TEMIZLER. */
    fun al(): String? {
        val deger = bekleyen
        bekleyen = null
        return deger
    }
}

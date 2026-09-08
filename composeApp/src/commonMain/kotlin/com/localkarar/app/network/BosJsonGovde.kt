package com.localkarar.app.network

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.setBody
import kotlinx.serialization.json.JsonObject

/**
 * GOVDESIZ POST'A BOS JSON NESNESI KOYAR.
 *
 * 🔴 GOVDESIZ POST'LAR 400 DONUYORDU.
 *
 * Olculdu (08.09.2026, calisan sunucu gunlugu): `POST
 * /community/social/notifications/read` `400
 * FST_ERR_CTP_EMPTY_JSON_BODY -- "Body cannot be empty when
 * content-type is set to 'application/json'"` ile dusuyordu.
 *
 * Sebep `HttpClient.kt`teki `defaultRequest`: HER istege
 * `Content-Type: application/json` ekliyor. Govde vermeyen bir cagri
 * sunucuya "JSON gonderiyorum" deyip bos govde gonderiyor ve Fastify
 * bunu hakli olarak reddediyor.
 *
 * ⚠️ AYNI ARIZA `SafeApiClient.post` icin daha once giderilmisti, ama
 * ham `HttpClient` ile yapilan cagrilarda duruyordu. Etkilenen ve
 * SESSIZCE calismayan islemler: begenme, kaydetme, takip etme,
 * engelleme, sohbet davetini yanitlama ve bildirimlerin tumunu okundu
 * isaretleme -- ve ayni sey DELETE icin de gecerliydi: begeniyi geri
 * alma, kayittan cikarma, takibi birakma, engeli kaldirma, gonderi ve
 * medya silme.
 *
 * Cozum basligi kaldirmak DEGIL, basligi DOGRU KILMAK: bos bir JSON
 * nesnesi gonderiliyor.
 */
/* POST ve DELETE icin ayni sekilde gecerli. */
fun HttpRequestBuilder.bosJsonGovde() {
    setBody(JsonObject(emptyMap()))
}

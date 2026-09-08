package com.localkarar.app.network

import com.localkarar.app.core.AppLog

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.content.TextContent
import kotlinx.serialization.json.JsonObject
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SafeApiClient(
    @PublishedApi internal val httpClient: HttpClient,
    private val moduleName: String
) {

    private inner class ApiContractViolation(
        userMessage: String,
        technical: String
    ) : Exception(userMessage) {
        init {
            AppLog.e(moduleName, "contract violation: $technical")
        }
    }

    @PublishedApi
    internal fun genericMessage(): String = "$moduleName yüklenemedi. Lütfen tekrar deneyin."

    @PublishedApi
    internal fun HttpResponse.requireJson() {
        val contentType = contentType()
        if (contentType != null && !contentType.match(ContentType.Application.Json)) {
            throw ApiContractViolation(
                userMessage = genericMessage(),
                technical = "Expected application/json but received '$contentType'"
            )
        }
    }

    @PublishedApi
    internal suspend fun HttpResponse.serverMessage(): String? {
        return try {
            val text = bodyAsText()
            val obj = Json.parseToJsonElement(text).jsonObject
            val message = obj["message"]?.jsonPrimitive?.contentOrNull
                ?: obj["error"]?.jsonPrimitive?.contentOrNull
            if (message != null && message.length < 300 && !message.contains('\n')) message else null
        } catch (e: Exception) {
            null
        }
    }

    @PublishedApi
    internal suspend fun HttpResponse.toApiError(): ApiError {
        val serverMessage = serverMessage()
        return when (status) {
            HttpStatusCode.Unauthorized -> ApiError.Unauthorized()
            HttpStatusCode.Forbidden -> ApiError.Forbidden(serverMessage ?: "Bu işlem için yetkiniz bulunmuyor.")
            HttpStatusCode.NotFound -> ApiError.NotFound(serverMessage ?: "İstenen kaynak bulunamadı.")
            HttpStatusCode.UnprocessableEntity -> ApiError.ValidationError(serverMessage ?: "Girdiğiniz bilgiler geçersiz.")
            HttpStatusCode.Conflict -> ApiError.ValidationError(serverMessage ?: "Bu işlem şu anda yapılamıyor.")
            else -> {
                if (status.value >= 500) ApiError.ServerError()
                else ApiError.UnknownError(serverMessage ?: "Beklenmeyen bir hata oluştu.")
            }
        }
    }

    @PublishedApi
    internal fun mapException(e: Exception): ApiError {
        return when (e) {
            is ApiError -> e
            else -> {
                AppLog.e(moduleName, "request failed", e)
                ApiError.UnknownError(genericMessage())
            }
        }
    }

    suspend inline fun <reified T> get(path: String): Result<T> = try {
        val response = httpClient.get(path)
        if (response.status.isSuccess()) {
            response.requireJson()
            Result.success(response.body())
        } else {
            Result.failure(response.toApiError())
        }
    } catch (e: Exception) {
        Result.failure(mapException(e))
    }

    /*
     * 🔴 GOVDESIZ POST BOS GOVDE GONDERMIYOR — BOS JSON NESNESI GONDERIYOR.
     *
     * Olculdu (07.09.2026, emulator): belge onerisini kabul etmek
     * `400 "Body cannot be empty when content-type is set to
     * 'application/json'"` ile dusuyordu.
     *
     * Sebep: `HttpClient.kt`teki `defaultRequest` HER istege
     * `Content-Type: application/json` ekliyor. Buradaki `if (body != null)`
     * kosulu yalnizca GOVDEYI atliyor, basligi degil; sunucuya "JSON
     * gonderiyorum" deyip bos govde giden bir istek kaliyor ve Fastify
     * bunu hakli olarak reddediyor.
     *
     * ⚠️ BU YALNIZ YENI EKLENEN CAGRILARIN SORUNU DEGILDI. Ayni desendeki
     * iki cagri uründe zaten bozuktu: e-Fatura gelen kutusunu ACMA
     * (`/inbox`) ve bildirimlerin TUMUNU OKUNDU ISARETLEME
     * (`/notifications/read-all`). Ikisi de sessizce basarisiz oluyordu.
     *
     * Cozum basligi kaldirmak DEGIL, basligi DOGRU KILMAK: bos bir JSON
     * nesnesi gonderiliyor. Basligi kaldirmak `defaultRequest` ile
     * cakisir ve her cagri yerinde tekrar edilmesi gereken bir istisna
     * olurdu.
     */
    suspend inline fun <reified T> post(path: String, body: Any? = null): Result<T> = try {
        val response = httpClient.post(path) {
            contentType(ContentType.Application.Json)
            setBody(body ?: kotlinx.serialization.json.JsonObject(emptyMap()))
        }
        if (response.status.isSuccess()) {
            response.requireJson()
            Result.success(response.body())
        } else {
            Result.failure(response.toApiError())
        }
    } catch (e: Exception) {
        Result.failure(mapException(e))
    }

    suspend inline fun <reified T> patch(path: String, body: Any? = null): Result<T> = try {
        val response = httpClient.patch(path) {
            if (body != null) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }
        if (response.status.isSuccess()) {
            response.requireJson()
            Result.success(response.body())
        } else {
            Result.failure(response.toApiError())
        }
    } catch (e: Exception) {
        Result.failure(mapException(e))
    }

    suspend inline fun <reified T> put(path: String, body: Any? = null): Result<T> = try {
        val response = httpClient.put(path) {
            if (body != null) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }
        if (response.status.isSuccess()) {
            response.requireJson()
            Result.success(response.body())
        } else {
            Result.failure(response.toApiError())
        }
    } catch (e: Exception) {
        Result.failure(mapException(e))
    }

    /**
     * IKILI INDIRME — disa aktarma dosyalari icin.
     *
     * 🔴 `get` BU IS ICIN KULLANILAMAZ: `requireJson()` cagiriyor ve
     * sunucu CSV/XLSX/PDF donduruyor, yani her indirme "sozlesme
     * ihlali" olarak reddedilirdi.
     *
     * ⚠️ Hata dalinda gövde METIN olarak okunuyor: sunucu basarisizlikta
     * JSON hata donduruyor (`{ error: ... }`), dolayisiyla kullaniciya
     * ham bayt degil gercek mesaj gosterilebiliyor.
     */
    suspend fun getBytes(path: String): Result<ByteArray> = try {
        val response = httpClient.get(path)
        if (response.status.isSuccess()) {
            Result.success(response.body<ByteArray>())
        } else {
            Result.failure(response.toApiError())
        }
    } catch (e: Exception) {
        Result.failure(mapException(e))
    }

    /**
     * ELLE KURULMUS JSON GOVDESIYLE PUT.
     *
     * 🔴 `put(path, jsonObject)` CALISMIYOR: Ktor govdenin CALISMA
     * ZAMANI tipine bakip serilestirici ariyor ve `JsonObject` icindeki
     * degerler `JsonLiteral` oldugu icin patliyor --
     * "Serializer for class 'JsonLiteral' is not found"
     * (olculdu 08.09.2026, kurulum profili kaydedilemiyordu).
     *
     * Burada govde METIN olarak gonderiliyor ve icerik turu elle
     * veriliyor; `TextContent` ContentNegotiation'i devre disi
     * birakmadan ham JSON yazmanin yolu.
     *
     * ⚠️ Ne zaman gerekir: bir alanin GONDERILMEMESI ile `null`
     * GONDERILMESI arasinda fark oldugunda. Sunucunun kurulum semasinda
     * tam bu ayrim var (`.optional()` ama `.nullable()` degil), ve
     * kotlinx varsayilan olarak null'lari da yaziyor.
     */
    suspend inline fun <reified T> putJson(path: String, govde: JsonObject): Result<T> = try {
        val response = httpClient.put(path) {
            setBody(TextContent(govde.toString(), ContentType.Application.Json))
        }
        if (response.status.isSuccess()) {
            response.requireJson()
            Result.success(response.body())
        } else {
            Result.failure(response.toApiError())
        }
    } catch (e: Exception) {
        Result.failure(mapException(e))
    }

    /*
     * 🔴 SILME ISTEKLERI DE BOS GOVDEYLE DUSUYORDU.
     *
     * Olculdu (08.09.2026, sunucu gunlugu): kayit–belge bagini koparma
     * `400 FST_ERR_CTP_EMPTY_JSON_BODY -- "Body cannot be empty when
     * content-type is set to 'application/json'"` donuyordu.
     *
     * Sebep `post`takinin AYNISI: `defaultRequest` her istege
     * `Content-Type: application/json` ekliyor, govdesiz DELETE ise
     * sunucuya "JSON gonderiyorum" deyip bos govde gonderiyor.
     *
     * ⚠️ Bu YALNIZ yeni ucun sorunu degildi: bu istemciden giden HER
     * silme cagrisi ayni yoldan geciyor (kayit silme, belge silme,
     * hatirlatici silme...). Basligi kaldirmak `defaultRequest` ile
     * cakisirdi; basligi DOGRU kilmak icin bos bir JSON nesnesi
     * gonderiliyor.
     */
    suspend fun delete(path: String): Result<Unit> = try {
        val response = httpClient.delete(path) {
            contentType(ContentType.Application.Json)
            setBody(JsonObject(emptyMap()))
        }
        if (response.status.isSuccess()) {
            Result.success(Unit)
        } else {
            Result.failure(response.toApiError())
        }
    } catch (e: Exception) {
        Result.failure(mapException(e))
    }
}
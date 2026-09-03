package com.localkarar.app.workspaces

import com.localkarar.app.network.ApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * BELGE YUKLEME ve e-FATURA.
 *
 * 🔴 MOBILDE HIC YOKTU. `DocumentsScreen` acikca "Belge yukleme su an icin web
 * suruminde kullanilabilir" diyordu; kullanici mobilden belge yukleyemiyordu.
 *
 * Bu, mobilde en cok anlam ifade eden ozelliklerden biri: kullanici faturayi
 * telefonuyla cekip ya da e-posta ekinden secip dogrudan yukleyebilir.
 *
 * ⚠️ XML UZANTISI KRITIK: UBL-TR e-fatura ayristiricisi o yoldan besleniyor
 * (src/services/e-fatura.ts). Izinli uzantilardan XML'i cikarmak, e-fatura
 * ozelligini sessizce kapatmak olurdu.
 *
 * Sunucu sinirlari (src/services/documentSecurity.ts):
 *   - 10 MB
 *   - txt, md, csv, json, xml, docx, xlsx, pdf, png, jpg, jpeg
 *   - saatte 10 yukleme
 */
class DocumentUploadRepository(private val client: HttpClient) {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        /** Sunucudaki `MAX_FILE_SIZE` ile AYNI olmali. */
        const val EN_BUYUK_BOYUT_BAYT = 10 * 1024 * 1024

        /**
         * Sunucudaki `ALLOWED_EXTENSIONS` ile AYNI olmali.
         *
         * Istemcide de tutuluyor ki kullanici 10 MB'lik bir dosyayi yukleyip
         * sunucudan 400 beklemek yerine hatayi ANINDA gorsun. Sunucu
         * dogrulamasinin yerine gecmiyor -- onunla ayni seyi soyluyor.
         */
        val IZINLI_UZANTILAR = setOf(
            "txt", "md", "csv", "json", "xml", "docx", "xlsx", "pdf", "png", "jpg", "jpeg"
        )

        fun uzanti(dosyaAdi: String): String =
            dosyaAdi.substringAfterLast('.', "").lowercase()

        /** Yerel dogrulama; sorun yoksa null. */
        fun dosyaHatasi(dosyaAdi: String, boyut: Int): String? {
            val uzanti = uzanti(dosyaAdi)
            return when {
                uzanti.isBlank() || uzanti !in IZINLI_UZANTILAR ->
                    "Bu dosya türü desteklenmiyor. İzinli türler: ${IZINLI_UZANTILAR.joinToString(", ")}"
                boyut <= 0 -> "Dosya boş görünüyor."
                boyut > EN_BUYUK_BOYUT_BAYT -> "Dosya 10 MB sınırını aşıyor."
                else -> null
            }
        }

        /**
         * Icerik turu.
         *
         * Sunucu dosyanin GERCEK icerigini de dogruluyor (`dosyayiDogrula`),
         * yani burada yanlis bir tur yazmak guvenligi asmaya yaramaz; dogru
         * yazmak yalnizca sunucunun isini kolaylastiriyor.
         */
        fun icerikTuru(dosyaAdi: String): String = when (uzanti(dosyaAdi)) {
            "pdf" -> "application/pdf"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "csv" -> "text/csv"
            "json" -> "application/json"
            "xml" -> "application/xml"
            "md" -> "text/markdown"
            "txt" -> "text/plain"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            else -> "application/octet-stream"
        }
    }

    /**
     * `POST /documents/upload` — multipart.
     *
     * Donen belge kimligi cagirana veriliyor. Calisma alanina baglama AYRI bir
     * adim: `calismaAlaninaBagla`. Tek basina cagrilirsa belge yalniz
     * kullanicinin kisisel listesine girer.
     */
    suspend fun belgeYukle(dosyaAdi: String, icerik: ByteArray): Result<String> {
        val yerelHata = dosyaHatasi(dosyaAdi, icerik.size)
        if (yerelHata != null) return Result.failure(Exception(yerelHata))

        return try {
            val yanit = client.post("${ApiConfig.baseUrl}/documents/upload") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                "file",
                                icerik,
                                Headers.build {
                                    append(HttpHeaders.ContentType, icerikTuru(dosyaAdi))
                                    append(
                                        HttpHeaders.ContentDisposition,
                                        "filename=\"$dosyaAdi\""
                                    )
                                }
                            )
                        }
                    )
                )
            }

            if (!yanit.status.isSuccess()) {
                // Sunucu hata mesajlari Turkce ve kullaniciya dogrudan
                // gosterilebilir ("Dosya boyutu 10MB ile sinirlidir" gibi).
                val govde = yanit.bodyAsText()
                val mesaj = try {
                    json.parseToJsonElement(govde).jsonObject["error"]?.jsonPrimitive?.content
                } catch (e: Exception) {
                    null
                }
                return Result.failure(Exception(mesaj ?: "Belge yüklenemedi."))
            }

            val govde = yanit.bodyAsText()
            val belgeId = try {
                val nesne = json.parseToJsonElement(govde).jsonObject
                nesne["id"]?.jsonPrimitive?.content
                    ?: nesne["document"]?.jsonObject?.get("id")?.jsonPrimitive?.content
            } catch (e: Exception) {
                null
            }

            if (belgeId == null) Result.failure(Exception("Sunucu belge kimliği döndürmedi."))
            else Result.success(belgeId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Yuklenen belgeyi CALISMA ALANINA baglar.
     *
     * 🔴 IKINCI ADIM ZORUNLU. `POST /documents/upload` belgeyi yalniz
     * KULLANICIYA baglar; calisma alaninin belge listesi
     * (`GET /workspaces/:id/documents`) onu GORMEZ. Web de tam olarak bu iki
     * adimi yapiyor (api.js `workspace.documents.upload`): once yukle, sonra
     * `PATCH /workspaces/:id/documents/:documentId`.
     *
     * Bu adim atlanirsa kullanici "yuklendi" mesajini gorur ama belge listede
     * cikmaz -- sessiz ve kafa karistirici bir sonuc.
     *
     * `category` sunucuda enum: invoice | receipt | contract | promissory_note
     * | shipment | purchase | other.
     */
    suspend fun calismaAlaninaBagla(
        workspaceId: String,
        belgeId: String,
        kategori: String? = null
    ): Result<Unit> {
        return try {
            val yanit = client.patch("${ApiConfig.baseUrl}/workspaces/$workspaceId/documents/$belgeId") {
                contentType(ContentType.Application.Json)
                setBody(BelgeMetaVerisi(category = kategori))
            }
            if (yanit.status.isSuccess()) Result.success(Unit)
            else {
                val mesaj = try {
                    json.parseToJsonElement(yanit.bodyAsText())
                        .jsonObject["error"]?.jsonPrimitive?.content
                } catch (e: Exception) {
                    null
                }
                Result.failure(Exception(mesaj ?: "Belge çalışma alanına bağlanamadı."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Iki adimi birlestiren tek cagri.
     *
     * Cagri yerlerinin ikinci adimi unutmamasi icin var: ayri birakilsaydi bir
     * ekran yalniz yukleyip baglamayi atlar ve belge kaybolmus gibi gorunurdu.
     */
    suspend fun yukleVeBagla(
        workspaceId: String,
        dosyaAdi: String,
        icerik: ByteArray,
        kategori: String? = null
    ): Result<String> {
        val belgeId = belgeYukle(dosyaAdi, icerik).getOrElse { return Result.failure(it) }
        return calismaAlaninaBagla(workspaceId, belgeId, kategori).map { belgeId }
    }
}

@kotlinx.serialization.Serializable
private data class BelgeMetaVerisi(
    val category: String? = null
)

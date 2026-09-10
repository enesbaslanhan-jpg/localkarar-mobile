package com.localkarar.app.workspaces

import com.localkarar.app.network.ApiConfig
import com.localkarar.app.network.SafeApiClient
import com.localkarar.app.network.dto.*

/**
 * Saglayici suzgecindeki "hepsi" secenegi.
 *
 * Arayuzdeki etiket ayni zamanda suzgec degeri olarak kullaniliyor ve
 * repository icinde string karsilastirmasiyla ayikliniyordu. Sabite alindi ki
 * etiketin degismesi sessizce "sunucuya provider=TUMU gonder" haline gelmesin.
 */
const val TUM_SAGLAYICILAR = "TÜMÜ"

/**
 * Sunucunun tanidigi pazaryeri saglayicilari.
 *
 * `ordersQuery` (marketplace-routes.ts) enum'u yalniz bu dordunu kabul ediyor.
 * Arayuz bir ara WOOCOMMERCE de sunuyordu; secildiginde sunucu 422 donuyor,
 * repository de bunu uydurma listeye dusurup hatayi tamamen gizliyordu.
 */
val DESTEKLENEN_SAGLAYICILAR = listOf("TRENDYOL", "HEPSIBURADA", "N11", "SHOPIFY")

class WorkspaceRepository(private val api: SafeApiClient) {

    private val base = ApiConfig.baseUrl

    suspend fun listWorkspaces(): Result<WorkspaceListResponseDto> {
        return api.get("$base/workspaces")
    }

    suspend fun createWorkspace(body: CreateWorkspaceRequestDto): Result<WorkspaceDetailDto> {
        return api.post("$base/workspaces", body)
    }

    suspend fun getWorkspace(workspaceId: String): Result<WorkspaceDetailDto> {
        return api.get("$base/workspaces/$workspaceId")
    }

    suspend fun updateWorkspace(workspaceId: String, body: UpdateWorkspaceRequestDto): Result<WorkspaceDetailDto> {
        return api.put("$base/workspaces/$workspaceId", body)
    }

    suspend fun deleteWorkspace(workspaceId: String): Result<Unit> {
        return api.delete("$base/workspaces/$workspaceId")
    }

    suspend fun getMembers(workspaceId: String): Result<List<WorkspaceMemberDto>> {
        return api.get("$base/workspaces/$workspaceId/members")
    }

    suspend fun updateMemberRole(workspaceId: String, memberId: String, role: String): Result<Unit> {
        return api.put(
            "$base/workspaces/$workspaceId/members/$memberId/role",
            UpdateMemberRoleRequestDto(role)
        )
    }

    suspend fun removeMember(workspaceId: String, memberId: String): Result<Unit> {
        return api.delete("$base/workspaces/$workspaceId/members/$memberId")
    }

    suspend fun getInvitations(workspaceId: String): Result<List<BusinessInvitationDto>> {
        return api.get("$base/workspaces/$workspaceId/invitations")
    }

    suspend fun inviteMember(workspaceId: String, email: String, role: String): Result<Unit> {
        return api.post("$base/workspaces/$workspaceId/members", InviteMemberRequestDto(email, role))
    }

    suspend fun cancelInvitation(workspaceId: String, invitationId: String): Result<Unit> {
        return api.delete("$base/workspaces/$workspaceId/invitations/$invitationId")
    }

    suspend fun getContacts(workspaceId: String): Result<List<BusinessContactDto>> {
        return api.get("$base/workspaces/$workspaceId/contacts")
    }

    /*
     * CARİ HESAP — bakiye + hareket dökümü.
     *
     * 🔴 Bu sorunun cevabı üründe hiçbir yerde yoktu: kayıtlar kişiye
     * bağlanabiliyordu ama toplanmıyordu. Bir esnafın defterinde ilk
     * baktığı sayı budur.
     */
    suspend fun getCariHesap(workspaceId: String, contactId: String): Result<CariHesapDto> {
        return api.get("$base/workspaces/$workspaceId/contacts/$contactId/hesap")
    }

    suspend fun createContact(workspaceId: String, body: ContactInputDto): Result<BusinessContactDto> {
        return api.post("$base/workspaces/$workspaceId/contacts", body)
    }

    suspend fun updateContact(workspaceId: String, contactId: String, body: ContactInputDto): Result<Unit> {
        return api.put("$base/workspaces/$workspaceId/contacts/$contactId", body)
    }

    suspend fun deleteContact(workspaceId: String, contactId: String): Result<Unit> {
        return api.delete("$base/workspaces/$workspaceId/contacts/$contactId")
    }

    suspend fun getSettings(workspaceId: String): Result<WorkspaceSettingsDto> {
        return api.get("$base/workspaces/$workspaceId/settings")
    }

    suspend fun updateSettings(workspaceId: String, body: UpdateSettingsRequestDto): Result<WorkspaceSettingsDto> {
        return api.put("$base/workspaces/$workspaceId/settings", body)
    }

    /*
     * e-FATURA GELEN KUTUSU — webdeki isletme ayarlarinin ikinci karti.
     * Uclerin tamami MANAGER yetkisi istiyor (workspace.ts); yetkisiz
     * kullanicida sunucu 403 donuyor ve arayuz bolumu gizliyor.
     */
    suspend fun getInbox(workspaceId: String): Result<InboxStatusDto> {
        return api.get("$base/workspaces/$workspaceId/inbox")
    }

    /** Her cagri YENI adres uretiyor; yani ayni zamanda "adresi yenile". */
    suspend fun enableInbox(workspaceId: String): Result<InboxStatusDto> {
        return api.post("$base/workspaces/$workspaceId/inbox")
    }

    suspend fun disableInbox(workspaceId: String): Result<Unit> {
        return api.delete("$base/workspaces/$workspaceId/inbox")
    }

    suspend fun getInboxSenders(workspaceId: String): Result<InboxSendersResponseDto> {
        return api.get("$base/workspaces/$workspaceId/inbox/senders")
    }

    suspend fun addInboxSender(
        workspaceId: String,
        email: String,
        label: String? = null
    ): Result<Unit> {
        return api.post(
            "$base/workspaces/$workspaceId/inbox/senders",
            AddInboxSenderRequestDto(email = email.trim(), label = label?.trim()?.ifBlank { null })
        )
    }

    suspend fun removeInboxSender(workspaceId: String, senderId: String): Result<Unit> {
        return api.delete("$base/workspaces/$workspaceId/inbox/senders/$senderId")
    }

    suspend fun getActivity(workspaceId: String, limit: Int = 100): Result<ActivityResponseDto> {
        return api.get("$base/workspaces/$workspaceId/activity?limit=$limit")
    }

    suspend fun getTrackerSummary(workspaceId: String): Result<TrackerSummaryDto> {
        return api.get("$base/workspaces/$workspaceId/tracker/summary")
    }

    suspend fun getCalendar(workspaceId: String, from: String, to: String): Result<CalendarDayResponseDto> {
        return api.get("$base/workspaces/$workspaceId/tracker/calendar?from=$from&to=$to")
    }

    /**
     * @param kararKaynakli yalnizca bir karara bagli gorevler. Suzgec
     *   SUNUCUDA: istemcide suzulseydi liste 100 kayitla sinirli
     *   oldugu icin 101. siradaki karar gorevi sessizce kaybolurdu.
     */
    suspend fun getRecords(
        workspaceId: String,
        status: String? = null,
        type: String? = null,
        direction: String? = null,
        kararKaynakli: Boolean = false,
        limit: Int = 100,
        offset: Int = 0
    ): Result<RecordListResponseDto> {
        val params = mutableListOf<String>()
        if (kararKaynakli) params.add("kararKaynakli=true")
        if (!status.isNullOrBlank()) params.add("status=$status")
        if (!type.isNullOrBlank()) params.add("type=$type")
        if (!direction.isNullOrBlank()) params.add("direction=$direction")
        params.add("limit=$limit")
        params.add("offset=$offset")
        val query = if (params.isEmpty()) "" else "?" + params.joinToString("&")
        return api.get("$base/workspaces/$workspaceId/records$query")
    }

    /**
     * Yonetici analizi. Sahip/yonetici disindaki roller 403 alir;
     * cagiran taraf bunu SESSIZCE yutar ve paneli hic cizmez —
     * erisemeyecegi bir seyi hatirlatan uyari kullaniciya bir sey
     * kazandirmaz.
     */
    suspend fun getTrackerAnalysis(workspaceId: String): Result<TrackerAnaliziDto> {
        return api.get("$base/workspaces/$workspaceId/tracker/analysis")
    }

    suspend fun getRecord(workspaceId: String, recordId: String): Result<BusinessRecordDto> {
        return api.get("$base/workspaces/$workspaceId/records/$recordId")
    }

    suspend fun createRecord(workspaceId: String, body: RecordInputDto): Result<BusinessRecordDto> {
        return api.post("$base/workspaces/$workspaceId/records", body)
    }

    suspend fun updateRecord(workspaceId: String, recordId: String, body: RecordUpdateDto): Result<BusinessRecordDto> {
        return api.patch("$base/workspaces/$workspaceId/records/$recordId", body)
    }

    suspend fun deleteRecord(workspaceId: String, recordId: String): Result<Unit> {
        return api.delete("$base/workspaces/$workspaceId/records/$recordId")
    }

    suspend fun deferRecord(workspaceId: String, recordId: String, dueAt: String, reason: String): Result<BusinessRecordDto> {
        return api.post(
            "$base/workspaces/$workspaceId/records/$recordId/defer",
            DeferRecordRequestDto(dueAt, reason)
        )
    }

    suspend fun getNotifications(workspaceId: String): Result<NotificationsResponseDto> {
        return api.get("$base/workspaces/$workspaceId/notifications")
    }

    suspend fun markNotificationRead(workspaceId: String, notificationId: String): Result<Unit> {
        return api.patch("$base/workspaces/$workspaceId/notifications/$notificationId/read")
    }

    suspend fun markAllNotificationsRead(workspaceId: String): Result<Unit> {
        return api.post("$base/workspaces/$workspaceId/notifications/read-all")
    }

    suspend fun getDocuments(workspaceId: String): Result<WorkspaceDocumentsResponseDto> {
        return api.get("$base/workspaces/$workspaceId/documents")
    }

    suspend fun deleteDocument(workspaceId: String, documentId: String): Result<Unit> {
        return api.delete("$base/workspaces/$workspaceId/documents/$documentId")
    }

    suspend fun updateDocumentMetadata(workspaceId: String, documentId: String, body: DocumentMetadataDto): Result<Unit> {
        return api.patch("$base/workspaces/$workspaceId/documents/$documentId", body)
    }

    /*
     * KAYIT–BELGE BAGI (madde 11).
     *
     * 🔴 Uc sunucuda VARDI ama ne webde ne mobilde cagriliyordu:
     * kullanici elindeki bir belgeyi mevcut bir kayda baglayamiyordu.
     * Bag yalnizca belge analizinden URETILEN kayitlarda kuruluyordu.
     *
     * ⚠️ Koparma ucu bu turda SUNUCUYA EKLENDI; ekleme varken kaldirma
     * yoktu ve yanlis baglanan belge geri alinamiyordu.
     *
     * ⚠️ Koparmak BELGEYI SILMEZ; belge calisma alaninda kalir.
     * Belgeyi silmek icin `deleteDocument` var.
     */
    suspend fun attachDocumentToRecord(
        workspaceId: String,
        recordId: String,
        documentId: String
    ): Result<Unit> {
        return api.post("$base/workspaces/$workspaceId/records/$recordId/documents/$documentId")
    }

    suspend fun detachDocumentFromRecord(
        workspaceId: String,
        recordId: String,
        documentId: String
    ): Result<Unit> {
        return api.delete("$base/workspaces/$workspaceId/records/$recordId/documents/$documentId")
    }

    /*
     * BELGE ONERISI — KABUL VE RET.
     *
     * 🔴 Bu iki cagri mobilde HIC YOKTU. Sunucu yuklenen belgeyi okuyup
     * "bundan su kaydi acayim mi" diye soruyor; webde iki dugme var
     * (`pages/Workspaces/Documents.jsx`), mobilde soru soruluyor ama
     * cevap verilemiyordu.
     *
     * ⚠️ Adresteki bolum `document-suggestions`, `documents` DEGIL:
     * pazaryeri kaynakli onerilerde belge yok (`documentId` null), o
     * yuzden sunucu oneriyi belgenin altina degil kendi ucuna koymus.
     */
    /*
     * DISA AKTARMA.
     *
     * 🔴 Mobilde HIC YOKTU. Webde kayit listesi CSV/Excel/PDF olarak
     * indiriliyor (`Tracker.jsx` -> `exports.downloadRecords`).
     *
     * ⚠️ Telefonda "indirilenler klasoru" webdeki ile ayni sey degil:
     * kullanici belgeyi muhasebecisine gondermek istiyor. Bu yuzden
     * inen bayt dizisi sistem PAYLASIM sayfasina veriliyor
     * (`rememberFileSharer`); "kaydet" onun icindeki seceneklerden biri.
     *
     * Suzgecler sorgu dizesiyle gidiyor: ekranda ne suzuldiyse disa
     * aktarilan da o. Suzgecsiz tam liste gondermek, kullanicinin
     * baktigi seyden baska bir dosya uretmek olurdu.
     */
    suspend fun exportRecords(
        workspaceId: String,
        format: String,
        status: String? = null,
        type: String? = null,
        direction: String? = null
    ): Result<ByteArray> {
        val sorgu = listOfNotNull(
            status?.let { "status=$it" },
            type?.let { "type=$it" },
            direction?.let { "direction=$it" }
        ).joinToString("&")
        val ek = if (sorgu.isBlank()) "" else "?$sorgu"
        return api.getBytes("$base/workspaces/$workspaceId/exports/records.$format$ek")
    }

    /*
     * TOPLU ICE AKTARMA.
     *
     * Iki asamali: `previewOnly = true` ile kac satirin gecerli oldugu
     * ve hatalar goruluyor, kullanici onaylayinca `false` ile yaziliyor.
     *
     * ⚠️ SUNUCU SUTUN ESLESTIRMESINI KENDISI YAPMIYOR; istemci
     * gonderiyor (`columnMapping`). Web bunu tarayicida CSV basligini
     * okuyup takma adlarla esleyerek uretiyor; mobil ayni takma adlari
     * kullaniyor (`iceAktarmaEslestir`).
     */
    suspend fun importRecords(
        workspaceId: String,
        fileId: String,
        columnMapping: Map<String, String>,
        previewOnly: Boolean
    ): Result<RecordImportResultDto> {
        return api.post(
            "$base/workspaces/$workspaceId/records/import",
            RecordImportRequestDto(fileId, columnMapping, previewOnly)
        )
    }

    /*
     * CARİ EKSTRE PDF'İ.
     *
     * 🔴 Cari hesap ekranda görünüyor ama dışarı çıkamıyordu. Esnaf bu
     * dökümü karşı tarafa ("bak, şu kadar kalmış") ya da muhasebecisine
     * gönderir; ekran görüntüsü almak kullanıcının işi olmamalı.
     */
    suspend fun cariEkstrePdf(workspaceId: String, contactId: String): Result<ByteArray> {
        return api.getBytes("$base/workspaces/$workspaceId/contacts/$contactId/ekstre.pdf")
    }

    /*
     * BELGENİN KENDİSİNİ İNDİR.
     *
     * 🔴 Bu yol YOKTU: kullanıcı faturasının fotoğrafını yüklüyor, geri
     * alamıyordu. Belge detayı yalnız OCR metnini dönüyordu -- veri
     * içeri girip çıkamıyordu.
     */
    suspend fun belgeIndir(workspaceId: String, documentId: String): Result<ByteArray> {
        return api.getBytes("$base/workspaces/$workspaceId/documents/$documentId/download")
    }

    /** Tek bir kaydin PDF dokumu — webde de kayit basina indiriliyor. */
    suspend fun exportRecordPdf(workspaceId: String, recordId: String): Result<ByteArray> {
        return api.getBytes("$base/workspaces/$workspaceId/records/$recordId/export.pdf")
    }

    /*
     * EKIP DAVETINI KABUL ET.
     *
     * 🔴 Mobilde HIC YOKTU: davet GONDERILEBILIYOR ama gelen davet
     * kabul edilemiyordu. Davet e-postasini telefonunda acan kullanici
     * akisin ortasinda kaliyordu.
     *
     * ⚠️ Adres calisma alani BAZLI DEGIL: davetli kisi henuz o calisma
     * alaninin uyesi degil, dolayisiyla `/workspaces/{id}/...` altindaki
     * yetki kapisindan gecemezdi. Sunucu bu yuzden ucu ustte tutuyor.
     */
    suspend fun acceptInvitation(token: String): Result<InvitationAcceptResponseDto> {
        return api.post(
            "$base/workspaces/invitations/accept",
            InvitationAcceptRequestDto(token)
        )
    }

    suspend fun acceptDocumentSuggestion(workspaceId: String, suggestionId: String): Result<Unit> {
        return api.post("$base/workspaces/$workspaceId/document-suggestions/$suggestionId/accept")
    }

    /*
     * BELGEDEN FINANSAL MODEL ONERISI.
     *
     * 🔴 Mobilde HIC YOKTU. Sunucu belgenin metninden hangi modelin
     * calistirilabilecegini ve girdilerin ne kadarinin HAZIR oldugunu
     * hesapliyor; webde belge kartinin altinda dugme olarak duruyor.
     *
     * ⚠️ Bu uc `financial-models/routes.ts` altinda ve adres
     * `/workspaces/...` ile BASLIYOR ama isletme rotalarina ait degil;
     * yine de ayni onek kullaniliyor.
     */
    suspend fun getDocumentModelSuggestions(
        workspaceId: String,
        documentId: String
    ): Result<ModelOnerileriDto> {
        return api.get(
            "$base/workspaces/$workspaceId/documents/$documentId/financial-model-suggestions"
        )
    }

    suspend fun rejectDocumentSuggestion(workspaceId: String, suggestionId: String): Result<Unit> {
        return api.post("$base/workspaces/$workspaceId/document-suggestions/$suggestionId/reject")
    }

    // ========================================================================
    // 🔴 UYDURMA VERI BURADAN SILINDI
    //
    // Bu bolum dort ayri yerde gercek olmayan veri uretiyordu:
    //
    //  1. getOrders          — kullanicinin KENDI muhasebe kayitlarini alip
    //                          `idx % 3` ile TRENDYOL/HEPSIBURADA/N11'e dagitiyor,
    //                          %15 sabit komisyon ve 45.0 TL sabit kargo uydurup
    //                          "pazaryeri siparisi" diye sunuyordu.
    //  2. getProducts        — dort elle yazilmis sahte urun (p-101...), uydurma
    //                          ciro (83250.0) ve iade oranlariyla. Biri stok=4,
    //                          biri stok=0 secilmisti ki suzgecler "calisir" gorunsun.
    //  3. getIntegrationStatus — HERHANGI bir hatada `connected = true`.
    //  4. syncOrders         — sunucu 404/400/409 donerken kullaniciya
    //                          "12 siparis basariyla esitlendi" diyordu.
    //
    // Dordu de "dayanikli yedek yol" diye yazilmisti ama yedek DEGILDI: DTO'lar
    // sunucunun gondermedigi alanlari zorunlu istedigi icin her basarili yanit
    // ayristirmada dusuyordu ve bu yol HER ZAMAN calisiyordu.
    //
    // Yeni kural: veri yoksa BOS DURUM gosterilir. Uydurulmaz.
    // ========================================================================

    suspend fun getOrders(
        workspaceId: String,
        provider: String? = null,
        limit: Int = 100
        // status: istemci tarafi derin baglanti suzgeci — sunucuya gonderilmiyor
        // q: kanonik liste sozlesmesinin parcasi degil
    ): Result<OrderListResponseDto> {
        val params = mutableListOf("workspaceId=$workspaceId")
        if (!provider.isNullOrBlank() && provider != TUM_SAGLAYICILAR) params.add("provider=$provider")
        params.add("limit=$limit")

        val wire: Result<OrderListWireDto> = api.get("$base/marketplace/orders?" + params.joinToString("&"))
        val liste = wire.getOrElse { return Result.failure(it) }

        // Baglanti durumu ve son esitleme zamani LISTE ucundan GELMIYOR --
        // sunucu `{ orders, total, limit, offset }` donuyor, baska bir sey degil.
        // Bilinmiyorsa "bagli degil" varsayiliyor; tersi kullaniciya yalan olur.
        val durum = getIntegrationStatus(workspaceId).getOrNull()

        return Result.success(
            OrderListResponseDto(
                orders = liste.orders,
                total = liste.total,
                lastSyncedAt = durum?.lastSyncedAt,
                integrationConnected = durum?.connected ?: false
            )
        )
    }

    suspend fun getOrderDetail(workspaceId: String, orderId: String): Result<OrderDto> {
        val wire: Result<OrderDetailWireDto> =
            api.get("$base/marketplace/orders/$orderId?workspaceId=$workspaceId")
        return wire.map { it.order }
    }

    suspend fun getIntegrationStatus(
        workspaceId: String,
        provider: String = "trendyol"
    ): Result<IntegrationStatusDto> =
        api.get("$base/integrations/${provider.lowercase()}/status?workspaceId=$workspaceId")

    // ========================================================================
    // ENTEGRASYON YASAM DONGUSU
    //
    // Bu bolum YOKTU. Mobil yalnizca trendyol/status ve trendyol/sync
    // cagiriyordu; kullanici mobilden hicbir pazaryeri BAGLAYAMIYORDU. Bu
    // yuzden gercek veri hicbir zaman gelmiyor, uydurma yol da hep devrede
    // kaliyordu.
    // ========================================================================

    /** Desteklenen pazaryerleri katalogu (kimlik dogrulamasi gerektirmiyor). */
    suspend fun getMarketplaceCatalog(): Result<MarketplaceCatalogDto> =
        api.get("$base/integrations/marketplaces")

    /** Calisma alaninin mevcut baglantilari + katalog. */
    suspend fun getWorkspaceIntegrations(workspaceId: String): Result<WorkspaceIntegrationsDto> =
        api.get("$base/integrations?workspaceId=$workspaceId")

    /*
     * Baglanma cagrilari saglayici basina AYRI, cunku her birinin kimlik
     * modeli farkli ve sunucu ayri zod semalariyla dogruluyor. Tek bir "genel"
     * istek nesnesi, alan adlarini yanlis gonderip 422 almanin en kolay yolu
     * olurdu -- ve o 422 eskiden uydurma listeye dusup gorunmez olurdu.
     *
     * ⚠️ Kimlik bilgileri MOBILDE SAKLANMIYOR. Sunucu onlari sifreleyip
     * tutuyor ve hicbir yanitta geri vermiyor; buradan yalniz gecerken
     * geciyorlar.
     *
     * ⚠️ Sunucu `connect` sirasinda kimlik bilgilerini ONCE DOGRULUYOR ve
     * gecersizse veritabanina YAZMIYOR. Dolayisiyla buradan donen hata gercek
     * bir dogrulama sonucudur, kullaniciya oldugu gibi gosterilmeli.
     */
    suspend fun connectTrendyol(body: TrendyolConnectRequestDto): Result<Unit> =
        api.post("$base/integrations/trendyol/connect", body)

    suspend fun connectHepsiburada(body: HepsiburadaConnectRequestDto): Result<Unit> =
        api.post("$base/integrations/hepsiburada/connect", body)

    suspend fun connectN11(body: N11ConnectRequestDto): Result<Unit> =
        api.post("$base/integrations/n11/connect", body)

    /**
     * Shopify OAuth ile baglaniyor: sunucu bir yetkilendirme adresi donuyor.
     *
     * O adres HARICI TARAYICIDA acilmali, uygulama ici WebView'de DEGIL:
     * kullanicidan Shopify hesabinin parolasi isteniyor ve uygulamanin
     * gosterdigi bir web goruntuleyicide parola toplamak hem guvenlik hem
     * magaza incelemesi acisindan yanlis.
     */
    suspend fun connectShopify(body: ShopifyConnectRequestDto): Result<ShopifyConnectResponseDto> =
        api.post("$base/integrations/shopify/connect", body)

    suspend fun disconnectIntegration(workspaceId: String, provider: String): Result<Unit> =
        api.delete("$base/integrations/${provider.lowercase()}/disconnect?workspaceId=$workspaceId")

    /**
     * Esitlemeyi BASLATIR. Sunucu `{ started, connectionId }` donuyor; isin
     * bitmesini beklemiyor. "Su kadar siparis esitlendi" denemez -- o bilgi bu
     * cagriyla ogrenilemez, esitleme calisma kayitlarindan (latestRuns) gelir.
     *
     * Hata durumlari gizlenmiyor: baglanti yoksa 404, baglanti pasifse 400
     * (CONNECTION_NOT_ACTIVE), zaten calisiyorsa 409 (SYNC_ALREADY_RUNNING).
     */
    suspend fun syncOrders(
        workspaceId: String,
        provider: String = "trendyol"
    ): Result<SyncStartedResponseDto> =
        api.post(
            "$base/integrations/${provider.lowercase()}/sync",
            SyncMarketplaceRequestDto(workspaceId = workspaceId)
        )

    // ========================================================================
    // PAZARYERI URUNLERI: GET /marketplace/products
    // ========================================================================

    suspend fun getProducts(
        workspaceId: String,
        provider: String? = null,
        onSale: Boolean? = null,
        stockFilter: String? = null, // "low" | "out" (kanonik web degerleri)
        windowDays: String = "30",   // "7" | "30" | "90"
        sort: String = "default",    // "default" | "bestSelling" | "topRevenue" | "mostReturned"
        query: String? = null
    ): Result<ProductListResponseDto> {
        val params = mutableListOf("workspaceId=$workspaceId")
        if (!provider.isNullOrBlank() && provider != TUM_SAGLAYICILAR) params.add("provider=$provider")
        if (onSale != null) params.add("onSale=$onSale")

        // Kanonik web degerleri "low" | "out" -- low_stock / out_of_stock DEGIL
        val kanonikStok = when (stockFilter) {
            "low_stock", "low" -> "low"
            "out_of_stock", "out" -> "out"
            else -> null
        }
        if (kanonikStok != null) params.add("stockFilter=$kanonikStok")

        val kanonikPencere = when (windowDays) {
            "7", "7d" -> "7"
            "90", "90d" -> "90"
            else -> "30"
        }
        params.add("windowDays=$kanonikPencere")

        // Varsayilan siralamada parametre HIC gonderilmiyor (sort=default DEGIL)
        val kanonikSiralama = when (sort) {
            "bestSelling", "best_selling" -> "bestSelling"
            "topRevenue", "top_revenue" -> "topRevenue"
            "mostReturned", "most_returned" -> "mostReturned"
            else -> null
        }
        if (kanonikSiralama != null) params.add("sort=$kanonikSiralama")
        if (!query.isNullOrBlank()) params.add("q=$query")

        val wire: Result<ProductListWireDto> = api.get("$base/marketplace/products?" + params.joinToString("&"))
        val liste = wire.getOrElse { return Result.failure(it) }

        // Suzme ve siralama SUNUCUDA yapiliyor; istemci tarafinda tekrarlanmiyor.
        // Onceki surumde bu isler uydurma listenin uzerinde calisiyordu, o yuzden
        // suzgecler "calisiyor" gibi gorunuyordu.
        val durum = getIntegrationStatus(workspaceId).getOrNull()

        return Result.success(
            ProductListResponseDto(
                products = liste.products,
                total = liste.total,
                lastSyncedAt = durum?.lastSyncedAt,
                integrationConnected = durum?.connected ?: false
            )
        )
    }

    suspend fun getProductDetail(workspaceId: String, productId: String): Result<ProductDto> {
        val wire: Result<ProductDetailWireDto> =
            api.get("$base/marketplace/products/$productId?workspaceId=$workspaceId")
        // Detay ucunda performans AYRI alanda geliyor; urun nesnesine tasiniyor
        // ki arayuz liste ile detay arasinda ayni alanlari okusun.
        return wire.map { it.product.copy(performance = it.performance) }
    }

    /**
     * Urun ayarlarini kaydeder.
     *
     * Onceki surum PATCH basarisiz olunca degisikligi surec ici bir map'e yazip
     * `Result.success` donuyordu: kullanici "kaydedildi" goruyor, uygulama
     * kapaninca kayip. Artik hata oldugu gibi yukari veriliyor.
     */
    suspend fun updateProductSettings(
        workspaceId: String,
        productId: String,
        body: UpdateProductSettingsRequestDto
    ): Result<Unit> =
        api.patch("$base/marketplace/products/$productId/settings", body)
}

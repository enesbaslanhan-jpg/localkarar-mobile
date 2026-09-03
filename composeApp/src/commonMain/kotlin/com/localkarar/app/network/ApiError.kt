package com.localkarar.app.network

sealed class ApiError(message: String) : Exception(message) {
    class NetworkUnavailable(message: String = "Bağlantı kurulamadı. İnternet bağlantınızı kontrol edin.") : ApiError(message)
    class Timeout(message: String = "İstek zaman aşımına uğradı. Lütfen tekrar deneyin.") : ApiError(message)
    class Unauthorized(message: String = "Oturumunuzun süresi doldu veya yetkisiz erişim.") : ApiError(message)
    class Forbidden(message: String = "Bu işlem için yetkiniz bulunmuyor.") : ApiError(message)
    class NotFound(message: String = "İstenen kaynak bulunamadı.") : ApiError(message)
    class ValidationError(message: String = "Girdiğiniz bilgiler geçersiz.", val details: Map<String, List<String>>? = null) : ApiError(message)
    class ServerError(message: String = "Sunucu hatası oluştu. Lütfen daha sonra tekrar deneyin.") : ApiError(message)
    class UnknownError(message: String = "Bilinmeyen bir hata oluştu.") : ApiError(message)

    /**
     * Uyelik suresi doldu: hesap SALT OKUNUR modda.
     *
     * Sunucu bunu 403 + `{"code": "MEMBERSHIP_EXPIRED"}` olarak donuyor
     * (src/services/membership-guard.ts). 401 DEGIL -- ve bu bilincli bir
     * secim: 401 istemcide sessiz token yenilemesini tetikleyip basarisiz
     * olunca oturumu siliyor, boylece suresi dolan kullanici uygulamadan
     * atiliyor ve uyeligini baslatabilecegi ekrana hic ulasamiyordu.
     *
     * Bu yuzden BURADA DA oturum silinmemeli: kullanici girisli kalmali,
     * verisini gorebilmeli ve disa aktarabilmeli. Engellenen tek sey yazma.
     *
     * Ayrim MESAJA degil KODA bakilarak yapiliyor: mesaj sunucuda
     * degisebilir/yerellestirilebilir, ustelik diger 403'lerin
     * ("Access denied", "Insufficient permissions") `code` alani hic yok.
     */
    class MembershipExpired(
        message: String = "Ücretsiz kullanım süreniz doldu. Hesabınız salt okunur modda; verileriniz duruyor ve dışa aktarılabilir."
    ) : ApiError(message)
}

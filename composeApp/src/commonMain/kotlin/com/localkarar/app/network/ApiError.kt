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
}

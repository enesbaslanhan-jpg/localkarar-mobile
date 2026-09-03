# LocalKarar Mobile — R8 / ProGuard kurallari
#
# R8 bu turda ACILDI (isMinifyEnabled = true). Kapaliyken release APK'si
# tamamen okunabilir kaliyordu.
#
# 🔴 BU DOSYADAKI -keep SATIRLARI SILINMEMELI.
#
# Sebep: kotlinx.serialization derleme zamaninda her @Serializable sinif icin
# bir `Companion.serializer()` uretir ve calisma zamaninda REFLECTION ile bulur.
# R8 bunlari "kullanilmiyor" sanip atarsa ya da yeniden adlandirirsa,
# uygulama DERLENIR, debug'da CALISIR, release'de ise her ag cagrisi
# ayristirma hatasiyla duser. Hata da "Beklenmeyen yanit formati" gibi
# alakasiz bir mesaj olarak gorunur.
#
# Bu tam olarak bu turda kapatilan arizanin ayni sekli: sessiz bozulma,
# yanlis yerde gorunen belirti. Release smoke testi (Faz 6.4) bunu yakalamak
# icin var.

# ---------------------------------------------------------------------------
# kotlinx.serialization
# ---------------------------------------------------------------------------
-keepattributes *Annotation*, InnerClasses, Signature, RuntimeVisible*Annotations, AnnotationDefault

# Uretilen serializer'lari koru
-keepclassmembers class ** {
    *** Companion;
}
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Projenin kendi DTO'lari: alan ADLARI tel uzerindeki JSON anahtarlaridir.
# @SerialName ile eslenmemis alanlarda ad degisirse sozlesme bozulur.
-keep,includedescriptorclasses class com.localkarar.app.**$$serializer { *; }
-keepclassmembers class com.localkarar.app.network.dto.** { *; }
-keepclassmembers class com.localkarar.app.auth.**Dto { *; }

# ---------------------------------------------------------------------------
# Ktor
# ---------------------------------------------------------------------------
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { volatile <fields>; }
-dontwarn io.ktor.**
-dontwarn org.slf4j.**
-dontwarn kotlinx.coroutines.**

# ---------------------------------------------------------------------------
# Kotlin / Coroutines
# ---------------------------------------------------------------------------
-keepclassmembers class kotlin.Metadata { public <methods>; }
-dontwarn kotlin.**

# ---------------------------------------------------------------------------
# androidx.security-crypto -> Google Tink
#
# EncryptedSharedPreferences (SecureStorage.android.kt) Tink'i cekiyor; Tink de
# derleme-zamani annotation'lara (errorprone, javax.annotation) referans veriyor
# ama bunlar calisma zamani sinif yolunda YOK. R8 bunu "eksik sinif" sayip
# `assembleRelease`i tamamen durduruyordu:
#
#   ERROR: Missing class com.google.errorprone.annotations.CanIgnoreReturnValue
#   ERROR: Missing class javax.annotation.Nullable
#
# Bu annotation'lar yalniz derleme zamaninda anlamli; calisma zamaninda
# gerekmiyorlar. Susturmak dogru cozum -- eksik olan gercek bir bagimlilik degil.
# ---------------------------------------------------------------------------
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-dontwarn javax.annotation.concurrent.**

# Tink'e blanket `-keep` VERILMEDI ve verilmemeli.
#
# Once `-keep class com.google.crypto.tink.** { *; }` yazilmisti; bu, hic
# kullanilmayan `KeysDownloader` sinifini da erisilebilir kildi ve o sinif
# Google HTTP istemcisine referans verdigi icin R8 yeni bir eksik sinif
# hatasiyla dustu. Blanket keep, cozdugunden fazla sorun uretiyor.
#
# EncryptedSharedPreferences Tink'i dogrudan cagiriyor, reflection ile degil;
# R8 gerekeni kendisi buluyor. Geriye yalniz uzak anahtar indirme yolunun
# istege bagli bagimliliklarini susturmak kaliyor -- o yol bu uygulamada
# hicbir zaman calismiyor.
-dontwarn com.google.api.client.**
-dontwarn com.google.api.services.**
-dontwarn org.joda.time.**

# ---------------------------------------------------------------------------
# Gunluk temizligi
#
# AppLog release'de zaten susuyor (AppEnvironmentProvider.isRelease), ama
# android.util.Log cagrilari ucuncu parti kutuphanelerden de gelebiliyor.
# Bunlar cikti binary'sinden tamamen atiliyor.
# ---------------------------------------------------------------------------
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

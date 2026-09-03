# Mobil Güvenlik Kararları

**Tarih:** 2 Eylül 2026
**Kapsam:** LocalKarar Mobile (Android + iOS)

Bu belge, **bilerek yapılmayan** güvenlik işlerini ve gerekçelerini tutuyor.
Yapılanlar koddaki yorumlarda; burası "neden yok" sorusunun cevabı.

---

## 1. Sertifika sabitleme (certificate pinning) — YAPILMADI

**Durum:** yok. Ktor motorları platform varsayılan güven zincirini kullanıyor.

**Gerekçe:** trafik Cloudflare'ın arkasından geçiyor ve sertifikayı Cloudflare
yönetiyor. Sabitlenen sertifika/anahtar Cloudflare tarafında döndürüldüğünde
uygulama **topluca ve aynı anda** bağlantı kuramaz hale gelir; düzeltmenin tek
yolu yeni bir mağaza sürümü yayımlamak ve kullanıcıların onu yüklemesini
beklemektir. Yani hata maliyeti "uygulama günlerce çalışmıyor".

Sabitlemenin koruduğu senaryo (kurumsal MITM vekili, sahte kök sertifika
yüklenmiş cihaz) bu ürünün tehdit modelinde birinci sırada değil: kullanıcı
kendi işletme verisini giren bir KOBİ sahibi, saldırgan da cihaza fiziksel ya
da yönetimsel erişim gerektiriyor.

**Yeniden değerlendirme koşulu:** kurumsal (MDM ile yönetilen) cihazlara
dağıtım gündeme gelirse.

---

## 2. Kök / jailbreak tespiti — YAPILMADI

**Durum:** yok. Play Integrity / SafetyNet de yok.

**Gerekçe:** tespit, kararlı bir güvenlik sınırı değil; Magisk gibi araçlar
yaygın kontrolleri rutin olarak atlatıyor. Karşılığında gerçek bedeli var:
yanlış pozitifler (özel ROM kullanan meşru kullanıcı) desteği tıkıyor.

Asıl koruma sunucuda ve orada duruyor: token doğrulaması, `tokenVersion` ile
anında oturum iptali, sahiplik (BOLA) kontrolleri, hız sınırları. Kökle
açılmış bir cihazda kullanıcı **kendi** verisine erişebilir — başkasınınkine
değil.

---

## 3. Uygulama kilidi (biyometrik / PIN) — YAPILMADI

**Durum:** yok.

**Gerekçe:** cihaz kilidi zaten var ve token deposu ona bağlı
(`kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly` / Android Keystore).
İkinci bir kilit, kullanıcının her açılışta ödediği bir maliyet karşılığında
yalnızca "telefonu açıkken başkasının eline geçmesi" senaryosunu kapatıyor.

**Yeniden değerlendirme koşulu:** ürün sahibi isterse; teknik engel yok.

---

## 4. Ekran görüntüsü engelleme — SEÇİCİ olarak yapıldı

**Durum:** `FLAG_SECURE` yalnız kimlik bilgisi girilen ekranlarda (giriş,
kayıt, şifre sıfırlama/değiştirme, hesap silme, pazaryeri kimlik bilgisi
formu). Uygulamanın tamamında **açık değil**.

**Gerekçe:** Türkiye'de ekran görüntüsü paylaşımı günlük kullanım — bir
hesaplama sonucunu muhasebeciye, bir sipariş listesini tedarikçiye göndermek
sıradan. Hepsini engellemek ürünü işini yapamaz hale getirirdi.

Korunan şey kullanıcının kendi verisi değil, **hesabına erişim**.

**iOS'ta karşılığı yok:** platform `FLAG_SECURE` benzeri desteklenen bir API
sunmuyor. `SecureScreen` orada bilerek boş.

---

## 5. `androidx.security:security-crypto` alpha sürümde

**Durum:** `1.1.0-alpha06` (`gradle/libs.versions.toml`).

**Bu turda değiştirilmedi.** Google bu hattı kullanımdan kaldırdı ve kararlı
bir halefi henüz net değil. Sürüm yükseltmek, `EncryptedSharedPreferences`
dosya biçimini değiştirme riski taşıyor — yani mevcut kullanıcıların oturumu
düşebilir.

**Azaltıcı önlem alındı:** `SecureStorage.android.kt` artık kurulum hatasını
yakalıyor. Önceden `MainActivity.onCreate` içinde firlatıp **açılışta çökme
döngüsü** üretiyordu; kullanıcının uygulamayı silmekten başka çaresi yoktu.
Ayrıca depo yedekten dışlandı (`data_extraction_rules.xml`, `backup_rules.xml`),
yani çökmenin bilinen asıl sebebi de ortadan kalktı.

**Yeniden değerlendirme koşulu:** kararlı bir sürüm çıktığında, göç yolu
yazılarak.

---

## 6. Hesap bazlı kaba kuvvet koruması — SUNUCUDA yapıldı

Mobilde değil, olması gereken yerde: `src/services/auth.ts` (10 deneme,
15 dakika kilit) + `prisma/migrations/20260902090000_login_lockout`.

İstemci tarafı bir kaba kuvvet koruması zaten anlamsızdı — saldırgan
uygulamayı hiç kullanmadan uca doğrudan istek atabilir.

---

## 7. Mağaza faturalandırma — "yalnızca giriş yap" modeli (KARAR VERİLDİ)

**Karar (03.09.2026, ürün sahibi):** uygulama Netflix/Spotify'ın izlediği
**reader app** modelini kullanacak.

- Ödeme ve hesap oluşturma **web'de** olur (`localkarar.com`, PayTR).
- Mobil uygulama yalnızca **giriş yapar**.
- Apple ve Google bu modele izin veriyor.

### 🔴 Tek şart vardı ve ihlal ediliyordu

Modelin tek şartı: **uygulamanın içinde satın almaya götüren düğme veya
bağlantı bulunmaması.**

Bu şart ihlal ediliyordu. Üyelik şeridinde "Üyeliği başlat" düğmesi vardı
ve `openExternalUrl` ile `localkarar.com/ayarlar/uyelik` sayfasını
açıyordu. Uygulama içi satın alma olmadığı için güvenli sanılmıştı;
değildi — harici tarayıcıya yönlendirmek de aynı şartın kapsamında.

Kaldırıldı: `LkStatusBanners.kt` (düğme ve `onUyeligiBaslat` parametresi),
`AppShell.kt` (`UYELIK_ADRESI` sabiti ve çağrı yeri).

### Şerit neden duruyor

Şerit **durum bildiriyor**, yönlendirme yapmıyor: "Ücretsiz kullanım
süreniz doldu. Hesabınız salt okunur modda; verileriniz duruyor."
Kullanıcı neden yazamadığını bilmek zorunda — yoksa uygulamayı bozuk
sanar. Süre dolduğunda kapatılamaz, deneme uyarısında kapatılabilir.

### Bundan sonra dikkat edilecekler

- Uygulamaya **fiyat gösteren hiçbir yüzey eklenmemeli**. Karşılama
  ekranı (`WelcomeScreen.kt`) bu yüzden fiyat tablosu taşımıyor; webdeki
  `/app/hosgeldin` taşıyor. Sunucu da fiyat aşamalarını hiçbir uçtan
  sunmuyor — eklenirse bu kural yeniden düşünülmeli.
- Kullanıcının "webden satın alıp gelmesi gerektiğini" bilmesi gerekiyor
  ama uygulama bunu **söyleyemez**. Bu anlatım tamamen web tarafında
  yapılmalı: kayıt e-postaları, site metinleri, `/fiyatlar`.
- İzin verilen harici bağlantılar: yasal metinler (mağazalar zaten
  bekliyor), haber kaynağı adresleri, kullanıcının kendi profil sitesi.

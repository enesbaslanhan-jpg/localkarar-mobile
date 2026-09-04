# Mobil Tasarım — Durum ve Yapılacaklar

**Dal:** `design` · **Commitler:** `8e8d75d`, `5df1363`, `169f926`
**⚠️ Push edilmedi** — `origin/design`'ın 3 commit önünde.

Doğrulama komutu (her değişiklikten sonra):

```
./gradlew :composeApp:compileDebugKotlinAndroid :composeApp:testDebugUnitTest
./gradlew :composeApp:installDebug
```

42 test var, hepsi geçiyor. Emülatör: `Pixel_8`.
**Arka uç ayakta olmalı** — debug yapısı `10.0.2.2:3000`'e gidiyor
(`network/ApiConfig.kt:30`). Kapalıysa uygulama "sunucuya ulaşılamadı" der.

---

## 0. Değişmez kurallar

Bunlar tartışmaya kapalı; ihlal eden değişiklik geri alınır.

1. **Referans sırası: `DESIGN.md` → web → mockup.**
   Mobil kodun mevcut hâli DELİL DEĞİLDİR. Bu turda üç sapma tam olarak
   "mobilde böyle yazıyor" denerek kaçırılmıştı (aşağıda §4).
2. **`DESIGN.md` madde numarası olmayan değer eklenmez.** Eksikse önce
   dokümana madde eklenir, sonra kod yazılır (§0).
3. **Reader-app kuralı**: uygulamada satın almaya götüren düğme, bağlantı
   veya fiyat YOK. Web'in `Ayarlar > Üyelik` bölümü ("ücretinizi görün")
   bu yüzden mobile taşınmadı.
4. **Prototipin renkleri ve fontu kullanılmaz.** `balanced_home_preview.html`
   bir YERLEŞİM taslağıdır; renk/font/tipografi `DESIGN.md`'den gelir.
5. **Erişilebilirlik pazarlık konusu değil** (§19): kontrast ≥4.5:1,
   dokunma hedefi ≥44dp, görünür odak.

---

## 1. Tamamlananlar

### Token katmanı (`ui/theme/`)
| dosya | içerik |
|---|---|
| `Color.kt` | §1.1 brand ailesi, §1.5 semantic, §2.1 yüzeyler, §2.2 metin, §2.3 çizgiler |
| `Type.kt` | §4 mobil sütunu; ağırlıklar `FontVariation` ile eksenden |
| `Shape.kt` | §3.3 |
| `Elevation.kt` | §3.2 gölge kademeleri (yeni) |
| `Motion.kt` | §12 süre ve easing (yeni) |
| `ReducedMotion.kt` + actual'lar | §12 hareket kısıtlama (yeni) |

### Ortak bileşenler (`ui/components/`)
`LkSection`, `LkHairline`, `LkSectionCard` (§10 üç seviye), `LkButton`
(§6 üç boyut/beş varyant), `LkTabs` (§11), `LkProgress` (§11),
`LkBadge` (§11), `LkTactileAction`, `LkPillChip`, `LkSoftDock`.

### Ekranlar
Beş ana sekme yeniden düzenlendi: Ana Sayfa, Hesaplamalar, İşletme Takibi,
Topluluk, Ayarlar. Karşılama ekranına hero kompozisyonu eklendi.

### Bu turda düzeltilen gerçek hatalar
- **Font ağırlığı**: beş font dosyası birebir aynıydı, hepsi tek variable
  fontun kopyasıydı ve varsayılan ekseni 200 (ExtraLight). Tüm yazı tek
  ağırlıkta çiziliyordu.
- **Yerleşim**: `AppShell`'de içerik `Box(fillMaxSize)` içindeydi; Column
  içinde `fillMaxSize` KALAN değil TÜM yüksekliği ister. 50 kaydırılabilir
  ekranın son satırı dock altında kalıyordu.
- **Kontrast**: seçili hap/sekme `LkPrimary` + beyaz kullanıyordu; koyu
  temada 1.9:1. `primaryFill` ile 4.6:1.
- **Para kırpma**: `formatTry` `toLong()` ile kırpıyordu (1416,67 → ₺1.416);
  fiyatlandırma aracında hedef marjın altında fiyat gösteriyordu.

---

## 2. Yapılacaklar

### 2.1 — Kalan ekranlar (öncelik: yüksek)

Beş sekme bitti; şu ekranlar hâlâ eski dilde (kart yığını, `LkSection`
kullanmıyor):

Akademi (Kurslar), Kurs Detayı, Ders Okuyucu, AI Mentor, Karar Araçları,
Karar Oturumu, Haberler, Haber Detayı, Bildirimler, Hakkında, Kılavuz,
Giriş, Kayıt.

**Her biri için yöntem:**
1. Bölümleri `LkSection` ile aç — çerçeve YOK, satırları `LkHairline` ayırır.
   Kart yalnızca kendi başına duran, tıklanabilir bir nesne için
   (`LkSectionCard`).
2. Elle yazılmış buton/sekme/hap varsa ortak bileşenle değiştir.
3. `Icons.Default.*` kalmışsa `Icons.Outlined.*` yap (beğeni/yer imi
   dolu-çizgi çiftleri hariç).
4. Emülatörde açık ve koyu temada kontrol et.

### 2.2 — Bildirimlerin birleştirilmesi (öncelik: yüksek)

Mobilde ÜÇ bildirim ekranı var, webde bir tane:
- `ui/screens/settings/AccountNotificationsScreen.kt`
- `ui/screens/community/NotificationsScreen.kt`
- `ui/screens/workspaces/NotificationsScreen.kt`

Web deseni (`frontend/src/pages/NotificationsPage.jsx`): hesap + topluluk
TEK ekranda iki bölüm ("ÜYELİK VE ÖDEME", "TOPLULUK"). İşletme Takibi
bildirimleri webde de ayrı sekmede, ONA DOKUNULMAZ.

`Destination.AccountNotifications` ve `Destination.CommunityNotifications`
tek hedefe iner; `NavController.kt` ve `AppShell.kt` çağrı yerleri güncellenir.

⚠️ Hesap ve topluluk satırları AYRI composable olarak yazılmalı. Webde tam
burada bir ızgara hatası olmuştu: 3 öğeli satır 4 sütunlu ızgaraya düşüyordu.

### 2.3 — Giriş ekranı (öncelik: orta)

- Google/Apple düğmeleri webdeki gibi: görünür ama **devre dışı**,
  "Yakında" etiketiyle. Web karşılığı `frontend/src/pages/AuthPage.jsx`.
- Karşılama ekranındaki hero dili giriş/kayıt akışına da uygulanabilir
  (`WelcomeScreen.kt` içindeki `HosGeldinHero` örnek alınabilir).
  Bu dil YALNIZ giriş öncesi akışa aittir; çalışma ekranlarına girmez.

### 2.4 — Akademi içeriği (öncelik: orta)

Mockup'ta "Sertifika: Dahil" ve "Video Modül" yazıyor. **KULLANILMAZ** —
arka uçta sertifika alanı yok ve dersler metin tabanlı (`/courses` yanıtı:
`sourceType: canonical-v1`). Ders sayısı ve süre gerçek alanlardan gelir
(`lessonCount`, `estimatedMinutes`).

### 2.5 — Para bileşeni yayılımı (öncelik: düşük)

`formatTry` düzeltildi ama mobil tarafta `LkFormatting.formatMoney` ayrı
bir yol. İkisinin aynı yuvarlama davranışını verdiği doğrulanmalı; farklıysa
tek yerde toplanmalı.

### 2.6 — iOS (öncelik: düşük ama BİLİNMEZ)

**iOS hiç çalıştırılmadı.** Derleniyor ama cihazda/simülatörde görsel
doğrulama YOK. Özellikle iki yeni `actual` doğrulanmalı:
- `ui/theme/ReducedMotion.ios.kt` — `UIAccessibilityIsReduceMotionEnabled`
- `Type.kt`'deki `FontVariation` — iOS'ta eksen örneklemesi çalışıyor mu

Bu ikisi çalışmıyorsa iOS'ta tüm yazı yine ExtraLight çıkar.

---

## 3. Bilinen eksikler

- §11'in `Skeleton`, `Toast`, `Tooltip` bileşenleri YOK. Gerçek kullanımları
  olmadığı için yazılmadı; ihtiyaç çıkınca §11'e göre eklenir.
- Açık temada `surfaceHighlight` ve `surfaceElevated` ikisi de `#FFFFFF`;
  §2.1 bunları ayırıyor ama mobilde ayrı kullanım yok.
- `DESIGN.md` §2.1 tablosu mobildeki yeni yüzey değerleriyle GÜNCELLENMELİ
  (gerekçe ve ölçümler `Color.kt` başında). Yoksa web ve mobil ayrışır.

---

## 4. Bu turda yakalanan referans hataları — tekrarlanmasın

Üçü de "mobil kodda böyle yazıyor" denerek kaçırılmıştı:

1. **"Finansal Görünüm" sekmesi** Hesaplamalar'da duruyordu. Webde BİLEREK
   silinmişti (`ToolsPage.jsx:219`): dört bloğundan üçü İşletme Takibi
   verisini tekrarlıyordu. Mobil aynı gereksiz iki isteği de yapıyordu.
2. **`awaitingDirection`** sunucudan geliyordu, web Ana Sayfa'da
   gösteriyordu, mobilin DTO'sunda alan bile yoktu. Tutarı olan ama yönü
   belirsiz kayıtlar mobilde hiçbir yerde görünmüyordu.
3. **Topluluk sekmeleri** webdeki sıradan farklıydı ve "Takip ve engelleme"
   bölümüne "Kişiler" denmişti — aynı şeye iki platformda iki ad.

**Kural:** yeni bir ekrana başlamadan önce webdeki karşılığını aç, IA'yı ve
adlandırmayı oradan al.

# Mobil Tasarım — Baştan Tasarım Turu

**Dal:** `design` · **Arşiv:** `archive/codex-yarim` (`ae1e320`)

Doğrulama komutu (her değişiklikten sonra):

```
./gradlew :composeApp:compileDebugKotlinAndroid :composeApp:testDebugUnitTest
./gradlew :composeApp:installDebug
```

42 test var, hepsi geçiyor. Emülatör: `Pixel_8`.
**Arka uç ayakta olmalı** — debug yapısı `10.0.2.2:3000`'e gidiyor
(`network/ApiConfig.kt:30`). Kapalıysa uygulama "sunucuya ulaşılamadı" der.

---

## 0. Referans sırası — BU TURDA DEĞİŞTİ

Önceki turlarda model "renk + font + tipografi webden, yalnız düzen farklı"
idi. **Ürün sahibi kararı (05.09.2026) bunu değiştirdi.** Hedef: dünya
çapındaki finans uygulamalarıyla yarışacak bir arayüz. Yerleşik düzenin
hiçbir bağlayıcılığı yok.

| katman | kaynak |
|---|---|
| renk | **web** — `DESIGN.md` §1.1 brand ailesi, §1.5 semantic |
| tipografi, yerleşim, hareket, bileşen davranışı | **mockup** (aşağıda) |
| bilgi mimarisi, adlandırma, alan isimleri | **web** — görsel dil değil, YAPI |

Mobil kodun mevcut hâli **delil değildir**. Önceki turda tam olarak
"mobilde böyle yazıyor" denerek üç sapma kaçırılmıştı (§5).

### Mockup

Tasarımın tamamı — bileşen föyü + 54 ekran, açık/koyu tema, dört animasyon
çalışır hâlde:

**https://claude.ai/code/artifact/0be0e453-b050-44dd-8fc6-a0424f7d0c88**

Bir ekranı yazmadan önce mockup'taki karşılığı açılır. Mockup HTML'dir,
Kotlin değil — **ölçü, ritim ve desen** alınır, kod değil.

---

## 1. Aşama 0 — TAMAMLANDI

Codex'in yarım tasarım turu (55 dosya) `archive/codex-yarim` dalına alındı;
`design` dalına yalnızca baştan tasarımın geçersiz kılmadığı parçalar döndü:

| korunan | neden |
|---|---|
| `AndroidManifest.xml` (simge bağlantısı) | uygulama simgesi Android varsayılanından gerçek marka işaretine çevrilmişti; geri alınsa boş Android ikonuna dönerdi |
| `res/drawable/local_karar_mark.png` | manifest'in referansı |
| `composeResources/drawable/local_karar_mark.png` | `LkBrandMark`'ın kaynağı |
| `ui/components/LkBrandMark.kt` | henüz kullanılmıyor; giriş akışında kullanılacak |
| `iosApp/.../app-icon-1024.png` | iOS uygulama simgesi |
| `PRODUCT.md`, `.impeccable/` | ürün dokümanı ve ekran arşivi, kod değil |

21 ekran düzenlemesi geri alındı — hepsi bu turda yeniden yazılacak.

⚠️ Arşiv dalı **silinmeyecek**. Bir şeye ihtiyaç çıkarsa:
`git checkout archive/codex-yarim -- <yol>`

---

## 1b. Aşama 1, 2, 3 — TAMAMLANDI

| commit | ne |
|---|---|
| `fbff520` | Aşama 1 — token katmanı §24'e göre |
| `1b5062b` | Aşama 2 — `LkHero`, `LkSheet`, 8 temel bileşen |
| `1d6efda` | Ana Sayfa |
| `0436d37` | İşletme Takibi + Profil (`LkCoverHeader`) |
| `5a7a727` | Hesaplamalar + Giriş + Topluluk akışı |

Altı ana ekranın hepsi hero + binen yüzey düzeninde. Derleme temiz,
42 test geçiyor.

### ⚠️ HENÜZ YAPILMAYAN: görsel doğrulama

**Hiçbir ekran emülatörde görülmedi.** Derleme ve testler tek kanıt;
renkler, hizalar, taşmalar doğrulanmadı. Ürün sahibi doğrulamayı sona
bırakmayı seçti (05.09.2026). Emülatör turu yapılmadan kalan 48 ekrana
geçilmemeli — sistemsel bir hata varsa hepsinde birden çıkar.

### Bu turda bulunan gerçek hatalar

1. `Elevation.kt` `isSystemInDarkTheme()` okuyup `ThemeController`'ı
   atlıyordu; kullanıcı açık tema seçtiğinde ama sistem koyuyken açık
   temaya koyu tema gölgesi çiziliyordu.
2. `HomeScreen.formatMoney` `"₺${amount.toInt()}"` idi — hem kırpıyor hem
   binlik ayracı koymuyordu. ₺182.450,67 → "₺182450".
3. Mockup'ın yüzey merdiveni ölçümde geriliyordu (1.134 < 1.20);
   alınmadı, §24.7'ye yazıldı.

### Bekleyen karar: görüntü yükleme kütüphanesi

`coverUrl` ve `avatarUrl` sunucudan geliyor ve DTO'da var, ama projede
**hiçbir görüntü yükleme kütüphanesi yok** — ne Coil, ne Kamel. Kapak ve
avatar şu an kodla çiziliyor (gradyan + baş harf). Gerçek fotoğraf için
Coil 3 gibi bir bağımlılık kararı gerekiyor; §24 kapsamında değil.

---

## 2. Aşama 1, 2, 3 — planlanan içerik (referans)

### Aşama 1 — Token katmanı (`ui/theme/`)

- **Renk**: brand ailesi webden değişmeden. **Nötr basamak mobilde ayrışıyor**
  — şu an açık temada `surfaceHighlight` ve `surfaceElevated` ikisi de
  `#FFFFFF`, yükseltilmiş kart zeminden ayrılmıyor. Yeni basamak:

  | rol | açık | koyu |
  |---|---|---|
  | canvas | `#EEF1F4` | `#0F1316` |
  | surface | `#F7F9FA` | `#171C21` |
  | raised | `#FFFFFF` | `#1F252B` |
  | overlay | `#FFFFFF` | `#272E35` |

- **Gölge**: açık temada yumuşak geniş gölge; **koyu temada gölge YOK**,
  yüzey tonu yükseltmesi + üst kenarda 1dp `rgba(255,255,255,.06)` ışık.
  Siyah zeminde siyah gölge görünmez. `Elevation.kt` bunu tek yerde çözer;
  bileşenler kademe adı ister, ham değer değil.
- **Tipografi**: Manrope kalır. Ölçek sertleşiyor —
  display 40/720 · titleL 26/700 · titleS 18/650 · body 15/500 ·
  label 13/600 · caption 11/500. Para ve oranlar **tabular figürlerle**;
  orantılı rakamlarla canlı güncellenen tutarlar zıplar.
- **Ölçü**: boşluk 4·8·12·16·20·24·32·40·56. Yarıçap: küçük 10 · kart 20 ·
  çekmece 28 · hap tam yuvarlak. **Kart iç dolgusu 20dp** (şu an 14-16).

### Aşama 2 — Bileşen katmanı

Yeniden yazılan: `LkCard`, `LkButton`, `LkChip`, `LkTabs`.
Yeni: `LkHeroHeader`, `LkSheet`, `LkMetric`, `LkIconTile`, `LkListRow`,
`LkCoverHeader`, `LkCollapsingBar`, `LkSkeleton`, `LkToast`,
`LkSuccessTick`, `LkConfetti`.

**`LkHeroHeader` uygulamanın karakterini taşıyor**: marka renginde başlık
bloğu + üstüne binen 32dp yarıçaplı yüzey. Referans kitlerin dördünde de
bu desen var. Gradyan webin `--auth-gradient`'ından:
`#060F14 → #0E2530 → #1B4356 → #275C72 → #2F6A82`, 158°.

⚠️ Mockup'ta yakalanan hata: özet kartına negatif üst kenar boşluğu verilip
kaydırma kabının üstüne taşırılırsa **kabın taşma kırpması kartın üstünü
keser**. Kart tümüyle yüzeyin içinde durur; derinliği gölge taşır.

**`LkSheet` en riskli parça.** Üç kademe (peek/half/full),
`AnchoredDraggableState` ile. Material3 `ModalBottomSheet` bunu **veremez**:
iki kademesi var ve karartmayla kapanır. Mockup'ta jest çakışmasını
önlemek için sürükleme yalnızca tutamak ve başlıkta başlıyor — aynısı
uygulanır. Oturmazsa iki kademeli sürüme düşülür, jest yerine düğme konur.

### Aşama 3 — Altı ekran, sonra kalan 48

Önce: Ana Sayfa, İşletme Takibi, Topluluk akışı, Profil, Hesaplamalar, Giriş.
Bunlar oturunca kalan 48 ekran aynı dille akar; mockup'ta hepsinin
karşılığı var.

---

## 3. Değişmez kurallar

Bunlar tartışmaya kapalı; ihlal eden değişiklik geri alınır.

1. **Reader-app**: uygulamada satın almaya götüren düğme, bağlantı veya
   fiyat YOK. Web'in `Ayarlar > Üyelik` bölümü ("ücretinizi görün") bu
   yüzden taşınmadı. Üyelik satırı **durumu gösterir**, satın almaya götürmez.
2. **Erişilebilirlik** (§19): kontrast ≥4.5:1, dokunma hedefi ≥44dp,
   görünür odak. **Ölçülür, tahmin edilmez.**
   - Ölçülmüş: `#306D88` üzerinde beyaz **5.72:1**. Opaklık düşünce kırılıyor:
     %85 → 4.65:1 (geçer), **%75 → 4.01:1 (kalır)**, %65 → 3.43:1.
     **Kural: hero başlık içinde hiçbir metin %85 beyaz opaklığın altına
     inmez.** Soluk etiket isteniyorsa opaklık değil, ayrı bir açık ton.
   - Durum **tek başına renge yaslanmaz**: takvimde vade noktası, kritik
     stokta ayrı etiket, "davet bekliyor" soluk renk değil kendi rozeti.
3. **`DESIGN.md`'ye §24 "Mobil tasarım dili" eklenecek.** Mobilin ayrıştığı
   her değer madde numarasıyla oraya yazılır. §0 kuralı: **numarasız değer
   eklenmez.**
4. **Hareket kısıtlaması pazarlık konusu değil.** Sayaç, konfeti, skeleton
   parlaması ve çekmece yayı `ReducedMotion` açıkken tamamen atlanır —
   son değer doğrudan yazılır.
5. **Marka işareti temayla dönmez.** Renkleri bilerek sabit; açık ve koyu
   temada aynı görünür.

---

## 4. Dört zorunlu animasyon

Ürün sahibi listesi. Hepsi Compose'da **dış bağımlılık olmadan** yazılır;
Lottie bu turun kapsamında değil.

| # | animasyon | teknik ve tuzak |
|---|---|---|
| 1 | Sayı sayacı | `Animatable`. **Tabular figürler şart.** Yalnızca değer gerçekten değiştiğinde tetiklenir — her yeniden bileşimde baştan sayarsa ekran her dokunuşta titrer. |
| 2 | Skeleton | `infiniteTransition` + `Brush.linearGradient` parlama |
| 3 | Başarı | tik: `PathMeasure` yol çizimi. Konfeti: `Canvas` parçacık, ~150 satır. **Yalnızca gerçek tamamlanma anlarında** (kayıt kapatma, karar oturumu bitişi) — her kaydetmede değil, yoksa ciddi bir finans aracı oyuncak gibi görünür. |
| 4 | Çekmece yayı | `spring(dampingRatio = .82f)` |

Marka işaretinin kendi animasyonu ayrı: pusula iğnesi −28°'den gelip 6°'ye
taşarak yerine oturur, yay aynı anda çizilir, 700ms. Karşılama ekranında
aynı hareketin büyütülmüş hâli var: 24 taksimatlı pusula kadranı, yön oku
−142°'den dönüp kuzeyi buluyor. **Tek seferlik** — sürekli dönen kadran
dekoratif gürültü olurdu.

⚠️ Webde ve mockup'ta iki kez yakalanan tuzak: **aynı animasyon adı yeniden
tetiklenmez.** CSS'te sınıfı kaldırıp reflow zorlamak gerekiyordu;
Compose'da karşılığı `key()` ile yeniden başlatmaktır.

---

## 5. Önceki turda yakalanan referans hataları — tekrarlanmasın

Üçü de "mobil kodda böyle yazıyor" denerek kaçırılmıştı:

1. **"Finansal Görünüm" sekmesi** Hesaplamalar'da duruyordu. Webde BİLEREK
   silinmişti (`ToolsPage.jsx:219`): dört bloğundan üçü İşletme Takibi
   verisini tekrarlıyordu. Mobil aynı gereksiz iki isteği de yapıyordu.
2. **`awaitingDirection`** sunucudan geliyordu, web Ana Sayfa'da
   gösteriyordu, mobilin DTO'sunda alan bile yoktu. Tutarı olan ama yönü
   belirsiz kayıtlar mobilde hiçbir yerde görünmüyordu.
3. **Topluluk sekmeleri** webdeki sıradan farklıydı ve "Takip ve engelleme"
   bölümüne "Kişiler" denmişti — aynı şeye iki platformda iki ad.

**Kural:** yeni bir ekrana başlamadan önce webdeki karşılığını aç, bilgi
mimarisini ve adlandırmayı oradan al. Görsel dili mockup'tan al.

---

## 6. Bilinen boşluklar

- **`coverUrl` mobil DTO'sunda var ama `ProfileScreen` çizmiyor.** Kapak
  fotoğrafı için sunucu tarafında yeni alan gerekmiyor.
- **iOS hiç çalıştırılmadı.** Derleniyor ama cihazda görsel doğrulama YOK.
  İki `actual` doğrulanmalı: `ui/theme/ReducedMotion.ios.kt`
  (`UIAccessibilityIsReduceMotionEnabled`) ve `Type.kt`'deki
  `FontVariation` eksen örneklemesi. Çalışmıyorsa iOS'ta tüm yazı
  ExtraLight çıkar.
- §11'in `Skeleton`, `Toast`, `Tooltip` bileşenleri hâlâ yok.
- Altı ekran yeni dile geçtiğinde kalan 48 ekran geçici olarak eski dilde
  kalır; ara durumda uygulama iki dilli görünür. Kabul edilen maliyet.
- **Akademi**: mockup'ta "Sertifika" ve "Video Modül" YOK — arka uçta
  sertifika alanı yok ve dersler metin tabanlı (`sourceType: canonical-v1`).
  Referans kitlerde vardı, bilerek alınmadı.

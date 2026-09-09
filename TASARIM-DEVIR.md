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

- **iOS hiç çalıştırılmadı.** Derleniyor ama cihazda görsel doğrulama YOK.
  İki `actual` doğrulanmalı: `ui/theme/ReducedMotion.ios.kt`
  (`UIAccessibilityIsReduceMotionEnabled`) ve `Type.kt`'deki
  `FontVariation` eksen örneklemesi. Çalışmıyorsa iOS'ta tüm yazı
  ExtraLight çıkar.
- **Karşılama ekranının pusula kadranı** (24 taksimat, −142°'den dönen yön
  oku) yazılmadı. Marka işaretinin kendi giriş hareketi var
  (`LkBrandMark(hareketli = true)`), kadran yok.
- **Mentor girişindeki "verinden çıkan sorular"** mockup'ta var, uygulamada
  yok. Sunucuda işletme verisinden soru türeten bir uç bulunmuyor; uydurma
  soru yazmak yerine boş bırakıldı.
- **Akademi**: mockup'ta "Sertifika" ve "Video Modül" YOK — arka uçta
  sertifika alanı yok ve dersler metin tabanlı (`sourceType: canonical-v1`).
  Referans kitlerde vardı, bilerek alınmadı.
- **Hesabı sil ekranındaki sayılar** mockup'ta rakamlı ("3 işletme ve 54
  kayıt"); sunucuda silinecekleri önceden sayan uç yok, kalemler rakamsız
  listeleniyor.

---

## 7. Uygulama durumu — 5 Eylül 2026

Mockup taraması sonrası kapatılan eksikler:

### Dört zorunlu animasyon — dördü de bağlı

| # | durum |
|---|---|
| 1 Sayaç | `rememberLkSayac` ortak kancaya alındı; Ana Sayfa, İşletme Takibi hero'su ve Kayıtlar hero'su sayarak yükseliyor |
| 2 Skeleton | `LkLoadingState` **yeniden yazıldı**: dönen halka gitti, yerine üç desen geldi (`LISTE` · `DETAY` · `FORM`). 35 ekran iskelet gösteriyor; `LkSkeleton` daha önce yazılıp hiç çağrılmamıştı |
| 3 Başarı | `LkSuccessTick` (iki parçalı çizim) + `LkConfetti` (sabit tohumlu parçacık) + `LkSuccessPanel`. Tik hesaplama tamamlanınca, konfeti YALNIZ olumlu karar sonucunda |
| 4 Çekmece yayı | zaten vardı (`spring(dampingRatio = .82f)`) |

Marka işaretinin giriş hareketi de eklendi: −28°'den gelip 6°'yi aşarak
oturuyor, giriş ve kayıt ekranlarında tek seferlik.

### Eksik olan ekran ve bölümler

- **E-posta doğrulama** (mockup "Giriş 5") yoktu. `AuthenticatedVerificationScreen`
  adında ölü bir dosya vardı, hiçbir yerden çağrılmıyordu; silindi.
  Yerine `EmailVerificationScreen`: adres, 6 haneli kod alanı, 60 saniyelik
  geri sayımlı "Tekrar gönder". Uçlar (`/auth/email/verify-request`,
  `verify-confirm`) aylardır yazılıydı ve commonMain'de hiç çağrılmıyordu.
  ⚠️ Mockup "bağlantı" diyor, sunucu **kod** gönderiyor ve bunu bilerek
  yapıyor (`auth.ts`); akış sunucudan, yerleşim mockup'tan alındı.
- **İşletme ayarlarında TEHLİKELİ ALAN** yoktu. Silme yalnız "İşletmelerim"
  listesindeydi; mockup onu işletmenin kendi ayarlarına koyuyor.
- **Hesabı sil**: ne silineceği tek tek sayılıyor.
- **Formül bloğu** (mockup "Hesap 2") hiç yoktu: ekran girdileri ve sonucu
  gösteriyor ama hesabın kendisini yazmıyordu. `FormulaExpressions.kt`
  18 formülün matematiğini taşıyor — **hepsi sunucunun
  `src/services/formulas.ts` gövdesinden çevrildi, uydurulmadı.**
- **Kayıtlar'da yön süzgeci** (`Tümü · Tahsilat · Ödeme · Yönü belirsiz`)
  ve hero'da iki sayaç (`counts.open`, `counts.awaitingDirection`).
  §5'te "tekrarlanmasın" denen `awaitingDirection` sapmasının son ayağı.
- **Ders okuyucu**: gövde 65 karakter ölçüsünde (`LkOkumaGenisligi`),
  satır yüksekliği 1,7. `MarkdownViewer` varsayılanlarıyla bırakılmıştı ve
  gövde **Manrope değil Material'in kendi ölçeğiyle** çiziliyordu.
- **`LkToast` ve `LkTooltip`** yazıldı; §11'in son iki eksiği kapandı.
- **`LkSegment`** (hap içinde hap) eklendi; Ayarlar'daki tema seçimi
  ayrı hapken segment oldu.

### Sistem dışına düşen ekranlar temizlendi

Ham `Card` (13 ekran) ve ham `OutlinedTextField` (13 ekran) kalmadı:
hepsi `LkCard` / `LkRowGroup` / `LkTextField` / `LkNotice`'a geçti.
Yasal izinler, Hesabı sil, Profili düzenle, E-posta ve Parola değiştir
ekranları mockup'ın "tek yüzeyde gruplanmış liste" desenine alındı.

### Bulunan gerçek hatalar

1. **`LkTextField` `trailingContent` parametresini alıp hiç çizmiyordu.**
   Dört çağıran taraf onu geçiriyordu ve dördü de sessizce kayboluyordu:
   parola alanının göz düğmesi, tarih seçicinin takvim ikonu, sayısal
   alanın birim eki, karar girdisinin son eki.
2. **Entegrasyonlar ham ISO damgası basıyordu** ("Son eşitleme:
   2026-09-05T09:41:12.482Z"). `formatTimeAgo` zaten vardı.
3. **Kayıtlar'daki hap şeridi bir `LazyColumn` içinde `weight(1f)`
   alıyordu**: ekranın altıda biri süzgeç şeridine gidiyordu.
4. **Kurslarda ilerleme** 6dp düz çubuktu; oran hiçbir yerde rakamla
   yazmıyordu (`LkProgressPill` hazırdı, kullanılmıyordu).
5. **Haber detayındaki panel adı** "Neden Önemli?" idi; mockup ve web
   "SENİ NASIL ETKİLER" diyor — §5'in "aynı şeye iki ad verilmez" kuralı.

### Doğrulama

`compileDebugKotlinAndroid` temiz, 42 test geçiyor. **Emülatör turu
yapıldı** (Pixel_8, arka uç ayakta): Ana Sayfa, İşletme Takibi, Kayıtlar,
Ayarlar, İşletme ayarları ve Fiyat Mimarisi ekranları açıldı; iskelet,
sayaç, formül bloğu, yön süzgeci, tehlikeli alan ve başarı tiki cihazda
görüldü. Hesaplama sonucu formül bloğuyla tutarlı çıktı
(950 ÷ 0,52 = 1.826,92).

Kalan: Topluluk, Akademi, Mentor ve Haberler ekranlarının emülatörde
tek tek gezilmesi.

---

## 8. Emülatör turu sonrası düzeltmeler — 6 Eylül 2026

Ürün sahibi uygulamayı cihazda gezdi; çıkan liste tek tek kapatıldı.

### Çalışmayan şeyler

| ne | sebep |
|---|---|
| **Kişiler ekranı hiç açılmıyordu** | `BusinessContactDto.workspaceId` ZORUNLU alandı, sunucu (`workspace.ts:1114`) o alanı hiç göndermiyor. Seri hale istisna atıyor, ekran "İşletme yüklenemedi" diyordu. |
| **Ürünler "İşletme yüklenemedi"** | `products[].tags` `null` geliyordu; DTO'da varsayılan var ama açık `null` varsayılanı kullanmıyor. Çözüm ekran bazlı değil: bütün `Json` yapılandırmalarına **`coerceInputValues = true`**. |
| **AI Mentor "Yeni sohbet" ham hata basıyordu** | Sunucu `{ conversation: {...} }` zarfı dönüyor (`conversation.ts` POST `/`, PATCH `/:id`), mobil zarfsız çözüyordu. `ConversationEnvelopeDto` eklendi. |
| **Ekranda `PROFITABILITY_INPUTS_INCOMPLETE` yazıyordu** | `parseUserFacingErrorMessage` makine kodunu insan mesajına TERCİH ediyordu (`errorVal ?: msgVal`). Artık önce `message`; SCREAMING_SNAKE bir metin hiçbir zaman ekrana düşmüyor. |
| **Sol üstteki geri oku çalışmıyordu** | Hesaplamalar, İşletme Takibi, Topluluk birer KÖK; `navigateTo` yığını sıfırlıyor, `popBackStack()` hep `false`. Ok artık yalnız dönülecek yer varken çiziliyor. |
| **Takvimdeki "bugün" tuşu** | Yalnız ay/yılı değiştiriyordu; zaten bu aydaysan hiçbir şey olmuyordu. Artık bugünü SEÇİYOR, ekran ilk açılışta da bugünle geliyor. |
| **İşletme ayarlarında hiçbir şey tıklanmıyordu** | Para birimi dışında düzenlenebilir alan yoktu. İşletme adı yerinde düzenleniyor (`PUT /workspaces/:id`). |
| **Hesaplamalardaki üç kısayol aynı yere gidiyordu** | Üçü de `WorkspaceHome`'a atıyordu. Artık: Kayıt ekle → `RecordEdit`, Fatura ve belgeler → `Documents`, Ödeme takvimi → `Calendar`. |
| **Parola sıfırlamada 8 karakter kabul ediliyordu** | Sunucu 10 istiyor (`PASSWORD_MIN`). İstemci artık 10. |
| **`LkTextField` `trailingContent`'i çizmiyordu** | Dört çağıran taraf geçiriyor, dördü de kayboluyordu (parola gözü, takvim ikonu, birim eki). |

### Tasarım kararları

- **Sipariş ve Ürünler'deki minik hap şeritleri** (11sp, ~24dp — §19 eşiği 44dp; Ürünler'de dört sıra) tek bir `LkFilterBar`'a indi: bir sıra ana filtre + "Filtreler" arkasında ikincil gruplar, rozet kaç tanesinin varsayılandan farklı olduğunu söylüyor.
- **Bölüm seçici** artık her alt ekrandan açılıyor: zaten açık olan İşletme Takibi sekmesine ikinci kez dokunmak seçiciyi getiriyor.
- **Karar oturumu**: seçenekli soru varsa adım adım (mockup "Karar 2"), yoksa hepsi tek formda — sayısal girdilerde ekran boş kalıyordu.
- **Ürün Merkezi** kart ızgarasından gruplanmış listeye geçti; mockup'ta böyle bir ekran yok, uygulamadaki eşdeğeri olan "İşletme Bölümleri" ile aynı desene alındı.
- **Gündemde** akışın tepesinden alınıp kendi "Trend" sekmesine taşındı (aynı dört gönderiyi iki kez gösteriyordu).
- **Ayarlar > Görünüm** en üste taşındı (mockup "Ayar 1").
- **Profili düzenle** mockup "Ayar 2"ye açıldı: kapak (`POST /auth/cover`), hakkında, konum, site. *Kullanıcı adı YOK — sunucuda `username` alanı yok.*
- **Hakkında** mockup "Ayar 8"e geçti: marka işareti, GERÇEK sürüm (`AppEnvironmentProvider.versionLabel`), üç belge satırı.
- **Destek** mockup "Ayar 7"ye geçti: önce SIK SORULANLAR (sekiz soru, webin kendi çevirisinden birebir), sonra form.
- **Giriş 1 — Karşılama** eklendi: kodla çizilen eşmerkezli halkalar + 24 taksimatlı pusula, tek seferlik. Uygulama artık doğrudan forma açılmıyor.
- Mentor konuşması ve topluluk sohbetinde alt dock gizleniyor; yazma alanının üstüne biniyordu.

### Sırada bekleyenler (ürün sahibi listesi, 6 Eylül)

1. Topluluk profili: kapak fotoğrafı yok, üstte iki mavi boşluk, profil fotoğrafı yok — webdeki düzene geçecek.
2. Siparişlerde "Filtreler" paneli üstteki bir şeyi kapatıyor.
3. Kurslar: karta basınca içerik açılmıyor; içerik mockup düzeninde değil.
4. Uygulama simgesi (mavi çerçeve + ortada beyaz kare) düzeltilecek.
5. İşletme ayarları webdeki ayarların tamamını taşımıyor.
6. Web ile mobil arasında "varmış gibi yapılan" ve eksik olan her şeyin denetimi.

---

## §9 — Ürün sahibi listesi (6 Eylül turu)

§8'in sonundaki altı maddenin ilk beşi yapıldı; altıncısı (tam denetim) açık.

| Madde | Ne çıktı | Ne yapıldı |
| --- | --- | --- |
| **1. Topluluk profili** | Ekran `LkHeroPage` içindeydi: hero'nun mavi başlığı + kapak bandının mavisi = **iki mavi bant**, ve hero "Profil" yazarken kapak da adı yazıyordu. Kendi profilimde kapak da avatar da **hiç yoktu** (`OwnProfileContent` doğrudan sayaç satırıyla başlıyordu). Üstelik akışta kendi adıma dokununca profil **yabancı profil dalına** düşüyor, kendime "Takip Et / Engelle" çıkıyordu. | Profilde **başlık kapaktır**: hero kaldırıldı, geri oku kapağın üzerinde yüzüyor. Kendi profili de aynı kapak/avatar/ad/bio/sayaç bloğunu çiziyor, bilgiler oturumdaki `user`dan geliyor (web `ProfilePage.jsx` da öyle yapıyor). `benimMi = userId == null \|\| userId == currentUser.id`. Sekmeler webdeki dörde çıktı: Paylaşımlarım / **Medya** / Beğenilerim / Kaydettiklerim (medya ucu vardı, mobil çağırmıyordu). "Profili düzenle" Ayarlar > Profil'e gidiyor. |
| **2. "Filtreler" paneli** | Panel satır içinde açılıyor, şerit ile listenin **arasına** giriyordu: liste aşağı kayıyor, bakılan satır ekrandan çıkıyordu. | Panel **alttan açılan katmana** taşındı (`Dialog`, tutamak + "Temizle" + "Bitti"). Arkadaki hiçbir şey yerinden oynamıyor. |
| **3. Kurslar** | İçerik AÇILIYORDU ama okunmuyordu. İki neden: **(a)** GFM **tabloları** çiziciye tanıtılmamıştı, hücreler alt alta paragraf oluyordu ("Maliyet Bileşeni / Sinem'in Varsaydığı / 120 TL / 120 TL"); **(b)** **LaTeX hiç çizilmiyordu** — `$48.000 \div 160 = 300$` ekranda YOK oluyor, geriye "İşçilik Saat Ücreti:" gibi boş başlıklar kalıyordu. Web ikisini de çiziyor (`remark-gfm`, `remark-math` + `rehype-katex`). | Ders gövdesi dış kütüphaneden (mikepenz) uygulamanın kendi çizicisine (`LkMarkdown`, `okuma = true`) alındı: matematik zaten yazılmış olan `LkMath` ile çiziliyor, tablolar için `LkMarkdownTable.kt` yazıldı (2 sütun → etiket/değer listesi, 3+ sütun → yatay kaydırılan tablo + kaydırma ipucu). **Madde içindeki** matematik de artık çiziliyor — hesapların çoğu maddede duruyordu. |
| **4. Uygulama simgesi** | Manifest düz bir bitmap gösteriyordu (`@drawable/local_karar_mark`). Android 8+ uyarlanabilir simge bekliyor; düz bitmap beyaz bir rozetin içine küçültülüyordu. | `mipmap-anydpi-v26/ic_launcher(.round)` + vektör zemin (marka gradyanı B700→B500), vektör ön plan (C halkası + baklava + nokta) ve Android 13 için `monochrome` katmanı. Emülatörde doğrulandı. |
| **5. İşletme ayarları** | Webdeki üç karttan (profil / e-Fatura gelen kutusu / tercihler) mobilde yalnız **işletme adı ve para birimi** vardı; saat dilimi ile dil "web sürümünden yapılır" notuyla salt okunurdu, gelen kutusu hiç yoktu. `taxNumber` sunucudan geliyor ama DTO'da alan olmadığı için sessizce düşüyordu. | Üç bölüm de eklendi: **İşletme profili** (ad, unvan, vergi/TC no, sektör, şehir, aşama, çalışan sayısı, satış kanalları, hedef, zorluklar) + **finansal özet** (aylık satış/gider, nakit, borç); **e-Fatura gelen kutusu** (adres aç/yenile/kapat, kopyalanabilir adres, güvenilir gönderen ekle/çıkar, kanal hazır değilse uyarı); **tercihler** (saat dilimi, tarih-sayı biçimi, para birimi, hafta başlangıcı). `taxNumber` DTO'ya eklendi. |

### Bu turda düzeltilen ikincil kusurlar

- Kendi profilimde "Takipçi / Takip Edilen" sayaçları `userId = 0` ile açılıyordu (liste boş gelirdi); artık kendi kimliğimle.
- `LkFilterGrup`'a "Temizle" geldi: varsayılandan sapan bütün gruplar tek dokunuşla geri alınıyor.
- Sekme şeridi `Row` → `LazyRow`: dört sekmede sonuncusu dar telefonda ekran dışında kalıyordu.

### 6. madde — web ↔ mobil denetimi (AÇIK)

Yol düzeyinde hızlı bir tarama yapıldı (`frontend/src/router/index.jsx` ↔ `navigation/Destination.kt`). Mobilde karşılığı olmayan **ürün** yolları:

- `/app/onboarding` — kurulum akışı (`UserDto.onboardingCompleted` mobilde okunuyor ama akış yok).
- `/app/assessment` — işletme değerlendirmesi.

`flashcards` ve `quiz` özellik bayrağıyla kapalı eski ekranlar, `/admin/*` yönetim paneli, `/fiyatlar` ve ödeme dönüş sayfaları kapsam dışı (mağaza kuralı). Alan alan denetim (her ekranda hangi alanın gösterilip gösterilmediği) yapılmadı.

---

## §10 — İkinci tur (aynı gün, akşam)

| Ne | Ne yapıldı |
| --- | --- |
| **"Filtreler" paneli yine yanlış yerde** | Önce satır içiydi (liste kayıyordu), sonra ekranın altına alınmıştı — dokunulan yer yukarıda, açılan yer aşağıdaydı. Artık **düğmenin kendi altında** açılan bir katman (`Popup`): arkadaki liste yerinden oynamıyor, dışarı dokununca kapanıyor. |
| **Kursa dokununca ders açılmıyordu** | Kurs kartı önce ders listesine düşüyordu; yayımdaki kursların hepsi tek dersli olduğu için bu fazladan bir dokunuştu. Artık **doğrudan derse** açılıyor: hedef ders "kaldığın yer → ilk tamamlanmamış → ilk" sırasıyla seçiliyor (web `CoursePlayerPage` ile aynı), ara ekran `replaceTop` ile yığından çıkıyor — geri tuşu kurs listesine döner. |
| **Entegrasyonlar birbirinden ayırt edilmiyordu** | Beş sağlayıcı da yalnız durum noktası + adla duruyordu. Her pazaryerinin **kendi marka renginde** baş harf kutucuğu eklendi. ⚠️ Gerçek logolar kullanılmadı: tescilli marka dosyalarını izinsiz paketlemiyoruz. İzinli dosyalar gelirse yalnız `SaglayiciIsareti` bileşeninin içi değişir. |

### Onay bekleyen

**Genel Bakış** ve **Hesaplamalar** için kategorileşme önerisi föy olarak hazırlandı (üç kadraj: Takip 1, Hesap 1, Hesap 2). Ürün sahibi onaylayınca uygulanacak; ikisi de yeni bileşen ya da yeni renk gerektirmiyor.

---

## §11 — Onaylanan föyün uygulanması

Ürün sahibi "Genel Bakış ve Hesaplamalar kategorileşme önerisi" föyünü onayladı; üç kadrajın üçü de uygulandı.

### Genel Bakış (Takip 1)

Ekranda hero'nun altında yalnız "Yaklaşan Kayıtlar" ve bir düğme vardı; işletmenin geri kalanına ancak bölüm çekmecesinden ulaşılıyordu. Artık dört grup var:

- **BUGÜN NE DURUMDAYIM?** — Geciken / Bugün / Açık kayıt / Yön bekleyen (dördü de `tracker/summary`'den).
- **PARA** — yaklaşan üç kayıt + yönü belirsiz satırı, başlıkta "Kayıtlar ›".
- **TİCARET** — Siparişler (`orders.size` + `counts.shipments`), Ürünler (`products.total` + `stockFilter=low` toplamı).
- **OPERASYON** — Takvim, Belgeler (`documents.total`), Bildirimler (`unreadCount`).

`WorkspaceHomeViewModel.BolumSayilari` bu sayıları dört ayrı istekle çekiyor; **gelmeyen sayı hiç yazılmıyor** (sıfır yazmak "bilmiyoruz"u "hiç yok"a çevirirdi).

⚠️ **Alttaki pazaryeri siparişleri çekmecesi kaldırıldı.** Föyde yok ve ekranın altını kaplıyordu; içeriği (durum filtreleri + sipariş listesi) Siparişler ekranında zaten tam hâliyle var, TİCARET grubundan tek dokunuşla açılıyor.

### Hesaplamalar (Hesap 1 / Hesap 2)

18 araç tek düz listedeydi, üstünde yatay kaydırılan bir kategori hap şeridi vardı; şerit kategorinin kaç araç taşıdığını söylemiyordu. Yeni yapı: **arama → kategori ızgarası (sayılarıyla, her kategoriye kendi ikonu) → kategoriye girince araç listesi**. Araçlar kart değil satır; rozet kipi söylüyor (Hızlı / İleri analiz). Kategoriler `CALCULATION_CATEGORIES`'in aynısı.

⚠️ İşletme Takibi kısayolları (kayıt, belge, takvim) bu ekrandan kalktı: aynı üç giriş artık Genel Bakış'ın kendi gruplarında ve ait oldukları yer orası.

🔴 Kategoriye girince liste önceki kaydırma konumunda kalıyordu (başlık görünmüyordu) — `rememberLazyListState` + kategori değişiminde başa sarma.

### Ders okuyucu

"Bir ders içeriği gibi görünmüyor" geri bildirimi. Üç şey değişti:

1. **Başlık bloğu**: ders adı sayfa başlığı, altında künye hapları (Ders 1/1 · süre · kurs ilerlemesi) ve ders şeridi. Kurs adı ders adıyla aynıysa yazılmıyor (tek dersli kurslarda üst üste iki kez okunuyordu).
2. **Açılış sorusu kendi kartında**: içerik `## Pratik Karar: "…"` başlığıyla açılıyor ve bu dersin CEVAPLADIĞI soru. Gövdenin içinde sıradan bir başlık gibi duruyordu; artık "BU DERSİN CEVAPLADIĞI SORU" kartında ve gövdeden çıkarılıyor (iki kez okunmasın).
3. **Numaralı bölüm başlıkları**: "1. Ciro İllüzyonu…" gibi başlıklarda numara kendi rozetine giriyor, bölümün üstüne ince ayraç konuyor. Numarayı biz eklemiyoruz — içerikte zaten var, yalnız biçimi değişiyor.

---

## §12 — Menü, derinlik ve belge yükleme turu

| Geri bildirim | Ne yapıldı |
| --- | --- |
| **"Menü açma işi için yeni bir tasarım istiyorum"** | Ürün Merkezi ve İşletme Bölümleri artık ORTAK bir bileşende (`LkMenuSheet`). Tepede **AÇIK BÖLÜM** bloğu (marka gradyanı — "neredeyim" sorusunu menüyü taramadan cevaplıyor), grup başlıkları çizgiyle kapanıyor, girişler iki sütunlu **kutucuk** hâlinde. Aynı iş için iki farklı desen kalmadı. |
| **"Butonlara derinlik istiyorum, sadece ışık görünüyor"** | Basma geri bildirimi artık yalnız dalga değil: `LkButton` basılıyken **gölgesini bırakıyor** ve küçülüyor (yüzeye gömülüyor), bırakınca yükseliyor. Menü kutucukları aynı davranışta. Seçili hap ve seçili kutucuk **daha derin gölgeyle** duruyor — seçili olmak artık renkten önce derinlikten okunuyor. |
| **"Takip ve engelleme ayrı görünmesin, ayarlara alalım"** | Topluluk profilindeki kart kalktı; Ayarlar > Hesap altında **Takip ve engelleme** satırı (`Destination.CommunityPeople`). Derin bağlantı (`community?tab=people`) çalışmaya devam ediyor. |
| **"Sohbet başlatta iki butona gerek yok"** | Boş ekranda yalnız ortadaki "Sohbet Başlat" kalıyor (ne yapılacağını da o anlatıyor); yuvarlak düğme liste doluyken görünüyor. |
| **"Belge yükleme haline bak, eksikler var"** | Webdeki karta göre üç eksik vardı: belge **türü** seçimi, **e-posta ile gönderme** yolu ve **desteklenen biçimlerin** listesi. Üçü de eklendi ("Belge ekle" paneli). Tür sunucunun kendi enum'u; "Otomatik" varsayılan çünkü e-Fatura XML'ini sunucu zaten çözümlüyor. |

⚠️ **"Fotoğraf çek" hâlâ yok.** Kamerayla çekim FileProvider kurulumu ve iki platformda ayrı `expect/actual` istiyor. Dosya seçiciyi açıp "fotoğraf çek" demek kullanıcıya yalan söylemek olurdu; düğme, gerçekten çalışana kadar konmadı.

---

## §13 — Kamera, açılış ekranı, profil ve İşletme Takibi geri dönüşü

| İstek | Ne yapıldı |
| --- | --- |
| **"Fotoğraf çek gerçekten çalışsın"** | `rememberCameraCapture` eklendi (`expect/actual`). Android'de sistem kamerası bir FileProvider adresine çekiyor, dosya okunup yükleniyor ve önbellekten **hemen siliniyor**. ⚠️ `CAMERA` izni bilerek bildirilmedi: bildirilse sistem gereksiz bir izin sorusu sorардı. iOS'ta `null` dönüyor ve düğme **hiç çizilmiyor** — çalışmayan düğme koymuyoruz. |
| **"Genel Bakış'ta TİCARET/OPERASYON grupları olmasın, gerçek veri olsun; alttan açılan panel daha iyiydi"** | İki gezinti grubu kaldırıldı (ikisi de üstteki bölüm menüsünde zaten var). Yerine **SON HAREKETLER**: gecikenler önce, sonra yaklaşanlar (`overdue` + `upcoming`), yönü belirsiz kayıt satırı korunuyor. **Pazaryeri siparişleri çekmecesi geri geldi** (üç kademeli, durum filtreleriyle). |
| **"Açılış ekranı daha güzel bir animasyonla açılsın, pusula gibi"** | Uygulamanın ilk karesi artık **pusula kadranı**: K/D/G/B yön harfleri, on iki taksimat, altın uçlu iğne −142°'den kuzeye oturuyor, ortada marka işareti. Kare **en az 1,3 saniye** duruyor — oturum kontrolü çoğu zaman 200 ms sürüyor ve animasyon başlamadan kayboluyordu. |
| **"Dönen mavi çember yerine modern bir şey"** | Material'ın `CircularProgressIndicator`'ı 15 çağrı noktasından kaldırıldı; yerine sırayla nefes alan **üç nokta** (`LkLoadingSpinner`, marka renginde, hareket kısıtlıyken sabit). |
| **"İlk ekranda yandaki mavi şerit olmasın, tamamen logo"** | Uygulama simgesinin zemini gradyandı ve kenarında açık mavi bir halka izlenimi doğuruyordu; zemin **tek renk** yapıldı. Sistem açılış ekranındaki simge arkası dairesi de kaldırıldı (`windowSplashScreenIconBackgroundColor` şeffaf). |
| **"Profil föydeki yapıya dönsün, Kaydettiklerim kalsın"** | Ad ve **Profili düzenle** aynı satırda; sayaçlar tek satırda metin gibi ("0 paylaşım · 0 takipçi · 0 takip"); engelleme/şikâyet ikincil satıra indi. Sekmeler: Paylaşımlarım / Medya / Beğenilerim / **Kaydettiklerim** (silinmedi). |
| **"Takip ve engelleme ayrı görünmesin"** | Topluluk profilinden kalktı, Ayarlar > Hesap altına taşındı. |

### Açık kalan

⚠️ **Android'in KENDİ açılış karesi (sistem splash) hâlâ uygulama simgesini gösteriyor.** `windowSplashScreenAnimatedIcon` teması doğru derleniyor (birleştirilmiş `values-v31` doğrulandı) ama bu cihazda dikkate alınmıyor; sistem simgeyi çiziyor. Görülen sıra: ~1 sn marka renginde logo → uygulamanın kendi **pusula animasyonu**. Simgenin arkasındaki daire kaldırıldığı için iki kare aynı renkte ve geçiş sıçramıyor.

---

## §14 — Web↔mobil parite turu, 1. bölüm (07.09.2026)

Kapsam kararı: **tam parite** (ürün sahibi, 07.09.2026). İki istisna:
İngilizce **yapılmayacak** (mobil Türkçe kalıyor) ve topluluk **reklamları**
mobil akışa taşınmayacak.

⚠️ **iOS bu turda hiç derlenmedi.** Geliştirme makinesi Windows; Kotlin/Native
Apple hedefleri macOS istiyor. Bu turda `iosMain`e yazılan tek şey
`rememberFileSharer` (UIActivityViewController) ve o kod **derlenmemiş,
çalıştırılmamış** durumda. §6'daki iOS boşlukları aynen duruyor.

### Yapılanlar

**Belge önerisi kabul/ret** (madde 7). Sunucu öneriyi belge listesiyle
birlikte zaten gönderiyordu; mobil DTO `payload`ı `JsonElement` olarak alıp
hiç okumuyordu. Artık belge satırının altında öneri kartı var: tür · tutar ·
vade, yönü belirsizse sunucunun açıklaması, güven yüzdesi, "Kayıt oluştur" /
"Yoksay". Tutar ve vade sunucuda null olabiliyor; boşsa **yazılmıyor**.

**Kayıt detayı: DAYANAK, HATIRLATICILAR, GEÇMİŞ** (madde 9, 10, 12). Sunucu
detay ucunda üçünü de gönderiyordu, mobil DTO hiçbirini tanımıyordu. e-Fatura
alanları (fatura no, düzenleme, vade, tutar, satıcı/alıcı + VKN/TCKN) artık
görünüyor. Vade yoksa "Faturada belirtilmemiş" — düzenleme tarihi vade yerine
konmuyor.

**Dışa aktarma** (madde 5). CSV / Excel / PDF + kayıt başına PDF. Webdeki
"indir" yerine sistem **paylaşım sayfası**: telefonda doğru karşılık bu.
Ekrandaki süzgeçler dosyaya da uygulanıyor.

**Paylaşım altyapısı** (`rememberFileSharer`). Android: FileProvider +
ACTION_SEND. Paylaşılan dosya hemen silinmiyor — alıcı uygulama kendi zamanında
okuyor — bir sonraki paylaşımda bir saatten eski artıklar temizleniyor.

### Emülatörde ölçülen ve düzeltilen BEŞ arıza

Hepsi gerçek veriyle (UBL örnek faturası sunucunun kendi yükleme yolundan
geçirildi), Pixel_8 / API 35 üzerinde ölçüldü.

1. 🔴 **Gövdesiz her POST 400 alıyordu.** `defaultRequest` her isteğe
   `Content-Type: application/json` koyuyor; `SafeApiClient.post` gövde yokken
   yalnız gövdeyi atlıyor, başlığı bırakıyordu. Fastify haklı olarak
   reddediyordu. **Bu yalnız yeni çağrıların sorunu değildi:** e-Fatura gelen
   kutusunu **açma** ve bildirimleri **tümünü okundu işaretleme** üründe zaten
   bozuktu. Çözüm başlığı kaldırmak değil doğru kılmak: boş JSON nesnesi.

2. 🔴 **Ham sunucu kodu ekrandaydı.** Belge satırında `review_required`
   yazıyordu; `when` yalnız üç değeri çeviriyor, sunucunun gerçekte ürettiği
   dördü listede yoktu ve `else -> it` kodu basıyordu. İfadeler webden alındı.

3. 🔴 **Kayıt detayı tümden düşüyordu** ("İşletme yüklenemedi").
   `document.analysis` liste ucunda **nesne**, detay ucunda **JSON dizgesi**
   olarak geliyor (`recordJson` yalnız `record.metadata`yı çözüyor). Web bu
   tutarsızlığı `analiziCoz` ile karşılıyor; mobil de aynı savunmayı yapıyor
   (`eFaturayiCoz`). Sunucu sözleşmesi değiştirilmedi — web iki biçime de bağlı.

4. 🔴 **Tarih alanları boş çiziliyordu.** `LkDateUtils.parseDate` yalnız tam
   zaman damgası anlıyordu; e-Fatura tarihleri düz `YYYY-MM-DD`. Düz tarih
   **zaman dilimine çevrilmiyor**: fatura tarihi bir ana değil bir güne işaret
   ediyor, çevirmek batı dilimlerinde tarihi bir gün kaydırırdı.

5. 🔴 **Dışa aktarma indirilemiyordu.** İstemcideki `HttpResponseValidator`
   JSON/SSE dışındaki her yanıtı reddediyordu. Kapı kaldırılmadı, **daraltıldı**:
   yalnız uygulamanın gerçekten indirdiği üç biçim (pdf, csv, xlsx) geçiyor —
   JSON yerine HTML giriş sayfası dönen durumları yakalama değeri korunuyor.

### Doğrulama

Pixel_8 (API 35) + gerçek arka uç. Öneri kabul edildi → sunucuda öneri
`accepted`, gerçek kayıt oluştu (₺17,88, `neutral`). Kayıt detayında fatura
alanları, hatırlatıcı ve geçmiş göründü. PDF dışa aktarma Android paylaşım
sayfasını `kayitlar.pdf` ile açtı. `:composeApp:testDebugUnitTest` geçiyor.

⚠️ Emülatör `-no-window -gpu swiftshader_indirect` ile çalıştırıldı; pencereli
kip bu makinede `bad color buffer handle` ile çöküyor. Ekran görüntüsü bazen
siyah dönüyor, uygulamayı yeniden başlatınca düzeliyor — doğrulamalar bu yüzden
`uiautomator dump` (metin) üzerinden yapıldı.

### Bu turda YAPILMAYANLAR

Madde 3 (davet kabul), 6 (toplu içe aktarma), 8 (belgeden model önerisi),
11 (kayıt–belge bağlama), 13–14 (karar makbuzu, karar takibi), 15–18
(hesaplama), 19 (mention), 22 (genel arama), 25 (bildirimler tek ekran),
28/30/31. Madde 26 (iOS çalıştırma) ve 27 — **27 zaten vardı**: iOS derin
bağlantı `iosApp.entitlements` + `ContentView.swift` ile kurulu ve
`M10A_DEEP_LINK_PARITY.md`de doğrulanmış; önceki tespit yanlıştı.

---

## §15 — Parite turu, 2. bölüm: Faz 2 tamamlandı (07–08.09.2026)

Faz 2 (İşletme Takibi) bitti. Madde 3, 5, 6, 7, 8, 9, 10, 12 yapıldı ve
**emülatörde gerçek veriyle** doğrulandı.

### Yapılanlar

**Ekip daveti (madde 3).** Davet e-postası `/davet?token=` gönderiyor;
mobil yalnız `/app/` yollarını tanıdığı için bağlantı tarayıcıya düşüyordu.
Artık manifest `/davet` yolunu da kaydediyor (`pathPrefix` değil `path` —
önek `/davetiye-kampanyasi` gibi ileride açılacak her sayfayı da çekerdi) ve
ayrıştırıcı jetonu sorgudan okuyor.

⚠️ **Sorgu dizesi hâlâ güvensiz sayılıyor.** Ayrıştırıcı sorguyu genel olarak
bilerek atıyor; davet tek istisna ve orada da biçim katı doğrulanıyor
(`[A-Za-z0-9_-]{16,256}`). Üç yeni test bu istisnanın genişlemesini
engelliyor.

⚠️ **Bağlantıya dokunmak kabul değil.** Davet bir işletmenin kayıtlarına,
kişilerine ve belgelerine erişim demek; kullanıcı neye katıldığını görüp
onaylıyor. Web de sessizce kabul etmiyor.

**Toplu içe aktarma (madde 6).** Webin üç adımlı sihirbazı taşınmadı: telefonda
11 alanı elle eşleştirmek yapılabilir bir iş değil. Eşleştirme webdeki takma
adlarla otomatik, ama **sonucu kullanıcıya yazılıyor** ("Tutar ← Tutar") —
yanlış sütundan tutar aktardığını kayıtlar oluştuktan sonra değil, öncesinde
görsün diye.

⚠️ **Yalnız CSV.** Web de aslında yalnız CSV'de çalışıyor: xlsx dalında sütun
listesi boş bırakılmış (`rows = []`), eşleştirme hiç kurulamıyor.
Desteklenmeyen bir biçimi destekliyormuş gibi göstermek yerine mobil bunu
açıkça söylüyor.

**Belgeden finansal model önerisi (madde 8).** "Bu belgeyle hangi hesabı
yapabilirim". İstek kendiliğinden atılmıyor — her belge için otomatik sormak,
liste her açıldığında N istek demekti.

### Emülatörde ölçülen ve düzeltilen DÖRT arıza daha

6. 🔴 **`missingFields` dizge listesi sanılmıştı.** Web bu alandan yalnız
   `.length` okuyor, dolayısıyla içeriği oradan anlaşılmıyor; sunucu
   `{key,label,unit}` nesneleri döndürüyor. Model önerisi isteği tümden
   çöküyordu.

7. 🔴 **Ham İngilizce kütüphane mesajı ekrandaydı.** İçe aktarma önizlemesinde
   sunucu aynı alan için iki hata gönderiyor: kendi Türkçe mesajı ve
   doğrulama kütüphanesinin ham çıktısı ("Expected number, received string").
   Alan başına tek mesaj bırakıldı — ilki, yani insan için yazılmış olan.

8. 🔴 **Ham alan kodu ekrandaydı** (`amount`, `dueAt`). Artık çevriliyor.

9. **Türkçe ek hatası:** "Verinin %100'si hazır" (doğrusu "%100'ü"). Ek sayının
   okunuşuna göre değişiyor (yüz→ü, elli→si); her sayı için doğru eki üretmek
   ayrı bir iş. İfade eksiz kuruldu: "%100 hazır".

### Madde 11 listeden DÜŞTÜ

Kayıt–belge bağlama bir parite eksiği değilmiş: `attachDocument` web API
istemcisinde tanımlı ama **webde hiçbir yerden çağrılmıyor**. Mobil için
yapmak, webde olmayan bir özellik eklemek olurdu.

### Doğrulama

Hepsi Pixel_8 (API 35) + gerçek arka uçta, gerçek veriyle:

- Davet kabul edildi → sunucuda davet `accepted`, üyelik `staff` rolüyle oluştu.
- CSV içe aktarıldı → 3 satır okundu, 2'si yazıldı, bozuk satır satır
  numarasıyla raporlandı; veritabanında tutar/yön/tür/vade doğru.
- Belgeden 10 model önerisi geldi, birine dokununca model ekranı açıldı.
- `:composeApp:testDebugUnitTest` — **45 test, 0 başarısız.**

### Sırada

Faz 3 (karar makbuzu, karar takibi, hesaplama maddeleri 15–18), Faz 4
(onboarding, assessment, tur), Faz 5 (topluluk, kabuk). iOS maddeleri (26, 28)
Mac bulunana kadar beklemede.

### BORÇ — kayıt–belge bağlama (eski madde 11)

`POST /workspaces/:ws/records/:id/documents/:docId` sunucuda **çalışır
durumda**, web API istemcisinde `attachDocument` olarak **tanımlı**, ama
webde hiçbir yerden **çağrılmıyor**. Yani kullanıcı bugün ne webde ne
mobilde bir kaydı bir belgeye elle bağlayabiliyor; bağlantı yalnızca
belge önerisi kabul edildiğinde sunucu tarafından kuruluyor.

Bu bir parite eksiği değil, **iki üründe birden eksik bir özellik**.
Ürün sahibi kararı (08.09.2026): şimdilik yapılmayacak, en son **web ve
mobilde birlikte** ele alınacak. Mobilde tek başına yapmak, webde
olmayan bir yüzey açmak olurdu.

---

## §16 — Faz 3, 1. bölüm: karar makbuzu ve karar takibi (08.09.2026)

### Madde 13 — Karar makbuzu

Webde her karar sonucunun yanında bir "Karar Fişi" duruyor
(`DecisionReceipt.jsx`); mobilde yalnız ayrıntılı sonuç paneli vardı ve
kullanıcının kararını dışarı çıkarmasının yolu yoktu.

Makbuz sonucun **üstünde**: makbuz "ne karar verdim"i, panel "nasıl
hesaplandı"yı anlatıyor. Webde de fiş panelin yerine geçmiyor, yanında
duruyor.

⚠️ **"Yazdır" yerine "Paylaş".** Telefonda yazdırma doğal eylem değil;
paylaşım sayfası göndermeyi, kaydetmeyi ve yazıcı tanımlıysa yazdırmayı
tek yerde veriyor. Webdeki niyet korunuyor, aracı mobile çevriliyor.
Bunun için `rememberTextSharer` eklendi — makbuz bir dosya değil birkaç
satır; dosya olarak paylaşılsa WhatsApp'a `.txt` eki olarak düşerdi.

⚠️ Paylaşılan metne **reklam satırı eklenmiyor**. Kullanıcının kararı
kullanıcınındır.

**"Mentora sor" bağlamı taşıyor.** Web mentor adresine `?prompt=` ile
gidiyor; mobilde mentor ekranı dışarıdan metin almıyordu, yani düğme
yalnızca mentoru açıyor ve kullanıcı kararını baştan yazmak zorunda
kalıyordu. `MentorPromptStore` (tek atışlık) eklendi: bağlam açılan
sohbetin **yazma kutusuna düşüyor**, gönderilmiyor — yazmadığı bir soruyu
kullanıcının ağzından sormak doğru olmazdı.

### Madde 14 — Karar takibi

Karar araçlarının öğrenme döngüsü: kararı bir göreve bağla, vakti gelince
gerçek sonucu ve çıkarılan dersi yaz.

⚠️ **Sunucuda özel bir uç yok ve gerekmiyor.** Web de normal bir kayıt
açıyor (`type: "task"`) ve karar bilgisini `metadata`ya yazıyor. Mobil
**aynı şekli** kullanıyor — başka türlü yazılsaydı webde açılan takip
mobilde görünmezdi.

⚠️ İşletme seçili değilken form çizilmiyor, sebebi yazılıyor:
kaydedilemeyecek bir form göstermek yanlış olurdu.

⚠️ Sonuç yazılırken **mevcut `metadata` korunuyor**. Üstüne yazmak
`decisionSessionId`i ve `expectedOutcome`u silerdi; kayıt karara bağlı
olmaktan çıkar, "ne bekliyordum" sorusu cevapsız kalırdı.

### Emülatörde ölçülen ve düzeltilen iki arıza

10. 🔴 **Türkçe büyük harf yanlıştı.** Makbuzda "İNDİRİM ÖNCESI KATKI"
    yazıyordu — doğrusu "ÖNCESİ". Kotlin Multiplatform'da
    `String.uppercase()` yerelden bağımsız çalışıyor ve `i` → `I` yapıyor.
    Aynı hata **avatar baş harflerinde** de vardı: "İrem" adının baş harfi
    `I` çıkıyordu. `trBuyuk()` eklendi (`i`→`İ`, `ı`→`I`), dört kullanıcıya
    görünen yerde uygulandı, üç testle kilitlendi.

11. 🔴 **`decisionTitle` veritabanına boş yazılıyordu.** Başlık ViewModel
    kurucusunda tutuluyordu ama AppShell orada oturumu henüz bilmiyor.
    Artık `takipOlustur`a parametre olarak, ekrandan geliyor.

### Doğrulama

Pixel_8 + gerçek arka uç, gerçek bir tamamlanmış kararla:
makbuz hüküm/özet/ana sonuç/kalemler/dayanak/sonraki adımla çizildi,
"Paylaş" Android paylaşım sayfasını **tam makbuz metniyle** açtı, takip
görevi oluşturuldu ve veritabanında doğru `metadata` şekliyle göründü.
`:composeApp:testDebugUnitTest` — **48 test, 0 başarısız.**

### Faz 3'te kalanlar

15 (girdi kaynak künyesi), 16 (pazaryeri verisinden doldur),
17 (karar günlüğüne kaydet), 18 (belgeden model ön doldurma).

---

## §17 — Faz 3, 2. bölüm: kaynak künyesi ve pazaryeri ipucu (08.09.2026)

### Madde 15 — Girdi kaynak künyesi

Mobil `assumptions` alanını **hep boş** gönderiyordu; sunucu her girdinin
nereden geldiğini saklıyor ve web bunu girdi başına soruyor.

⚠️ **Mobilde girdi başına dört alanlık künye formu YOK, bilerek.** Telefonda
her girdi için dört ek alan doldurtmak yapılabilir bir iş değil. Künye
**yalnız değeri kullanıcı koymadıysa** görünüyor: belgeden ya da
pazaryerinden geldiyse nereden geldiği yazılıyor, belgeden geldiyse
"doğruladım" isteniyor. Kendi yazdığı sayı için kullanıcıya "bu nereden
geldi" diye sormak anlamsızdır.

Bu, madde 18'in ön koşulu: sunucu, belgeden gelen alanlar
`userVerified` olmadan modeli **çalıştırmıyor**.

### Madde 16 — Pazaryeri verisinden doldur

Son 90 günün gerçek sipariş kalemlerinden ortalama fiyat ve komisyon.
Yalnız `PRODUCT_PROFITABILITY` için — web de öyle; ortalama satış fiyatını
başka bir modelin başka bir alanına yazmak farklı bir büyüklüğü aynı sayıyla
doldurmak olurdu. Komisyon oranı gelmemişse o alan **elle bırakılıyor**,
sıfır yazılmıyor.

### 🔴 WEB HATASI BULUNDU VE DÜZELTİLDİ

`FinancialModelWorkspace.jsx` pazaryerinden doldururken
`sourceType: 'marketplace'` yazıyordu. Sunucunun enum'u altı değer kabul
ediyor ve **'marketplace' onların arasında yok** — aynı dosyadaki `<Select>`
zaten doğru altısını sunuyor.

Çalışan sunucuya istek atılarak ölçüldü (08.09.2026):
`'marketplace'` → **422 enum hatası**, `'market_data'` → geçiyor.
Yani **webde "Bu değerlerle doldur" dedikten sonra model çalıştırılamıyordu.**
Tek kelimelik düzeltme yapıldı; frontend testleri (63 dosya, 473 test) geçiyor.

### Emülatörde ölçülen ve düzeltilen iki mobil arıza

12. 🔴 **Açılan işletme AKTİF olmuyordu.** Listeden başka bir işletme
    açıldığında ekrandaki veriler o işletmenin oluyor ama aktif işletme
    eskisi kalıyordu — üst rozet "Davet Testi Atölyesi" derken veriler
    Deniz Tekstil'indi. Etkisi kozmetik değil: model çalıştırma, karar
    takibi ve pazaryeri ipucu hep aktif işletmeyi kullanıyor, yani kullanıcı
    baktığı işletmeye bakarken **başka bir işletmede** model çalıştırabilirdi.

13. 🔴 **Sunucunun hata mesajı yutuluyordu.** Model çalıştırma başarısız
    olunca sabit "Model çalıştırılamadı." yazıyordu; oysa sunucu hangi alanın
    eksik olduğunu söylüyor ("Ürün Maliyeti zorunludur..."). Kullanıcı neyi
    düzelteceğini bilmeden aynı düğmeye basıp duruyordu.

### 🔴 AÇIK KALAN: finansal model ekranı kaydırma arızası

Ekran **hiç kaydırmıyordu**: 1600 piksellik kaydırmadan sonra "Model
Girdileri" başlığı tam aynı yerde kalıyor, beş girdinin ikisine ve
"Modeli Çalıştır" düğmesine ulaşılamıyordu. Yani finansal model ekranı
mobilde fiilen kullanılamıyordu.

**Kısmen düzeltildi:** sekme satırı YATAY kaydırılıyor ve içindeki her
sekmede `fillMaxWidth()` vardı — yatay kaydırmada çocuklar sonsuz genişlik
kısıtıyla ölçülüyor, yani yedi sekme birden "sonsuzu" istiyordu. Kaldırıldı,
ekran artık kaydırıyor.

⚠️ **Ama tam çözülmedi.** İçerik belli bir yükseklikten sonra hâlâ
kesiliyor: pazaryeri kutusu ve kaynak künyeleri eklendiğinde son iki girdi
ve çalıştır düğmesi yeniden ulaşılamaz oluyor (üç ardışık kaydırmada
"Operasyon Maliyeti" y=1181'de sabit kalıyor). Kök sebep bulunamadı; ekranda
iç içe kaydırma ya da sabit yükseklik yok. **Bir sonraki turun ilk işi bu**
— maddeler 17 ve 18 bu ekranın üstüne kurulacak ve ekran çalışmadan onları
doğrulamak mümkün değil.

### Faz 3'te kalanlar

17 (karar günlüğüne kaydet) ve 18 (belgeden model ön doldurma) — ikisi de
finansal model ekranına bağlı, yukarıdaki kaydırma arızasından sonra.

---

## §18 — Faz 3 tamamlandı: kaydırma arızası çözüldü, 17 ve 18 yapıldı (08.09.2026)

### 🔴 KAYDIRMA ARIZASI — kök sebep bulundu ve çözüldü

§17'de açık bırakılan arıza kapandı.

Ölçüm: `scrollState.maxValue` yalnızca **732 piksel** dönüyordu; içerik üç bin
pikseli aşıyor. Yani `Column(verticalScroll)` içeriği ölçümde kesiyordu — son
girdiler ve "Modeli Çalıştır" düğmesi ulaşılamaz kalıyordu.

İki aşamalı düzeltme:
1. Sekme satırındaki `fillMaxWidth()` kaldırıldı (yatay kaydırmada sonsuz
   genişlik isteniyordu). Bu kaydırmayı başlattı ama kesilmeyi bitirmedi.
2. Ekran **`LazyColumn`a çevrildi** — uygulamanın zaten çalışan deseni
   (Kayıtlar, Belgeler, Kayıt Detayı hepsi `LkHeroPage` içinde `LazyColumn`
   kullanıyor). `LazyColumn` her öğeyi sonsuz yükseklik kısıtıyla ölçüyor.

⚠️ Öğeler bilerek iri tutuldu: sekme içerikleri `ColumnScope` bekleyen bütünler.
Amaç tembel çizim değil, **doğru ölçüm**.

Doğrulandı: ekran sonuna kadar kaydırıyor, beş girdinin hepsi ve çalıştır
düğmesi erişilebilir.

### Madde 17 — Karar günlüğü

Model çalıştıktan sonra "Bu sonuca göre karar kaydet": ne karar verdim, ne
bekliyorum. Form **kapalı başlıyor** — her çalıştırmadan sonra iki boş kutu
açmak, kullanıcıya ödev gibi görünürdü; karar kaydetmek isteğe bağlı.

⚠️ Yalnız **çalıştırılmış** bir modelden kaydedilebiliyor: sunucu `modelRunId`
istiyor ve doğrusu bu — dayanaksız bir karar kaydı izlenebilir olmazdı.

### Madde 18 — Belgeden model ön doldurma

Belgeler ekranındaki "hesaplama öner" modeli açıyordu ama belgenin **okunan
değerlerini taşımıyordu**; kullanıcı faturadaki rakamları elle yeniden
yazıyordu. Artık `sourceDocumentId` gezinmeyle taşınıyor, girdiler belgeden
dolduruluyor ve kaynak "belge" işaretleniyor.

⚠️ Değerler **doğrulanmış sayılmıyor**: `dogrulandi = false` başlıyor ve
sunucu da aynı kuralı tutuyor. OCR/ayrıştırma yanılabilir; okunan bir rakamı
kullanıcıya sormadan hesaba sokmak yanlış olurdu.

⚠️ Kullanıcının **yazdığı değer ezilmiyor**: yalnız boş alanlar dolduruluyor.

### Doğrulama (hepsi Pixel_8 + gerçek arka uç, gerçek veri)

- Kaydırma: beş girdi + çalıştır düğmesi erişilebilir.
- Madde 16: "Son 90 günde 8 sipariş kaleminde ortalama satış fiyatı ₺159,88",
  doldurunca "Pazaryeri: TRENDYOL · 8 sipariş kalemi" künyesi.
- Madde 15: sunucuya giden gövde ölçüldü — `netPrice:market_data` referansıyla,
  diğerleri `user` olarak **saklandı**.
- Madde 18: belgeden açılan Cari Oran modeli 1.250.000 / 640.000 ile doldu,
  künye "Belge: ozet-mali-tablo.txt". **Doğrulamadan çalıştırınca sunucu
  reddetti** ve mesaj kullanıcıya ulaştı ("OCR belge verileri modelde
  kullanılmadan önce kullanıcı tarafından doğrulanmalıdır") — bu, §17'deki
  "sunucu mesajı yutuluyordu" düzeltmesinin karşılığı. Kutular işaretlenince
  model çalıştı: **Cari Oran 1,95**.
- Madde 17: karar kaydedildi ve veritabanında `modelRunId` ile bağlı göründü.
- `:composeApp:testDebugUnitTest` — **48 test, 0 başarısız.**

### Sırada

Faz 4 (onboarding, assessment, ürün turu) ve Faz 5 (topluluk, kabuk).
iOS maddeleri (26, 28) Mac bulunana kadar beklemede.

---

## §19 — Faz 4: giriş akışları (8 Eylül 2026)

Kapsanan maddeler: **1 (kurulum anketi), 2 (öz değerlendirme), 4 (tur bayrağı)**.
Faz 4 bitti.

### Madde 1 — İşletme kurulumu (`Destination.Onboarding`)

Web `/app/onboarding` dört adım sunuyor; mobilde **üç adım**: işletme / kanallar /
hedefler. Webdeki dördüncü "özet" adımı telefonda yalnız bir kaydırma daha demek —
girilen değerler zaten formda görünüyor. **Hiçbir alan atılmadı.** Her adımda
"Şimdilik atla" açık: web de kurulumu zorunlu tutmuyor.

Bu maddede **dördü de ayrı ayrı ölçülen dört arıza** çıktı; üçü ürün arızasıydı.

**🔴 1. Sunucu, kaydedilen anket cevaplarını aynı istek içinde geri siliyordu
(webi de etkiliyordu).**

`PUT /onboarding/profile` önce `BusinessProfile`a yazıyor, hemen ardından
`syncWorkspaceToLegacyProfile` `BusinessWorkspace`i profilin üstüne **koşulsuz**
kopyalıyordu. Anket cevapları çalışma alanına hiç yazılmadığı için yazılan
değerler siliniyordu. Yanıt gövdesi *upsert'ten önceki* değerleri döndüğü için
arayüz "kaydedildi" diyor, sayfa yenilenince alanlar boş dönüyordu.

Yalnızca **zaten çalışma alanı olan** kullanıcıda görünüyordu: alanı olmayanda
sync erken dönüyor, veri sağ kalıyordu. Aynı uç noktayı web de kullanıyor
(`OnboardingPage.jsx`), yani bu bir mobil sorunu değildi.

Düzeltme sync'i kaldırmak **değil**: `BusinessProfile` kodun kendi deyimiyle
"legacy", doğruluk kaynağı çalışma alanı. Anket cevapları artık **kaynağa** da
yazılıyor (`src/services/onboarding.ts`). Boş ad çalışma alanının adını silmiyor.
`weeklyLearningMinutes` çalışma alanında yok; sync ona dokunmuyor, yalnız
profilde yaşıyor. Gerileme testi: `tests/onboarding-profile-persist.test.ts`
(4 test; düzeltme geri alınınca 3'ü düşüyor — kontrol edildi).

**🔴 2. Kurulum hiç kapanmıyordu: `complete` her seferinde 422 dönüyordu.**

`KurulumTamamlaDto` alanı `= true` varsayılanıyla tanımlıydı. İstemcinin `Json`
yapılandırmasında `encodeDefaults` kapalı (varsayılan), dolayısıyla kotlinx
değeri varsayılanına eşit olan alanı gövdeye **hiç yazmıyordu**; sunucuya boş
`{}` gidiyor ve zod "Required" diyordu. Varsayılan kaldırıldı.

⚠️ Bu, DTO'ların tamamı için geçerli bir tuzak: **sunucunun zorunlu tuttuğu bir
alana istemcide varsayılan değer verilmemeli.** Aynı kalıptaki başka çağrı
arandı, yoktu.

**🔴 3. Seçim hapları dokunmayı almıyordu.**

`LkChip`e dışarıdan `Modifier.clickable` takılmıştı. Bileşenin **seçim aşırı
yüklemesi** (`text/selected/onClick`) kullanılıyor artık: seçili durumu renk +
**gölge** ile veriyor (renk tek başına taşıyıcı olmamalı, §8.1) ve dokunma
hedefini 44dp'ye yükseltiyor (§19).

**⚠️ 4. Kullanıcıya ham İngilizce sunucu metni gösteriliyordu.**

Kayıt düşünce ekranda **"Validation failed"** yazıyordu. Bu uçta sunucunun
söylediği hiçbir şey kullanıcının yapabileceği bir şeye karşılık gelmiyor; metin
artık istemcide yazılıyor. Tamamlama düştüğünde ise verinin **durduğu**
söyleniyor ("kaydedilemedi" demek yanlış olurdu) ve sunucunun en olası gerekçesi
(ad ya da sektör boş) kullanıcının düzeltebileceği bir şey olduğu için
yazılıyor.

### Madde 2 — Öz değerlendirme (`Destination.Assessment`)

Sorular alana göre gruplu, **hepsi tek sayfada**. Webde çok adımlı; telefonda
24 soru için 24 "Devam" dokunuşu demekti. Gönderim ancak hepsi cevaplandığında
açılıyor: sunucu eksik cevabı reddediyor ve kullanıcıya makine listesi
göstermenin anlamı yok.

İki düzeltme:

- **Türkçe büyük harf**: alan başlığı "FINANSAL YÖNETIM" yazıyordu. `uppercase()`
  yerel-bağımsız; `trBuyuk()` kullanılıyor.
- **Sonuç ekranı ham kod yazıyordu**: "finance", "cyber", "ai". Sunucu sonuç
  ucunda etiket dönmüyor, yalnız kod dönüyor. Adlar **uydurulmadı**; webin kendi
  tablosundan alındı (`AssessmentPage.jsx` → `domainLabels`,
  `tr/learning.json`). Tanınmayan kod gelirse kodun kendisi yazılıyor.

Seçenek satırlarına `heightIn(min = 44.dp)` eklendi; dolgu tek başına kısa
etiketlerde bu yüksekliği garanti etmiyordu.

### Madde 4 — Ürün turu bayrağı

**Mobilde rehberli tur YOK ve yapılmayacak**: webdeki tur ekrandan ekrana
gezdiren bir kaplama; mobilde karşılığı bambaşka bir tasarım işi olurdu ve
dock'lu bir kabukta aynı şekilde çalışmazdı. Ama kurulum tamamlanınca
`POST /onboarding/tour/complete` çağrılıyor: kurulumu telefonda bitiren kullanıcı
webde bir daha turla karşılaşmasın. Sonucu **kullanıcıya söylenmiyor** — bir
kolaylığın yazılamaması akışı durdurmaz.

### Doğrulama (hepsi Pixel_8 + gerçek arka uç, gerçek veri)

- Kurulum: "Oturmuş" + "Toptan" + "Rekabet" seçilip kaydedildi; veritabanında
  `businessStage: mature`, `challenges: ["cash_flow","competition"]`,
  `salesChannels: ["marketplace","wholesale"]`. Ekran **Ana Sayfa'ya geçti.**
- Tur bayrağı: `tourCompletedAt` yazıldı (2026-09-08T11:01:27Z).
- Değerlendirme: 24 sorunun tamamı cevaplandı, gönderildi; sekiz alan da
  **"Finansal Yönetim 50/100"** biçiminde Türkçe adla göründü. "Yeniden
  değerlendir" formu **0 / 24**'e döndürdü.
- `:composeApp:testDebugUnitTest` — yeşil.
- Arka uç: `npx vitest run` — **153 dosya / 2216 test geçti** (yeni dosya dahil).

### ⚠️ Ortam notu (ürün arızası değil)

Arka uç testlerinin tam takımı yerel veritabanının şemasını geride bırakıyor
(`UserPreference.analyticsConsent` sütunu düşüyor) ve sonrasında `npm run dev`
500 vermeye başlıyor. Çözüm: `npx prisma db push`. Bu tur bir kez yaşandı ve
teşhisi yanlış yöne çekti; not düşülüyor.

### Sırada

Faz 5: madde 19 (bahsetme seçici), 20'nin katkı listesi yarısı, 22 (genel arama),
25 (bildirimlerin tek ekranda toplanması), 30 (entegrasyonlar derin bağlantısı +
sekme vurgusu), 31 (ölü kod).
iOS maddeleri (26, 28) Mac bulunana kadar beklemede.
Borç: madde 11 (kayıt–belge bağlama) — web ve uygulamada birlikte, en son.

---

## §20 — Faz 5: topluluk ve kabuk (8 Eylül 2026)

Kapsanan maddeler: **19 (etiket seçici), 20 (katkı sağlayanlar), 22 (genel arama),
25 (bildirimlerin tek ekranda toplanması), 30 (entegrasyon derin bağlantısı +
sekme vurgusu), 31 (ölü kod)**. Faz 5 bitti.

### Madde 31 — Ölü kod

`ui/shell/MenuBottomSheet.kt` hiçbir yerden çağrılmıyordu; silindi.

### Madde 30 — Entegrasyonlar

**Sekme vurgusu**: `WorkspaceIntegrations` "İşletme Takibi" sekmesinin listesinde
yoktu; o ekrandayken alt çubukta hiçbir sekme seçili görünmüyordu. Eklendi.

**Derin bağlantı**: webin adresi `/app/settings?bolum=integrations` ve web **dört
yerden** bu adrese gönderiyor (`Dashboard.jsx`, `Workspaces/index.jsx`,
`WorkspaceLayout.jsx`). Mobil bağı tanımadığı için kullanıcı düz Ayarlar'a
düşüyordu.

⚠️ Ayrıştırıcı sorgu dizesini **bilerek** atıyor (sorgudan gelen değerlerin
yönlendirmeye karışması bir saldırı yüzeyi). O karar korundu: `bolum` yalnız
ayarlar yolunda, yalnız **tek bir sabitle** karşılaştırılarak okunuyor. Diğer
değerler (`uyelik`, `profile`, `security`...) Ayarlar köküne düşüyor — bugünkü
davranış, gerileme değil. Çalışma alanı kimliği adreste yok (webde de yok); etkin
alan kullanılıyor, yoksa Ayarlar açılıyor — uydurma bir alan seçilmiyor.

**⚠️ Yan bulgu — seçili sekme yalnız renkle belliydi.** Erişilebilirlik ağacında
seçili sekme de seçili olmayan da `selected="false"` geliyordu; ekran okuyucu
kullanıcısının hangi sekmede olduğunu anlamasının hiçbir yolu yoktu. `clickable`
yerine `selectable` (+ `Role.Tab`) kullanılıyor artık. İkonun `contentDescription`'ı
kaldırıldı: etiketi alttaki metin taşıyor, ikisi birden adı iki kez okutuyordu.

### Madde 20 — Katkı sağlayanlar

Webin sağ rayındaki `contributors` kartının karşılığı. Telefonda ray yok; Trend
sekmesine, gündemin altına kondu — webde de ikisi yan yana duran iki karttır.

⚠️ **Sunucu ucu yok, webde de yok**: liste yüklenmiş akıştan türetiliyor (yazara
göre say, çoktan aza sırala, ilk dört). Yani "topluluğun en çok katkı vereni"
değil, **görülen akışta** en çok paylaşanı. Uydurma bir itibar puanı
hesaplanmıyor. Webden tek fark satırın dokunulabilir olması — profil ekranı zaten
var ve akış kartındaki yazar adı da aynı yere gidiyor.

### Madde 19 — Etiket seçici

Paylaşım yazarken açılan kişi listesi. Liste webdekiyle **aynı kaynaktan**: akışta
en çok paylaşan dört kişi (`katkicilariCikar`, tek yerde). Sunucuda kişi arama ucu
yok; "tüm kullanıcılar" diye bir liste uydurulmuyor. Kimse yoksa düğme hiç
çizilmiyor — web de öyle.

Metin ekleme kuralı webden birebir: `@Ad_Soyad` + boşluk (`CommunityPage.jsx:589`).

**⚠️ Seçici tek başına yarım iş olurdu**: gönderi gövdesinde etiket `@Ad_Soyad`
olarak duruyor ve mobil bunu alt çizgileriyle basıyordu; web aynı metni vurgulu ve
alt çizgileri **boşluğa çevirerek** gösteriyor. Artık mobil de öyle
(`bahsetmeliMetin`). Yalnızca görünüm değişiyor; sunucuya giden metne
dokunulmuyor. E-posta adresindeki `@` etiket sayılmıyor — etiket ancak satır
başında ya da boşluktan sonra başlıyor. Sekiz yeni test.

### Madde 25 — Bildirimler tek ekranda

Mobilde **iki ayrı ekran** vardı ve ikisinin de başlığı "Bildirimler"di: biri
Topluluk sekmesindeki zilden, diğeri Ayarlar listesinden. Artık iki giriş de aynı
ekranı açıyor; web gibi **iki bölüm**: önce HESAP (üyelik, ödeme), sonra TOPLULUK.
Webin gerekçesi kendi kodunda yazıyor: "üyeliğin doluyor" uyarısını beğeni
bildirimlerinin arasında kaybetmemek.

İki kaynak bağımsız yükleniyor; hata ekranı ancak ikisi birden düşerse çıkıyor
(web `Promise.allSettled`). "Tümünü oku" ikisini birden işaretliyor.

Bu birleştirme üç eksiği daha ortaya çıkardı ve üçü de giderildi:

- **Hesap bildiriminin tarihi ham geliyordu**: satırda sunucunun ISO dizesi
  yazıyordu. Aynı ekranda topluluk bildirimleri biçimlenmiş tarih gösterirken.
- **`linkTo` hiç kullanılmıyordu**: hesap satırları dokunulamazdı, web tıklayınca
  oraya gidiyor. Sunucu göreceli yol gönderiyor (`/app/settings#uyelik`); kendi
  ayrıştırıcımıza veriliyor, çözülemeyen bağda satır dokunulamaz kalıyor —
  kullanıcıyı boş bir ekrana götürmek, dokunulamaz olmasından kötüdür.
- **Okunmamış işareti iki bölümde iki farklı dil konuşuyordu** (zemin tonu vs.
  dolu nokta). İkisi de dolu nokta.

**🔴 Yan bulgu — gövdesiz POST'ların hepsi 400 dönüyordu.**

Sunucu günlüğünde ölçüldü: `POST /community/social/notifications/read` →
`FST_ERR_CTP_EMPTY_JSON_BODY`. `HttpClient.kt`teki `defaultRequest` her isteğe
`Content-Type: application/json` ekliyor; gövde vermeyen çağrı sunucuya "JSON
gönderiyorum" deyip boş gövde gönderiyor.

Aynı arıza `SafeApiClient.post` için daha önce giderilmişti ama **ham `HttpClient`
ile yapılan çağrılarda duruyordu.** Sessizce çalışmayan işlemler: **beğenme,
kaydetme, takip etme, engelleme, sohbet davetini yanıtlama** ve bildirimlerin
tümünü okundu işaretleme. Altısı da düzeltildi (`network/BosJsonGovde.kt`).
Beğeni emülatörde doğrulandı: sayaç 0 → 1.

### Madde 22 — Genel arama

Ana Sayfa hero'sundaki arama simgesinden açılan tam ekran arama. Gruplar ve sıra
webdeki gibi: kişiler → paylaşımlar → kurslar → karar araçları → hesaplamalar →
haberler. Webin sırasındaki gerekçe kodunda yazıyor: arama kutusuna bir **ad**
yazan kullanıcı önce kişiyi görmeli.

⚠️ **En az iki harf ve 250 ms bekleme** — webin kurallarının aynısı (uç dakikada
60 istekle sınırlı). Kural kullanıcıya **söyleniyor**: tek harf yazıp "arama
bozuk" sanmasın.

⚠️ **Kutu değil simge**: hero'da bir metin alanı hem selamlamayı aşağı iter hem de
klavyeyi Ana Sayfa'ya bağlar.

⚠️ **Hesaplamalar grubu sunucunun formül listesinden süzülüyor.** Web bunu yerel
bir katalog dosyasından süzüyor (`data/calculationCatalog`); aynı listeyi mobilde
ikinci kez elle yazmak iki kopyanın sessizce ayrılması demekti.

⚠️ Haber sonucu **haber listesine** götürüyor, tek habere değil — web de öyle.

⚠️ Sunucu `knowledge` alanını da dönüyor ama çizilmiyor: Bilgi Kütüphanesi üründen
kaldırıldı, web de bu grubu çizmiyor.

Bir DTO hatası ölçülerek bulundu: `decisionChecks[].id` **uuid**, `Int` değil —
arama "Arama şu anda yapılamıyor" diyordu.

### Doğrulama (hepsi Pixel_8 + gerçek arka uç, gerçek veri)

- Madde 30: `/app/settings?bolum=integrations` derin bağlantısı **Entegrasyonlar**
  ekranını açtı; erişilebilirlik ağacında "İşletme Takibi" sekmesi artık
  `selected="true"`.
- Madde 20: Trend sekmesinde "Katkı sağlayanlar" — Admin User 5 paylaşım, Deniz
  Kaya 2 paylaşım.
- Madde 19: seçiciden "Deniz Kaya" seçilince kutuya `@Deniz_Kaya ` yazıldı
  (12/500); paylaşılan gönderi akışta **"@Deniz Kaya etiket gorunumu testi"**
  olarak göründü. *(Test gönderisi ve tohumlanan bildirimler sonradan silindi.)*
- Madde 25: HESAP (2) + TOPLULUK (2), "4 okunmamış"; "Tümünü oku" sonrası
  veritabanında **ikisi de 0**. Hesap satırına dokununca Entegrasyonlar açıldı.
- Madde 22: "deniz" → KİŞİLER (2, biri profile açıldı); "stok" → PAYLAŞIMLAR,
  KURSLAR (5), KARAR ARAÇLARI (2), HESAPLAMALAR; "kdv" → KURSLAR + "KDV Ekleme".
- `:composeApp:testDebugUnitTest` — **59 test, 0 başarısız** (48 → 59).

### ⚠️ Ölçüm notu

Emülatörde `input text` sonrası ekran klavyesi listenin alt yarısını kapatıyor ve
kaydırma hareketini yutuyor; `keyevent 111` (Esc) ile kapatmadan yapılan
"görünmüyor" tespitleri yanıltıcı. Bu turda bir kez yanlış teşhise yol açtı.

### Sırada

Faz 5 bitti. Kalanlar:
- iOS maddeleri 26 ve 28 — **Mac bulunana kadar beklemede**.
- Madde 29 (iOS ekran görüntüsü engeli) — iOS'ta karşılığı yok, kapanmaz.
- Borç: madde 11 (kayıt–belge bağlama) — web ve uygulamada **birlikte**, en son.
- PayTR canlı geçiş: mağaza bilgileri girildikten sonra `BILLING_STARTS_AT`.

---

## §21 — Borç: kayıt–belge bağlama (madde 11, 8 Eylül 2026)

Bu madde faz listesinden **borç** olarak ayrılmıştı: ekleme ucu sunucuda vardı ama
web de mobil de kullanıcıya sunmuyordu, dolayısıyla parite eksiği değil ortak bir
boşluktu. Ürün sahibinin kararı gereği **web ve uygulamada birlikte** yapıldı.

**Ne eksikti**: elle yüklenmiş bir sözleşmeyi ya da dekontu mevcut bir kayda
iliştirmenin hiçbir yolu yoktu. Bağ yalnızca belge analizinden **üretilen**
kayıtlarda kuruluyordu (`document-suggestions/:id/accept`). Sunucudaki
`POST /workspaces/:ws/records/:id/documents/:docId` ucu ve webin
`api.workspace.tracker.attachDocument` istemcisi yazılmış, **hiçbir yerden
çağrılmamıştı**.

### 🔴 Sunucuda ekleme vardı, kaldırma yoktu

Yanlış belgeyi bağlayan kullanıcının bunu geri almasının hiçbir yolu yoktu; tek
çare belgeyi tümden arşivlemekti — yani doğru bağlandığı **diğer** kayıtlardan da
koparmak. `DELETE /workspaces/:ws/records/:id/documents/:docId` eklendi.

⚠️ Yalnız **bağ** siliniyor, belge değil. Belge çalışma alanında kalıyor ve başka
kayıtlara bağlı kalabiliyor.

⚠️ Belgenin `workspaceId` alanı **geri alınmıyor**: ekleme sırasında sahipsiz bir
belge çalışma alanına yazılıyor; bağı kopardık diye onu tekrar sahipsiz bırakmak,
belgenin Belgeler listesinden kaybolması demek olurdu.

Dört yeni sunucu testi (`business-tracker.test.ts`): başka çalışma alanından
koparılamıyor (404), salt okuyan üye koparamıyor (403), koparma sonrası belge
duruyor ve çalışma alanından düşmüyor, olmayan bağ 404.

### Web (`KayitDetay.jsx`)

- "Dayanak belge" bölümü **belge yokken de** çiziliyor. Önceden yalnız bağlı belge
  varken görünüyordu; bağlama eklenince bölümü gizlemek işlevin bulunamaması
  demek olurdu.
- Belge listesi **ancak seçici açılınca** çekiliyor; her detay açılışında çalışma
  alanının tüm belgelerini indirmek için sebep yok.
- Zaten bağlı belgeler seçeneklerde **yok**. Sunucu aynı bağı ikinci kez yazmıyor
  (upsert) ama hiçbir şeyi değiştirmeyecek bir seçenek göstermek yanıltıcı olurdu.
- Kopar düğmesinin etiketi ve `title`'ı bağın koparıldığını, **belgenin
  silinmediğini** söylüyor.
- TR ve EN çeviri anahtarları eklendi (web iki dilli).
- Dört yeni bileşen testi (`KayitDetay.test.jsx`).

### Mobil (`RecordDetailScreen`, `RecordDetailViewModel`)

Aynı davranış, Türkçe. Bağ kurulduktan/koparıldıktan sonra kayıt **taze
çekiliyor**: detay ucu bağlı belgeyi ve içinden okunan e-Fatura alanlarını
birlikte döndürüyor.

⚠️ `secilebilirBelgeler` alanında `null` = "henüz istenmedi", boş liste =
"istendi ve boş". İkisini aynı değerle göstermek "belge yok" ile "daha
yüklenmedi"yi aynı ekrana düşürürdü.

### 🔴 Yan bulgu — silme istekleri de boş gövdeyle düşüyordu

Sunucu günlüğünde ölçüldü: bağ koparma `400 FST_ERR_CTP_EMPTY_JSON_BODY`
dönüyordu. Sebep §20'deki POST arızasının **aynısı**: `defaultRequest` her isteğe
`Content-Type: application/json` ekliyor, gövdesiz DELETE ise sunucuya "JSON
gönderiyorum" deyip boş gövde gönderiyor.

Bu **yalnız yeni ucun sorunu değildi**. `SafeApiClient.delete`ten geçen her silme
çağrısı ve `CommunityRepository`deki altı ham DELETE aynı yoldan geçiyordu.
Sessizce çalışmayanlar: **beğeniyi geri alma, kayıttan çıkarma, takibi bırakma,
engeli kaldırma, gönderi silme, medya silme**. Yani §20'de düzelttiğim eylemlerin
**geri alma yarısı** hâlâ bozukmuş. Hepsi düzeltildi (`bosJsonGovde`).

*(`MentorRepository` ve `SettingsRepository`deki DELETE'ler zaten gövde
gönderiyordu — biri bu tuzağa daha önce düşmüş ve orada çözmüş.)*

### Doğrulama

- **Mobil, Pixel_8 + gerçek arka uç**: "Toptanci tahsilati" kaydı → "Bu kayda
  bağlı belge yok." + "Belge bağla" → seçiciden `ornek-fatura.xml` → e-Fatura
  alanları (GIB20090000000001, ₺17,88, VKN 1288331521) **anında göründü**.
  Seçici tekrar açılınca bağlı belge seçeneklerde **yok**, diğer üçü var.
  Koparınca bölüm boş hâline döndü; veritabanında **bağ 0, belge duruyor**
  (`archivedAt` null, `workspaceId` yerinde).
- **Beğeniyi geri alma** (DELETE düzeltmesi): sayaç 0 → 1 → **0**.
- Sunucu: `business-tracker.test.ts` **28 test** (27 → 28... dört yeni test bir
  `it` bloğunda).
- Web: `KayitDetay.test.jsx` **11 test** (7 → 11), tam takım **64 dosya / 478
  test**.
- Mobil: `:composeApp:testDebugUnitTest` **59 test, 0 başarısız**.
- Doğrulama sırasında kurulan bağ sonradan koparıldı; veri bulunduğu hâlde
  bırakıldı.

### ⚠️ Kapsam notu

Koparma ucu **plana ek**: madde 11 "bağlama" diyordu, ama geri alınamayan bir bağ
kullanıcıya kötü bir takas sunardı. Ekleme ile kaldırma birlikte çıkıyor.

## §22 — Karar–görev döngüsü (9 Eylül 2026)

Ürün sahibi beş madde sordu: görev atanınca bildirim, yaklaşan/geciken
hatırlatma, ana sayfada "kararlardan gelen görevler", hedeflenen–gerçekleşen
raporu, yönetici analizi. Beşi de webde eksikti; önce web tamamlandı, sonra
mobil.

### Mobilde kod GEREKTİRMEYEN iki madde

**Atama bildirimi ve gecikme hatırlatması sunucu tarafı.** Bildirim ekranı
(`NotificationsScreen.kt`) sunucunun verdiği başlık ve gövdeyi olduğu gibi
çiziyor, `type` alanına göre dallanmıyor. Sunucu `record_assigned` ve
`record_overdue` üretmeye başladığı an mobil ikisini de gösteriyor.

Bu bilerek böyle bırakıldı: tipe göre dallanan bir ekran, sunucu yeni bir
bildirim tipi eklediğinde sessizce "bilinmeyen tip" satırı çizerdi.

### Mobilde yapılanlar

| Ne | Nerede |
|---|---|
| Karar günlüğünü geri okuma | `CalculationsRepository.kararGunlugu()` |
| Karar görevi süzgeci | `WorkspaceRepository.getRecords(kararKaynakli = true)` |
| Yönetici analizi | `WorkspaceRepository.getTrackerAnalysis()` |
| Birleştirme mantığı (saf) | `decision/KararRaporu.kt` |
| Ekran | `ui/screens/workspaces/KararRaporuScreen.kt` |
| Ana sayfa bölümü | `WorkspaceHomeScreen.kt` + `WorkspaceHomeViewModel.kt` |

### Web ile bilerek FARKLI olan üç şey

1. **Hedeflenen/gerçekleşen yan yana DEĞİL, alt alta.** Web iki sütun
   kullanıyor çünkü orada 700px var; 360dp'de iki sütun okunmuyor.
   Karşılaştırmayı kuran şey sütun değil, iki metnin aynı kartta ve aynı
   etiket dilinde durması.
2. **Yönetici analizi tablo DEĞİL, satırlar.** Beş sütunlu bir tablo dar
   ekranda ya yana kayar ya okunmaz. Her kişi kendi satırında, sayılar
   etiketli.
3. **Ana sayfada "Rapor ›" bağlantısı var, web'de bölüm başlığında.** Aynı
   iş, mobil kabuğun kendi deseni.

### 🔴 Uydurulmayan iki sayı

- **Sapma yüzdesi yok.** Karar aracı tarafında sapma alanı hiç yok; finansal
  model tarafında kullanıcı yazdıysa gösteriliyor, sunucu hesaplamıyor.
- **Karar başarısı oranı yok.** Hedeflenen ve gerçekleşen serbest metin;
  farkını programla ölçmenin yolu yok. Sonucu yazılmış her kararı "başarılı"
  saymak yöneticiye uydurma bir sayı vermek olurdu. Ekranda bunu söyleyen bir
  not var — çünkü "%73 başarı" bekleyen biri sayının olmamasını eksiklik
  sanabilir.

### Doğrulama

`:composeApp:testDebugUnitTest` yeşil; `KararRaporuTest` 9 test (birleştirme,
sıralama, özetin süzgeçten bağımsızlığı, ana sayfa kuralları, takip verisi
olmayan kayıtta çökmeme), `DestinationCodecTest` yeni hedefin gidiş-dönüşünü
kapsıyor.

**AÇIK:** Emülatörde gerçek sunucuyla gezinti yapılmadı — ekran derleniyor ve
saf mantık testli, ama gerçek veriyle görülmedi.

## §23 — CI aylardır hiç koşmuyordu (9 Eylül 2026)

§22 push edildikten sonra ürün sahibi Actions sekmesinde hiçbir koşu göremedi.
Sebep, bulunması işin kendisinden uzun süren bir zincirdi.

### Dört ayrı arıza, hepsi "sessizce yok"

| # | Arıza | Belirti |
|---|---|---|
| 1 | Her iki iş akışı da yalnız `main`/`feature/**`/`fix/**` dallarında koşuyordu; çalışma dalı `design` | Koşu **hiç** yok. Kırmızı değil, yok. |
| 2 | `gradlew` git indeksinde `100644` — çalıştırma biti yok | Android işi ilk gradle adımında `exit 126`, tek test bile koşmadan |
| 3 | Test adımı ortadaydı; link hatası kendinden sonraki her şeyi bloke ediyordu | Framework linki, Xcode derlemesi, simülatörde açılış **hiç** koşmadı |
| 4 | Xcode seçici kalıbı `Xcode_1[6-9]*.app` — Apple numaralandırmayı 26'ya atladı | Sessizce Xcode 16.4'e düşüyor, iOS 18.5 SDK ile link, `UIViewLayoutRegion` bulunamıyor |

4'ü ben yazdım. Aynı adıma "sabit sürüm yazma, imaj değişince sessizce bozulur"
diye yorum düşüp, sürüm **aralığı** kullanarak aynı tuzağın başka biçimine
düştüm. Kalıp kaldırıldı: kurulu tüm Xcode'lar sıralanıp en yenisi seçiliyor.

### Denenip işe YARAMAYANLAR — tekrar denenmesin

İkisi de kodda notlu duruyor, silinmedi:

- **Kotlin/Native önbelleğini kapatma** (`kotlin.native.cacheKind`, hem hedefe
  özel hem küresel biçim). Bağlayıcı satırında `-cache.a` arşivleri kalmaya
  devam etti, hata birebir aynı çıktı. Tek etkisi koşuyu 12→19 dakika yapmak.
- **Test ikilisinin `osVersionMin`ini 18.0'a çekme.** Uygulandığı doğrulandı
  (log'da `being linked (18.0)`, öncesinde 15.0) ama hatayı çözmedi. Yani
  dağıtım hedefi sebep değildi; sınıf kullanılan SDK'da hiç yoktu.

⚠️ Uygulamanın dağıtım hedefi **14.1'de bırakıldı**. Copilot 18.0'a çekmeyi
önerdi; bu hatayı susturur ama uygulamayı yalnız iOS 18+ cihazlarda çalışır
hale getirir — bir derleme ayarı değil, hangi müşteriye satıldığı kararıdır.

### Şimdi doğrulanan

Her push'ta koşuyor ve yeşil:

- **Android:** `assembleDebug`, `testDebugUnitTest`, `lintDebug`, `assembleRelease`.
  Birim testleri ilk kez CI'da koşuyor. `assembleRelease` R8 açık — R8'in
  kotlinx.serialization DTO'larını bozması yalnız orada yakalanır.
- **iOS:** Kotlin derlemesi, framework linki, Xcode derlemesi, simülatörde
  kurulum ve açılış, birim testleri.

§9'daki "iOS hiç çalıştırılmadı" maddesi **simülatör tarafında kapandı**.
Gerçek cihazda hâlâ çalıştırılmadı.

### Hâlâ açık

- Emülatörde/simülatörde **gerçek sunucuyla** elle gezinti yapılmadı; §22'deki
  ekranlar derleniyor ve saf mantığı testli, ama gerçek veriyle görülmedi.
- Coil 3.1.0 skiko 0.8.18 istiyor, Compose 1.11.1 ise 0.144.6 — şu an yukarı
  çözülüyor ve zarar vermiyor. Tuhaf bir çalışma-anı davranışı çıkarsa bakılacak
  ilk yer burası.

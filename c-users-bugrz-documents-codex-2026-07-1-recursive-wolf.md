# LocalKarar Mobil — Web Tasarımını App'e Sadık Şekilde Taşıma

## Context

Ürün sahibi `Desktop/localkarar mobil app/LocalKarar-Mobile` altında Compose
Multiplatform bir istemci geliştiriyor (Antigravity ile). Şikâyet: **"web
tasarımının aynı şekilde app'e uygulanmasını istiyorum ama istediğim gibi
olmuyor."**

Bu plan **teşhis + yol haritası**dır; kodu Antigravity yazacak. Bu yüzden
düzeltmeler dosya/satır düzeyinde ve kendi başına anlaşılır yazıldı.

- **Rol:** yalnız teşhis ve plan · **Hedef:** önce Android
- **Öncelik ekranlar:** Ana Sayfa · Karar Araçları · Haberler
- **Açık tasarım sorusu:** Topluluk ana modül olacak ama nasıl görüneceğine
  dair plan yok — §4'te somut bir yapı öneriliyor.

### Önce iyi haber: temel sağlam, bazı korkular güncelliğini yitirmiş

Kod tabanını haritaladım ve **daha önce "bozuk" görünen iki şeyin artık
düzelmiş olduğunu doğruladım** (eski logcat ve eski derleme çıktısı yanıltıcı):

| Daha önce | Şimdi (doğrulandı) |
|---|---|
| Karar araçları özelliği hiç derlenmemiş, 4 derleme hatası | ✅ Derleniyor — `build/tmp/kotlin-classes/.../decision/` altında **40 sınıf**. `ApiConfig.BASE_URL`, `httpClient`, `fromResponse`, `LkButton(loading=)` hatalarının **hiçbiri kalmamış** |
| Ana ekran çalışma zamanında JSON hatasıyla patlıyor | ✅ `UpcomingTaskDto.id`/`taskId` artık `String` — backend'de `TaskAssignment.id` gerçekten `uuid`, eşleşiyor |

Ayrıca **renk sistemi web'in koyu temasının birebir kopyası** — bu iş doğru
yapılmış, dokunulmamalı:

`LkPrimary #94CEED` · `LkSurfaceCanvas #121619` · `LkSurfaceSunken #0C1013` ·
`LkSurfacePanel #1D2429` · `LkSurfaceSignature #173F4E` ·
`LkOnSignature #F4FAFC` · `LkTextPrimary #F1F4F5` · `LkSuccess #72D3AD` ·
`LkDanger #FFB4AB` — hepsi `frontend/src/styles/theme-modes.css` ile aynı.

Yani sorun "yanlış renk" değil. Aşağıdaki beş sebep.

---

## 1. Tasarımın oturmamasının GERÇEK sebepleri

### 1a. 🔴 Tipografi ve şekiller tasarım sisteminden HİÇ ulaşmıyor

`composeApp/src/commonMain/kotlin/com/localkarar/app/App.kt`:

```kotlin
41:    LocalKararTheme {          // LkTypography + LkShapes + renkler burada
44:        MaterialTheme {        // ← çıplak sarmalayıcı: type ve shape'i SIFIRLIYOR
```

İçteki argümansız `MaterialTheme { }` tipografiyi ve şekilleri Material
varsayılanlarına döndürüyor. Renkler `darkColors` üzerinden geçtiği için
ayakta kalıyor — **bu yüzden renkler doğru ama yazı ölçeği ve köşe yarıçapları
webden farklı.** "Renkler tutuyor ama bir şey tutmuyor" hissinin birinci
sebebi bu.

**Düzeltme:** `App.kt:44`'teki iç `MaterialTheme { }` sarmalayıcısını kaldır.

### 1b. 🔴 Uygulama yalnız KOYU tema, web'de açık + koyu var

`ui/theme/Theme.kt` yalnız `darkColors` döndürüyor; `lightColors` ve
`isSystemInDarkTheme()` hiç yok. Web'de iki tema var ve açık tema varsayılan
(`--primary: #0D556F`, canvas `#E1E2E5`).

**Eğer app'i web'in açık haliyle yan yana koyup karşılaştırıyorsan hiçbir zaman
tutmayacak.** Önce hangi temayı hedeflediğine karar verilmeli (§5, karar 1).

### 1c. Yazı tipi ağırlıkları sahte

`ui/theme/Type.kt` beş ağırlığın (W400–W800) **tamamını aynı** `manrope.ttf`
dosyasına bağlıyor. Compose eksik ağırlıkları sentezliyor; web'de gerçek
Manrope kesimleri yükleniyor. Başlıklar webdekinden farklı kalınlıkta görünür.

**Düzeltme:** `@fontsource/manrope` paketindeki gerçek ağırlık dosyalarını
(400/500/600/700) `composeResources/font/` altına ekle ve `Type.kt`'de ayrı
ayrı bağla. Web tarafında bu dosyalar zaten var
(`frontend/node_modules/@fontsource/manrope/files/`).

### 1d. Material 2 ile web'in düz dili çakışıyor

Proje `compose.material` (M2) kullanıyor. Web'in dili düz yüzeyler, 1px
`--line` çizgileri ve `--radius-sm: 8px` / `--radius-md: 12px`. M2'nin
varsayılan gölge ve yükseklik (elevation) davranışı bunu bozuyor.

**Düzeltme:** M3'e geçmek büyük iş; bunun yerine `LkShapes` uygulandıktan
sonra (1a) bileşenlerde `elevation = 0.dp` verilip kenarlık `LkLineSoft` ile
çizilmeli. Web'deki kart görünümü bu.

### 1e. Alt navigasyon eksik ve metinler bozuk

- `ui/shell/AppShell.kt:210` → sekme etiketi **`"Menüor"`** (bozuk bir
  `Menu`→`Menü` değiştirmesi `Mentor` kelimesini de vurmuş)
- `MenuBottomSheet.kt:44` → **`"ÖÖğrenme İlerlemesi"`**
- Tanımlayıcılarda da aynı bozulma: `openMenü`, `closeMenüAndNavigate`,
  `onMenüClick`
- **Alt navigasyon ikonları boş `Box`** — hiç ikon yok, web'de var
- Uygulama ikonu **stok Android ikonu**
  (`@android:drawable/sym_def_app_icon`)

---

## 2. Tasarımdan bağımsız, ama yayına engel

| Sorun | Yer | Neden önemli |
|---|---|---|
| **Versiyon kontrolü YOK** | proje kökü git deposu değil | Antigravity kapsamlı değişiklikler yapıyor ve **geri alma yok**. Tek kötü koşu her şeyi götürür. En acil madde bu. |
| Release build emülatöre bakıyor | `network/ApiConfig.kt:9` `var environment = Development` | Derleme varyantına bağlı değil; release APK `http://10.0.2.2:3000` adresine gider. |
| Ekran döndürmede state kaybı | `ui/shell/AppShell.kt:132,158` `remember { XViewModel(...) }` | `viewModel()` yerine `remember` kullanılmış; `viewModelScope` da hiç iptal edilmiyor. |
| Çevrimdışı açılışta oturum siliniyor | `auth/AuthRepository.kt:41-46` | `restoreSession` **her** istisnada logout ediyor — geçici ağ hatası token'ı siliyor. |
| 13.9 MB `logcat.txt` proje kökünde | kök | git kurulunca `.gitignore`'a girmeli. |
| Test yok | hiç test kaynak seti yok | — |

---

## 3. Boş duran ekranlar

`ui/screens/PlaceholderScreens.kt` — hepsi `LkEmptyState`, sıfır mantık:
`AiMentorScreen`, `CalculationsScreen`, `NewsScreen`, `UpdatesScreen`,
`SavedScreen`, `ProgressScreen`, `ProfileScreen`.

Öncelik sıran (Ana Sayfa · Karar Araçları · Haberler) açısından:

- **Ana Sayfa** ✅ zaten gerçek ve çalışıyor
- **Karar Araçları** ✅ zaten gerçek ve artık derleniyor
- **Haberler** ❌ boş — ama backend `GET /api/news` hazır ve **düz REST**,
  en hızlı kazanç bu.

---

## 4. Topluluk modülü — mobil yapı önerisi

Web'de `CommunityPage.jsx` (394 satır) tek bileşen, iki modlu (`community` /
`news`). Masaüstü düzeni **iki kolon**: ana akış + sağ ray.

**Webdeki parçalar:**

| Parça | Community modu | News modu |
|---|---|---|
| Üst başlık | "YEREL İŞLETMELER / Topluluk" | "LocalKarar Haber Merkezi / Haberler" |
| Composer | "Deneyimini paylaş" — başlık + metin + medya, moderasyon uyarısı | — |
| Öne çıkan | "Öne çıkan tartışma" | "Günün gelişmesi" |
| Akış | `CommunityCard` | `NewsCard` (kategori · kaynak · dış bağlantı · raporla) |
| Sağ ray | "Gündemde" + "Katkı sağlayanlar" | "Öne çıkanlar" + "Kaynağı belli" |
| Admin | moderasyon kuyruğu | resmî içerik oluşturma |

**Mobil için önerilen çeviri** — sağ ray mobilde yok, asıl tasarım kararı bu:

1. **Topluluk ve Haberler ayrı iki alt-nav hedefi olsun.** Webdeki `mode`
   bayrağı masaüstü kolaylığı; mobilde iki ayrı sekme daha doğru.
2. **Sağ ray → akışın üstünde yatay şerit.** "Gündemde" başlıkları yatay
   kaydırılan çipler; "Katkı sağlayanlar" avatar şeridi. Ray'i alta atma —
   kimse oraya kaydırmaz.
3. **Composer → FAB + tam ekran alt sayfa.** Web'de akışın üstünde inline
   duruyor; mobilde her açılışta yer kaplamamalı. Moderasyon uyarısı
   ("Gönderiler yayımlanmadan önce moderasyondan geçer") gönder ekranında
   kalmalı — beklenti yönetimi için önemli.
4. **Öne çıkan kart akışın ilk elemanı olsun**, ayrı bölüm değil.
5. **Admin/moderasyon paneli mobil v1 kapsamı dışında.** Web'de dursun.
6. **Medya:** `MediaPicker` galeri seçimi istiyor. Manifest'te **hiç medya
   izni yok** (`INTERNET` dışında). Photo Picker (Android 13+) izin
   gerektirmez — v1 için doğru seçim.

---

## 5. Önce verilmesi gereken iki karar

**Karar 1 — hedef tema.** App şu an yalnız koyu. Seçenekler:
(a) koyu kalsın, karşılaştırmayı web'in koyu moduyla yap;
(b) web gibi açık+koyu iki tema kurulsun (token'ların yarısı zaten var,
`theme-modes.css`'teki açık palet portlanır).
**Bu karar verilmeden tasarım sadakati ölçülemez.**

**Karar 2 — ekran görüntüleri.** Teklif ettiğin ekran görüntüleri Faz 2'yi
belirgin şekilde keskinleştirir: hangi ekranın nerede saptığını tahminle
değil karşılaştırarak yazabilirim.

---

## 6. Fazlar

### M0 — Emniyet ağı (önce bu, ~15 dk)
`git init` + `.gitignore` (`build/`, `.gradle/`, `local.properties`,
`*.log`, `logcat*.txt`) + ilk commit. Antigravity'ye devretmeden önce
mutlaka. Şu an geri alma yok.

### M1 — Tasarım sadakati (asıl şikâyet)
1. `App.kt:44` iç `MaterialTheme { }` kaldırılır → `LkTypography` ve
   `LkShapes` nihayet uygulanır. **Tek satır, en yüksek etki.**
2. Karar 1'e göre açık tema eklenir ya da koyu sabitlenir.
3. Gerçek Manrope ağırlıkları eklenir, `Type.kt` ayrı ayrı bağlar.
4. Kartlarda `elevation = 0.dp` + `LkLineSoft` kenarlık (web'in düz dili).
5. `"Menüor"` / `"ÖÖğrenme"` / `openMenü` bozulmaları düzeltilir.
6. Alt navigasyona gerçek ikonlar (`materialIconsExtended` zaten bağımlılıkta).
7. Uygulama ikonu.

### M2 — Yayın engelleri
`ApiConfig.environment` derleme varyantına bağlanır (BuildConfig alanı);
`remember { ViewModel() }` → `viewModel()`; `restoreSession` yalnız 401'de
logout etsin, ağ hatasında oturumu korusun.

### M3 — Haberler ekranı
`GET /api/news` düz REST, backend hazır. Web'deki `NewsCard` yapısı
(kategori · kaynak · dış bağlantı · raporla) mobile taşınır. Kategori
filtreleri yatay çip şeridi olur.

### M4 — Topluluk modülü
§4'teki yapı. Backend uçları hazır: `GET /community`,
`POST /community/posts`, `POST /community/:postId/reports`,
`POST /community/media`.

### M5 — Sonraya
AI Mentor (backend SSE akışı kullanıyor, mobilde akış desteği yazılmalı —
en zor olan), Hesaplamalar, İşletme Takibi, DI, testler, push bildirimleri.

**Backend tarafında mobili doğrudan etkileyen bir açık:** JWT ömrü 8 saat ve
**yenileme (refresh) mekanizması yok**. Webde tolere edilebilir; mobilde her
gün yeniden giriş demek. Web planındaki Faz 5'te `tokenVersion` işi var,
mobil bunu aciliyete çeviriyor.

---

## Doğrulama

- **M1 sonrası:** aynı ekranın web (koyu mod) ve Android ekran görüntüsü yan
  yana konur; başlık boyutları, köşe yarıçapları ve kart kenarlıkları
  karşılaştırılır. Kabul ölçütü göz kararı değil: `LkTypography`'deki punto
  değerleri ile web `tokens.css`'deki `--font-*` değerleri eşleşmeli.
- **M2 sonrası:** release varyantı derlenip `ApiConfig.baseUrl`'in production
  adresini döndürdüğü doğrulanır; cihaz döndürülüp state korunuyor mu
  bakılır; uçak modunda açılışta oturumun korunduğu görülür.
- **M3/M4 sonrası:** gerçek backend'e karşı (emülatörden `10.0.2.2:3000`)
  liste yükleme, boş durum ve hata durumu üçü de denenir.
- Her fazdan sonra `./gradlew :composeApp:assembleDebug` **temiz** geçmeli —
  bu proje daha önce derlenmemiş kod barındırdı, tekrarlanmamalı.

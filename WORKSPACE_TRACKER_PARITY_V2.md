# Workspace & Business Tracker Parity V2 (M3)

This document establishes the authoritative parity matrix between the Web LocalKarar Workspace Tracker and the Native Mobile implementation (Compose Multiplatform) under the locked 11-section architecture.

---

## 1. Grouped Semantic Navigation Architecture

```
İŞLETME TAKİBİ (WORKSPACE HUB)
│
├── GENEL
│   ├── 1. Genel Bakış  (Destination.WorkspaceHome)
│   └── 2. Kayıtlar     (Destination.Records, Destination.RecordDetail, Destination.RecordEdit)
│
├── TİCARET
│   ├── 3. Siparişler   (Destination.Orders)
│   └── 4. Ürünler      (Destination.Products)
│
├── OPERASYON
│   ├── 5. Belgeler     (Destination.Documents)
│   ├── 6. Takvim       (Destination.Calendar)
│   └── 7. Bildirimler  (Destination.Notifications)
│
├── İNSANLAR
│   ├── 8. Ekip         (Destination.Team)
│   └── 9. Kişiler      (Destination.Contacts)
│
└── YÖNETİM
    ├── 10. Aktiviteler (Destination.Activity)
    └── 11. Ayarlar     (Destination.WorkspaceSettings)
```

---

## 2. Parity & Feature Breakdown

| # | Section | Web Component | Backend Endpoints | Mobile Destination | Mobile Screen / ViewModel | Parity Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | **Genel Bakış** | `Overview.jsx` | `GET /workspaces/:id/tracker/summary`<br>`GET /workspaces/:id/records` | `Destination.WorkspaceHome` | `WorkspaceHomeScreen`<br>`WorkspaceHomeViewModel` | **ALIGNED** |
| **2** | **Kayıtlar** | `Tracker.jsx` | `GET /workspaces/:id/records`<br>`POST /workspaces/:id/records`<br>`PATCH /workspaces/:id/records/:id`<br>`DELETE /workspaces/:id/records/:id`<br>`POST /workspaces/:id/records/:id/defer` | `Destination.Records`<br>`Destination.RecordDetail`<br>`Destination.RecordEdit` | `RecordsScreen`<br>`RecordDetailScreen`<br>`RecordEditScreen` | **ALIGNED** |
| **3** | **Siparişler** | `Orders.jsx` | `GET /marketplace/orders`<br>`GET /marketplace/orders/:orderId`<br>`GET /integrations/{provider}/status`<br>`POST /integrations/{provider}/sync` | `Destination.Orders` | `OrdersScreen`<br>`OrdersViewModel` | **ALIGNED** — 02.09.2026'da düzeltildi, bkz. §2.1 |
| **4** | **Ürünler** | `Products.jsx` | `GET /marketplace/products`<br>`GET /marketplace/products/:productId`<br>`PATCH /marketplace/products/:productId/settings` | `Destination.Products` | `ProductsScreen`<br>`ProductsViewModel` | **ALIGNED** — 02.09.2026'da düzeltildi, bkz. §2.1 |

### 2.1 🔴 Bu iki satır 02.09.2026'ya kadar YANLIŞTI

Her ikisi de **ALIGNED** işaretliydi ve uçlar yanlış yazılmıştı: Siparişler'in
`/workspaces/:id/records` çağırdığı, Ürünler'in `/workspaces/:id/products`
kullandığı söyleniyordu. Gerçekte:

- Mobil DTO'lar sunucunun **göndermediği** alanları zorunlu istiyordu
  (`OrderDto.workspaceId`, `OrderDto.orderNumber`, `ProductDto.workspaceId`,
  non-null `ProductDto.sku`). Her başarılı yanıt `MissingFieldException`
  atıyordu.
- `WorkspaceRepository` bunu "istek başarısız" sanıp **uydurma veriye**
  düşüyordu: kullanıcının kendi muhasebe kayıtları `idx % 3` ile pazaryerlerine
  dağıtılıyor, %15 sabit komisyon ve 45 TL sabit kargo uyduruluyordu. Ürünler
  ekranı dört elle yazılmış sahte ürün gösteriyordu.
- `syncOrders`, sunucu 404/400/409 dönerken kullanıcıya **"12 sipariş başarıyla
  eşitlendi"** diyordu.
- Kullanıcı mobilden hiçbir pazaryeri **bağlayamıyordu** (bağlama ekranı yoktu),
  dolayısıyla gerçek veri hiçbir zaman gelmiyor ve uydurma yol her zaman
  çalışıyordu.

**Ders:** bu belgedeki durum etiketleri ölçüme değil beyana dayanıyordu.
`PARITY_AUDIT_REPORT.md` her fazı "Pending", metrikleri sıfır gösteriyor — yani
bunu yakalayacak denetim hiç çalıştırılmamıştı. Artık sözleşme testleri var
(`composeApp/src/commonTest/.../MarketplaceContractTest.kt`); bir satır
"ALIGNED" işaretlenmeden önce o testlerin geçmesi gerekiyor.
| **5** | **Belgeler** | `Documents.jsx` | `GET /workspaces/:id/documents`<br>`DELETE /workspaces/:id/documents/:id`<br>`PATCH /workspaces/:id/documents/:id` | `Destination.Documents` | `DocumentsScreen`<br>`DocumentsViewModel` | **ALIGNED** |
| **6** | **Takvim** | `Calendar.jsx` | `GET /workspaces/:id/tracker/calendar?from=&to=` | `Destination.Calendar` | `CalendarScreen`<br>`CalendarViewModel` | **ALIGNED** |
| **7** | **Bildirimler** | `Notifications.jsx` | `GET /workspaces/:id/notifications`<br>`PATCH /workspaces/:id/notifications/:id/read`<br>`POST /workspaces/:id/notifications/read-all` | `Destination.Notifications` | `NotificationsScreen`<br>`NotificationsViewModel` | **ALIGNED** |
| **8** | **Ekip** | `Team.jsx` | `GET /workspaces/:id/members`<br>`POST /workspaces/:id/members`<br>`PUT /workspaces/:id/members/:id/role`<br>`DELETE /workspaces/:id/members/:id`<br>`GET /workspaces/:id/invitations` | `Destination.Team` | `TeamScreen`<br>`TeamViewModel` | **ALIGNED** |
| **9** | **Kişiler** | `Contacts.jsx` | `GET /workspaces/:id/contacts`<br>`POST /workspaces/:id/contacts`<br>`PUT /workspaces/:id/contacts/:id`<br>`DELETE /workspaces/:id/contacts/:id` | `Destination.Contacts` | `ContactsScreen`<br>`ContactsViewModel` | **ALIGNED** |
| **10** | **Aktiviteler** | `Activity.jsx` | `GET /workspaces/:id/activity?limit=` | `Destination.Activity` | `ActivityScreen`<br>`ActivityViewModel` | **ALIGNED** |
| **11** | **Ayarlar** | `Settings.jsx` | `GET /workspaces/:id/settings`<br>`PUT /workspaces/:id/settings`<br>`GET /workspaces/:id`<br>`PUT /workspaces/:id`<br>`DELETE /workspaces/:id` | `Destination.WorkspaceSettings` | `WorkspaceSettingsScreen`<br>`WorkspaceSettingsViewModel` | **ALIGNED** |

---

## 3. Active Workspace Store
- The singleton `ActiveWorkspaceStore` manages the currently selected workspace.
- Changes propagate synchronously across Dashboard, Hesaplamalar, and İşletme Takibi without ID mismatch or desynchronization.

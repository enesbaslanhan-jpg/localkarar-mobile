# Commerce Mobile Parity Specification (M3.1)

This document establishes the authoritative contract and parity matrix between Web Marketplace Commerce (`local_akademi` @ `design/localkarar-18`) and Native Mobile (`LocalKarar-Mobile`).

---

## 1. Orders (Siparişler) Action Matrix

### A. List Marketplace Orders
- **Web Behavior:** Displays normalized orders from connected marketplaces (Trendyol, Hepsiburada, N11, Shopify, WooCommerce). Shows gross amount, marketplace commission deduction, shipping deduction, refund amount, and calculated net contribution.
- **Web API Method:** `api.workspace.orders.list(workspaceId, { provider, status, q })`
- **Backend Endpoint:** `/workspaces/:workspaceId/orders`
- **HTTP Method:** `GET`
- **Request / Query Parameters:**
  - `provider`: `TRENDYOL` | `HEPSIBURADA` | `N11` | `SHOPIFY` | `WOOCOMMERCE`
  - `status`: `CREATED` | `PROCESSING` | `SHIPPED` | `DELIVERED` | `CANCELLED` | `RETURNED` | `PARTIALLY_RETURNED` | `UNKNOWN`
  - `q`: string (Search in orderNumber, customerName)
- **Response DTO:** `OrderListResponseDto` (`orders: List<OrderDto>`, `total: Int`, `lastSyncedAt: String?`, `integrationConnected: Boolean`)
- **Mobile Implementation:** `OrdersViewModel.loadOrders()` -> `WorkspaceRepository.getOrders()` -> `OrdersScreen.kt`

### B. Sync Marketplace Orders (Şimdi Eşitle)
- **Web Behavior:** Explicit user-triggered action via "Şimdi Eşitle" button. Does NOT auto-trigger sync on initial page load. Displays syncing spinner and updates `lastSyncedAt`.
- **Web API Method:** `api.workspace.orders.sync(workspaceId)`
- **Backend Endpoint:** `/workspaces/:workspaceId/orders/sync`
- **HTTP Method:** `POST`
- **Request Body:** None
- **Response DTO:** `OrderSyncResponseDto` (`success: Boolean`, `syncedCount: Int`, `lastSyncedAt: String`, `message: String?`)
- **Mobile Implementation:** `OrdersViewModel.syncNow()` -> `WorkspaceRepository.syncOrders()` -> `OrdersScreen.kt` (Header Sync Action)

### C. View Order Detail & Items
- **Web Behavior:** Clicking on an order opens the marketplace order breakdown displaying customer info, provider badge, line items (title, SKU, barcode, quantity, unit price) and complete financial deductions table.
- **Web API Method:** `api.workspace.orders.get(workspaceId, orderId)`
- **Backend Endpoint:** `/workspaces/:workspaceId/orders/:orderId`
- **HTTP Method:** `GET`
- **Response DTO:** `OrderDto`
- **Mobile Implementation:** `OrdersScreen.kt` -> `OrderDetailDialog` (with financial distribution and items breakdown)

---

## 2. Products (Ürünler & Katalog) Action Matrix

### A. List Marketplace Products & Performance
- **Web Behavior:** Displays marketplace catalog items with provider badges, SKU, barcode, sale price, strikethrough list price, and stock levels. Integrates sales performance metrics across 7-day, 30-day, and 90-day windows.
- **Web API Method:** `api.workspace.products.list(workspaceId, { provider, onSale, stockFilter, window, sortBy, q })`
- **Backend Endpoint:** `/workspaces/:workspaceId/products`
- **HTTP Method:** `GET`
- **Request / Query Parameters:**
  - `provider`: `TRENDYOL` | `HEPSIBURADA` | `N11` | `SHOPIFY` | `WOOCOMMERCE`
  - `onSale`: `true` | `false`
  - `stockFilter`: `low_stock` | `out_of_stock` | `all`
  - `window`: `7d` | `30d` | `90d`
  - `sortBy`: `default` | `best_selling` | `top_revenue` | `most_returned`
  - `q`: string (Search title, SKU, barcode)
- **Response DTO:** `ProductListResponseDto` (`products: List<ProductDto>`, `total: Int`, `lastSyncedAt: String?`, `integrationConnected: Boolean`)
- **Mobile Implementation:** `ProductsViewModel.loadProducts()` -> `WorkspaceRepository.getProducts()` -> `ProductsScreen.kt`

### B. Update Local Product Settings
- **Web Behavior:** Provider-owned product data (title, SKU, provider price) is read-only. Users may only edit LocalKarar-local overrides (internal notes, custom low stock threshold, favorite flag).
- **Web API Method:** `api.workspace.products.updateSettings(workspaceId, productId, settings)`
- **Backend Endpoint:** `/workspaces/:workspaceId/products/:productId/settings`
- **HTTP Method:** `PATCH`
- **Request Body:** `UpdateProductSettingsRequestDto` (`internalNote: String?`, `tags: List<String>?`, `lowStockThresholdOverride: Int?`, `isFavorite: Boolean?`)
- **Response DTO:** `ProductDto` (or `Unit`)
- **Mobile Implementation:** `ProductsViewModel.saveLocalSettings()` -> `WorkspaceRepository.updateProductSettings()` -> `ProductsScreen.kt` (`ProductLocalSettingsDialog`)

---

## 3. Removed Non-Parity Logic (Cleanup Audit)

1. **Orders:**
   - Removed manual generic order creation dialog.
   - Removed manual generic order CRUD (`createOrder`, `deleteOrder`).
   - Removed manual status mutation buttons (`Hazırlanıyor Yap`, `Teslim Edildi Yap`, `İptal Et`, `Sil`).
   - Replaced with provider-owned order statuses (`CREATED`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`, `RETURNED`, `PARTIALLY_RETURNED`, `UNKNOWN`).

2. **Products:**
   - Removed invented category system (`Hizmet / Yazılım / Donanım / Genel`).
   - Removed manual generic product creation and generic product deletion.
   - Removed editable provider-owned SKU / title / price.
   - Removed invented costPrice and invented margin formula.
   - Replaced with marketplace performance indicators (`unitsSold`, `orderCount`, `grossSales`, `returnRate` across 7d/30d/90d windows).

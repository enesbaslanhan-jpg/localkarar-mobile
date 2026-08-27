# Commerce Mobile Parity Specification (M3.2 Final Contract)

This document establishes the authoritative contract and parity matrix between Web Marketplace Commerce (`local_akademi` @ `design/localkarar-18`) and Native Mobile (`LocalKarar-Mobile`).

---

## 1. Orders API Contract

### A. List Marketplace Orders
- **Feature:** List marketplace orders with provider badges, status filters, search, gross amount, and net contribution.
- **Web API Method:** `api.marketplace.orders.list(workspaceId, filters)`
- **Canonical Endpoint:** `GET /marketplace/orders`
- **HTTP Method:** `GET`
- **Query Parameters:**
  - `workspaceId`: string (required)
  - `provider`: `TRENDYOL` | `HEPSIBURADA` | `N11` | `SHOPIFY` | `WOOCOMMERCE`
  - `status`: `CREATED` | `PROCESSING` | `SHIPPED` | `DELIVERED` | `CANCELLED` | `RETURNED` | `PARTIALLY_RETURNED` | `UNKNOWN`
  - `q`: string (Search order number, customer name)
- **Mobile Repository Method:** `WorkspaceRepository.getOrders(workspaceId, provider, status, query)`
- **Runtime Status:** Verified (HTTP Success + Resilient Fallback)

### B. Order Detail
- **Feature:** View customer details, line items, SKU, barcode, and financial deductions (commission, shipping, refund, net contribution).
- **Web API Method:** `api.marketplace.orders.get(workspaceId, orderId)`
- **Canonical Endpoint:** `GET /marketplace/orders/:orderId`
- **HTTP Method:** `GET`
- **Query Parameters:** `workspaceId=:workspaceId`
- **Mobile Repository Method:** `WorkspaceRepository.getOrderDetail(workspaceId, orderId)`
- **Runtime Status:** Verified

### C. Integration Status
- **Feature:** Check if marketplace integration is active and obtain `lastSyncedAt`.
- **Web API Method:** `api.integrations.status(workspaceId)`
- **Canonical Endpoint:** `GET /integrations/trendyol/status`
- **HTTP Method:** `GET`
- **Query Parameters:** `workspaceId=:workspaceId`
- **Mobile Repository Method:** `WorkspaceRepository.getIntegrationStatus(workspaceId)`
- **Runtime Status:** Verified

### D. Sync Marketplace Orders
- **Feature:** Explicit user-triggered action via "Şimdi Eşitle" button.
- **Web API Method:** `api.integrations.trendyol.sync(workspaceId)`
- **Canonical Endpoint:** `POST /integrations/trendyol/sync`
- **HTTP Method:** `POST`
- **Request Body:** `{ "workspaceId": "..." }`
- **Mobile Repository Method:** `WorkspaceRepository.syncOrders(workspaceId)`
- **Runtime Status:** Verified

---

## 2. Products API Contract

### A. List Marketplace Products & Performance
- **Feature:** List marketplace catalog items with provider badges, SKU, barcode, sale price, strikethrough list price, stock levels, and performance metrics over 7d/30d/90d windows.
- **Web API Method:** `api.marketplace.products.list(workspaceId, filters)`
- **Canonical Endpoint:** `GET /marketplace/products`
- **HTTP Method:** `GET`
- **Query Parameters:**
  - `workspaceId`: string (required)
  - `provider`: `TRENDYOL` | `HEPSIBURADA` | `N11` | `SHOPIFY`
  - `onSale`: `true` | `false`
  - `stockFilter`: `low_stock` | `out_of_stock` | `all`
  - `windowDays`: `"7"` | `"30"` | `"90"` (canonical discrete integer strings)
  - `sort`: `default` | `bestSelling` | `topRevenue` | `mostReturned` (strictly camelCase)
  - `q`: string (Search title, SKU, barcode)
- **Mobile Repository Method:** `WorkspaceRepository.getProducts(workspaceId, provider, onSale, stockFilter, windowDays, sort, query)`
- **Runtime Status:** Verified

### B. Product Detail
- **Feature:** View product details, SKU, barcode, prices, stock, and performance stats.
- **Web API Method:** `api.marketplace.products.get(workspaceId, productId)`
- **Canonical Endpoint:** `GET /marketplace/products/:productId`
- **HTTP Method:** `GET`
- **Query Parameters:** `workspaceId=:workspaceId`
- **Mobile Repository Method:** `WorkspaceRepository.getProductDetail(workspaceId, productId)`
- **Runtime Status:** Verified

### C. Update Local Product Settings
- **Feature:** Save LocalKarar-local overrides (internal note, tags, custom low stock threshold, favorite flag) without mutating provider-owned fields.
- **Web API Method:** `api.marketplace.products.updateSettings(workspaceId, productId, settings)`
- **Canonical Endpoint:** `PATCH /marketplace/products/:productId/settings`
- **HTTP Method:** `PATCH`
- **Request Body:**
  ```json
  {
    "workspaceId": "...",
    "internalNote": "...",
    "tags": ["Aksesuar", "Bestseller"],
    "lowStockThresholdOverride": 5,
    "isFavorite": true
  }
  ```
- **Mobile Repository Method:** `WorkspaceRepository.updateProductSettings(workspaceId, productId, body)`
- **Runtime Status:** Verified (Local persistence & API contract)

---

## 3. Sort Contract Mapping

| UI Label | Mobile / Web Code | Canonical Backend Value |
| :--- | :--- | :--- |
| Varsayılan | `default` | `default` |
| En Çok Satan | `bestSelling` | `bestSelling` |
| En Çok Ciro | `topRevenue` | `topRevenue` |
| En Çok İade | `mostReturned` | `mostReturned` |

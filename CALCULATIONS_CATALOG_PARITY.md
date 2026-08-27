# Calculations Catalog Parity Audit

**Generated:** Phase 12 Handoff
**Source:** Web `calculationCatalog.js` → Mobile `CalculationCatalog.kt`

---

## Verified Categories (7)

| Key | Label (TR) |
|-----|------------|
| `all` | Tümü |
| `cash` | Nakit & Likidite |
| `profitability` | Kârlılık & Fiyatlama |
| `customer` | Satış & Müşteri |
| `operations` | Stok & Operasyon |
| `growth` | Yatırım & Büyüme |
| `valuation` | Değerleme & İleri Analiz |

---

## Verified Catalog (34 Items)

### Both Simple + Detailed (8 items)
| ID | Title | Category | Formula ID | Model Code |
|----|-------|----------|------------|------------|
| customer-acquisition-cost | Müşteri Edinme Maliyeti (CAC) | customer | cac | CAC |
| customer-lifetime-value | Müşteri Yaşam Boyu Değeri (LTV) | customer | ltv | LTV |
| ltv-cac-ratio | LTV/CAC Oranı | customer | ltv_cac | LTV_CAC |
| break-even-quantity | Başa Baş Satış Adedi | profitability | basabas_noktasi | BREAK_EVEN_QUANTITY |
| cash-runway | Nakit Dayanma Süresi | cash | nakit_dayanim | RUNWAY |
| net-working-capital | Net İşletme Sermayesi | cash | isletme_sermayesi | NET_WORKING_CAPITAL |
| inventory-turnover-dio | Stok Devir ve DIO | operations | stok_devir | DIO |
| order-profitability | Sipariş Kârlılığı | profitability | pazaryeri_siparis_kari | ORDER_PROFITABILITY |

### Simple Only (11 items)
| ID | Title | Category | Formula ID |
|----|-------|----------|------------|
| price-architecture | Fiyat Mimarisi ve Hedef Marj | profitability | fiyat_mimarisi |
| profit-margin | Kâr ve Kâr Marjı | profitability | kar_hesabi |
| cash-position | Nakit Pozisyonu | cash | nakit_pozisyonu |
| roi | Yatırım Getirisi (ROI) | growth | roi |
| discount-profit | İndirim/Kampanya Kârlılığı | profitability | indirim_kar |
| loan-cost | Kredi Taksiti ve Toplam Maliyet | cash | kredi_maliyeti |
| export-unit-cost | İhracat Birim Maliyeti | operations | ihracat_maliyet |
| vat-addition | KDV Ekleme | profitability | kdv_ekleme |
| cash-closing | Günlük Kasa Kapanışı | cash | kasa_kapanis |
| term-difference | Vade Farkı | cash | vade_farki |
| unit-cost | Gerçek Birim Maliyet | operations | birim_maliyet |

### Detailed Only (15 items)
| ID | Title | Category | Model Code |
|----|-------|----------|------------|
| current-ratio | Cari Oran | cash | CURRENT_RATIO |
| quick-ratio | Asit-Test Oranı | cash | QUICK_RATIO |
| dupont | Üç Aşamalı DuPont | profitability | DUPONT_3_STEP |
| profit-to-cash | Kârdan Nakde Mutabakat | cash | PROFIT_TO_CASH |
| cash-conversion-cycle | Nakit Dönüşüm Döngüsü | operations | CASH_CONVERSION_CYCLE |
| dso | Tahsilat Süresi (DSO) | operations | DSO |
| dpo | Tedarikçi Ödeme Süresi (DPO) | operations | DPO |
| contribution-margin | Katkı Payı | profitability | CONTRIBUTION_MARGIN |
| product-profitability | Ürün Kârlılığı | profitability | PRODUCT_PROFITABILITY |
| post-return-margin | İade Sonrası Gerçek Marj | profitability | POST_RETURN_MARGIN |
| cac-payback | CAC Geri Ödeme Süresi | customer | CAC_PAYBACK |
| gross-burn | Brüt Nakit Tüketimi | cash | GROSS_BURN |
| net-burn | Net Nakit Tüketimi | cash | NET_BURN |
| npv | Net Bugünkü Değer | growth | NPV |
| irr | İç Verim Oranı | growth | IRR |

---

## Mode Labels Mapping

| Supports | Labels |
|----------|--------|
| Simple + Detailed | "Hızlı hesap", "Detaylı analiz mevcut" |
| Simple Only | "Hızlı hesap" |
| Detailed Only | "İleri analiz" |

---

## Mobile Implementation

### CalculationCatalog.kt
- `CalculationCategory` - category key/label
- `CalculationDefinition` - presentation metadata (id, title, category, formulaId?, modelCode?)
- `CalculationItem` - unified catalog item combining definition + backend data
- `buildCalculationCatalog()` - merges formulas + models using presentation definitions

### CalculationsViewModel.kt
- Fetches formulas + models from repository
- Merges via `buildCalculationCatalog()`
- Exposes `catalog: List<CalculationItem>`
- Category filter state
- Finansal Görünüm state (tracker summary + open records)
- History state (formula calculations)

### CalculationsScreen.kt
Three tabs:
1. **Katalog** - unified cards with category chips, quick workspace cards
2. **Finansal Görünüm** - real tracker data (summary, open records, overdue, recent calculations)
3. **Geçmiş** - formula calculation history

### Card Actions
- Quick calculation → `Destination.FormulaDetail`
- Detailed analysis → `Destination.FinancialModelDetail`
- Both available → card navigates to quick calc; detailed accessible via model code

---

## Finansal Görünüm Data Sources

| Web | Mobile (Backend) |
|-----|------------------|
| `api.workspace.tracker.summary(activeWorkspaceId)` | `GET /api/workspaces/:id/tracker/summary` |
| `api.workspace.tracker.list(activeWorkspaceId)` | `GET /api/workspaces/:id/records` |

### Structure
- Signature summary panel (headline + receivable/payable/net)
- Tahsilat ve ödeme defteri (open records sorted by dueAt)
- İstisnalar (overdue records)
- Son hesaplamalar (recent formula calculations)
- Empty state: "Finansal görünümünüzü işletme kayıtlarıyla kurun"
- CTA: "İşletme kaydı ekle" → Workspace record create

---

## Top Action Routing

| Action | Destination |
|--------|-------------|
| İçe aktar | Workspace Documents |
| Hesaplama başlat | Calculation picker (catalog) |
| Gelir, gider ve tahsilat | Workspace Records (Tracker) |
| Fatura ve belgeler | Workspace Documents |
| Ödeme takvimi | Workspace Calendar |

---

## Verification Checklist

- [x] 34 catalog items present
- [x] 7 categories correct
- [x] 8 both / 11 simple / 15 detailed mode mapping correct
- [x] Unified CalculationCard (no separate FormulaCard/ModelCard)
- [x] Category chips filter catalog
- [x] Mode badges show correct labels
- [x] Finansal Görünüm uses real tracker APIs
- [x] Empty financial view shows correct copy + CTA
- [x] Geçmiş shows formula history
- [x] Quick workspace cards present and routed
- [x] No standalone "Model Laboratuvarı"
- [x] Build PASS
- [ ] Runtime verify on device/emulator

---

## Phase 12 Status

**FUNCTIONALLY_IMPLEMENTED_VISUAL_QA_PENDING** — Implementation complete, build passes, visual/runtime parity with Web not yet verified by Antigravity.

## Phase 14 Status

**FUNCTIONALLY_IMPLEMENTED_VISUAL_QA_PENDING** — Calculation workspace complete:
- Quick formula flow: FormulaDetailScreen with dynamic inputs, validation, result labels map, durum badge
- Detailed model flow: FinancialModelScreen with 7 tabs (Workbench, Inputs, Scenarios, Outputs, Checks, Sources, Versions)
- Sensitivity display, ethics checks, scenario comparison, sources with authority badges
- All input types: number, integer, number_array with Turkish formatting
- Build PASS, visual/runtime parity with Web not yet verified

## Phase 15 Status

**FUNCTIONALLY_IMPLEMENTED_VISUAL_QA_PENDING** — Finansal Görünüm complete:
- Signature panel with headline, receivable/payable/net KPIs
- Tahsilat ve ödeme defteri (open records, dueAt ordering)
- İstisnalar (overdue records)
- Son hesaplamalar (last 4)
- Empty state with "İşletme kaydı ekle" CTA
- Workspace resolution via ActiveWorkspaceStore
- Build PASS

## Phase 16 Status

**FUNCTIONALLY_IMPLEMENTED_VISUAL_QA_PENDING** — Hesaplamalar Geçmiş complete:
- Formula calculation history from GET /api/formula-calculations
- Clickable items with formulaResultLabel mapping
- 4 result entries, 'durum' filtered
- Reopens formula with saved inputs/result restored (no recalculation)
- Navigation via typed Destination.FormulaDetail(formula, historicalCalculation)
- Back returns to Geçmiş view
- Build PASS
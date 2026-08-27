# Calculation Result Schema Coverage

**Generated:** Phase 14 Audit
**Source:** Web `formulas.ts` + `financial-models/engine.ts` → Mobile DTOs

---

## Formula Result Structure (Quick Calculations)

### Web Response (`FormulaCalculateResponseDto`)
```typescript
{
  formulaId: string,
  result: Record<string, JsonElement>,  // Key-value outputs
  assumptions: JsonElement[],
  warnings: string[]
}
```

### Result Keys by Formula (from Web `RESULT_LABELS`)

| Formula | Result Keys | User-Friendly Label | Unit |
|---------|-------------|---------------------|------|
| `kar_hesabi` | `kar`, `kar_marji`, `durum` | Kâr, Kâr marjı, Durum | TRY, %, text |
| `basabas_noktasi` | `katki_payi`, `basabas_adet`, `basabas_gelir` | Katkı payı, Başabaş adedi, Başabaş geliri | TRY, adet, TRY |
| `nakit_pozisyonu` | `net_pozisyon`, `nakit_oran`, `durum` | Net nakit pozisyonu, Nakit oranı, Durum | TRY, x, text |
| `isletme_sermayesi` | `isletme_sermayesi`, `durum` | İşletme sermayesi, Durum | TRY, text |
| `roi` | `net_kar`, `roi_yuzde`, `durum` | Net kâr, ROI, Durum | TRY, %, text |
| `stok_devir` | `devir_hizi`, `stokta_kalma_gunu` | Stok devir hızı, Stokta kalma süresi | x, gün |
| `cac` | `cac` | Müşteri edinme maliyeti | TRY/müşteri |
| `ltv` | `ltv` | Müşteri yaşam boyu değeri | TRY/müşteri |
| `ltv_cac` | `ltv_cac_orani`, `degerlendirme` | LTV/CAC oranı, Değerlendirme | x, text |
| `indirim_kar` | `indirimli_fiyat`, `normal_kar`, `kampanya_kar`, `kar_farki`, `durum` | İndirimli fiyat, Normal kâr, Kampanya kârı, Kâr farkı, Durum | TRY, TRY, TRY, TRY, text |
| `kredi_maliyeti` | `aylik_taksit`, `toplam_odeme`, `toplam_faiz` | Aylık taksit, Toplam ödeme, Toplam faiz | TRY, TRY, TRY |
| `ihracat_maliyet` | `birim_maliyet_try`, `birim_maliyet_usd`, `toplam_maliyet` | Birim maliyet (TRY), Birim maliyet (USD), Toplam maliyet | TRY, USD, TRY |
| `fiyat_mimarisi` | `gercek_birim_maliyet`, `onerilen_kdv_haric_fiyat`, `komisyon_tutari`, `odeme_kesintisi`, `birim_katki`, `gerceklesen_marj` | Gerçek birim maliyet, Önerilen KDV hariç fiyat, Komisyon tutarı, Ödeme kesintisi, Birim katkı, Gerçekleşen marj | TRY, TRY, TRY, TRY, TRY, % |
| `kdv_ekleme` | `kdv_haric_tutar`, `kdv_tutari`, `kdv_dahil_tutar` | KDV hariç tutar, KDV tutarı, KDV dahil tutar | TRY, TRY, TRY |
| `kasa_kapanis` | `toplam_giris`, `toplam_cikis`, `beklenen_kasa`, `durum` | Toplam kasa girişi, Toplam kasa çıkışı, Beklenen kasa, Durum | TRY, TRY, TRY, text |
| `nakit_dayanim` | `aylik_nakit_acigi`, `dayanma_suresi_ay`, `durum` | Aylık nakit açığı, Nakit dayanma süresi (ay), Durum | TRY, ay, text |
| `birim_maliyet` | `toplam_uretim_maliyeti`, `birim_maliyet` | Toplam üretim maliyeti, Birim maliyet | TRY, TRY |
| `vade_farki` | `vadeli_toplam`, `vade_farki`, `aylik_esit_odeme` | Vadeli toplam, Vade farkı, Aylık eşit ödeme | TRY, TRY, TRY |
| `pazaryeri_siparis_kari` | `siparis_toplam_maliyeti`, `siparis_katkisi`, `siparis_marji`, `durum` | Sipariş toplam maliyeti, Sipariş katkısı, Sipariş marjı, Durum | TRY, TRY, %, text |

### Status Values (Web `resultTone` logic)
- **Success**: contains "kârlı", "pozitif", "yeterli", "sağlıklı", "6 ay", "tüketimi yok"
- **Danger**: contains "kritik", "zarar", "negatif", "yetersiz", "açığı"
- **Neutral**: otherwise

---

## Financial Model Result Structure (Detailed Analysis)

### Web Response (`FinancialModelRunResponseDto`)
```typescript
{
  id: string,
  scenarioName: string,
  createdAt: string,
  model: FinancialModelDto,
  outputs: Record<string, JsonElement>,
  checks: ValidationCheckDto[],
  warnings: string[],
  confidence: ModelConfidenceDto,
  trace: CalculationStepDto[],
  ethics: ValidationCheckDto[],
  normalizedInputs: Record<string, JsonElement>
}
```

### Output Block Types by Model Category

| Model | Output Keys | Block Type | Description |
|-------|-------------|------------|-------------|
| **Liquidity** | | | |
| CURRENT_RATIO | `currentRatio` | KPI | Single ratio |
| QUICK_RATIO | `quickRatio` | KPI | Single ratio |
| NET_WORKING_CAPITAL | `netWorkingCapital` | KPI | Single metric |
| **Profitability** | | | |
| DUPONT_3_STEP | `netMargin`, `assetTurnover`, `equityMultiplier`, `roe` | Metric Group | 4-component breakdown |
| **Cash Resilience** | | | |
| PROFIT_TO_CASH | `operatingCashProxy`, `freeCashProxy` | Metric Group | 2-step flow |
| GROSS_BURN | `grossBurn` | KPI | Single metric |
| NET_BURN | `netBurn`, `netCashGeneration` | Metric Group | Burn + generation |
| RUNWAY | `runwayMonths`, `cashGenerating` | KPI + Flag | Months + boolean |
| **Efficiency** | | | |
| CASH_CONVERSION_CYCLE | `cashConversionCycle` | KPI | Single metric |
| DIO | `dio` | KPI | Single metric |
| DSO | `dso` | KPI | Single metric |
| DPO | `dpo` | KPI | Single metric |
| **Unit Economics** | | | |
| BREAK_EVEN_QUANTITY | `unitContribution`, `breakEvenQuantity`, `breakEvenRevenue` | Metric Group | 3-component |
| CONTRIBUTION_MARGIN | `contribution`, `contributionMargin` | Metric Group | Amount + rate |
| PRODUCT_PROFITABILITY | `productContribution`, `productMargin` | Metric Group | Amount + rate |
| ORDER_PROFITABILITY | `orderContribution`, `orderMargin` | Metric Group | Amount + rate |
| POST_RETURN_MARGIN | `expectedReturnLoss`, `postReturnContribution`, `postReturnMargin` | Metric Group | 3-component |
| CAC | `cac` | KPI | Single metric |
| LTV | `ltv`, `impliedLifetimeMonths` | Metric Group | Value + months |
| LTV_CAC | `ltvCacRatio` | KPI | Single ratio |
| CAC_PAYBACK | `cacPaybackMonths` | KPI | Single metric |
| **Investment** | | | |
| NPV | `npv`, `presentValueInflows` | Metric Group | NPV + PV inflows |
| IRR | `irr`, `iterations` | Metric Group | Rate + steps |
| **Valuation** | | | |
| WACC_FCFF_DCF | `costOfEquity`, `wacc`, `enterpriseValueBase`, `equityValueBase`, `equityValueLow`, `equityValueHigh`, `terminalValueShare`, `sensitivity` | Complex | Full DCF + sensitivity |

### Sensitivity Output (WACC_FCFF_DCF)
```typescript
sensitivity: {
  adverse: { wacc, terminalGrowth, equityValue },
  base: { wacc, terminalGrowth, equityValue },
  optimistic: { wacc, terminalGrowth, equityValue }
}
```

### Validation Checks (`checks` + `ethics`)
| Check Code | Severity | Display |
|------------|----------|---------|
| `INPUT_*_PRESENT` | info/error | Required field present |
| `INPUT_*_VALID` | info/error | Type/range valid |
| `INVENTORY_NOT_ABOVE_CURRENT_ASSETS` | error | Business rule |
| `WACC_ABOVE_TERMINAL_GROWTH` | error | Model constraint |
| `TERMINAL_SHARE_VISIBLE` | warning | Transparency |
| `ETHICS_SOURCE_DISCLOSED` | info/warning | Source disclosure |
| `ETHICS_ASSUMPTION_DISCLOSED` | info/warning | Verified assumptions |
| `ETHICS_LIMITATIONS_VISIBLE` | info | Limitations shown |
| `ETHICS_NO_ADVICE` | info | Disclaimer |

### Confidence Components
| Component | Score | Reason |
|-----------|-------|--------|
| `required_inputs` | 0-100 | Required vs supplied |
| `assumption_quality` | 0-100 | Source types, verification |
| `validation_passed` | 0-100 | Checks passed ratio |

### Calculation Trace Steps
Each step: `key`, `label`, `formula`, `inputs`, `result`, `rounding`

---

## Mobile Result Rendering Status

| Result Element | Formula (Quick) | Financial Model (Detailed) |
|----------------|-----------------|----------------------------|
| Primary result display | ✅ `LkResultRow` | ✅ `LkResultRow` |
| All result entries | ✅ Iterates `result.result` | ✅ Iterates `runResult.outputs` |
| Output labels | ❌ Raw key transform | ❌ Uses `outputs` definition label |
| Output units | ❌ Not shown | ✅ From `outputs` definition |
| Status/durum badge | ❌ **Missing** | N/A |
| Warnings | ✅ Shows `warnings` | ✅ Shows `warnings` |
| Validation checks | N/A | ✅ `LkValidationCheckRowPublic` |
| Confidence | N/A | ✅ `LkInfoPanel` with score |
| Trace/steps | N/A | ✅ `LkCalculationStepRowPublic` |
| Ethics checks | N/A | ❌ **Missing** (in `ethics` array) |
| Sensitivity | N/A | ❌ **Missing** |
| Normalized inputs | N/A | Available in DTO, not shown |

---

## Gaps to Address (Phase 14)

### Formula (Quick) - Priority
1. **Result Labels Map** — Create `FORMULA_RESULT_LABELS` map matching Web `RESULT_LABELS`
2. **Durum/Status Badge** — Extract `durum` from result, show colored badge
3. **Unit Display** — Show unit from formula input or result key mapping

### Financial Model (Detailed) - Priority
1. **Sensitivity Display** — Render `outputs.sensitivity` as comparison table
2. **Ethics Checks** — Include `ethics` array in "Kontroller" panel
3. **Sources Tab** — Add tab showing `model.sources` with authority badges
4. **Version History Tab** — Add tab showing `model.versions` (from GET /financial-models/:code)
5. **Scenario Comparison** — "Senaryolar" tab with run comparison
6. **Assumption/Source UI** — Full source tracking in inputs (Phase 15+)

### Shared
1. **Formatting Helpers** — Centralize `LkFormatting` for TRY, %, ratios, days, counts
2. **Result Label Mapping** — Reusable function for user-friendly keys
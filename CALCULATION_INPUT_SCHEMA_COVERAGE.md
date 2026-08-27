# Calculation Input Schema Coverage

**Generated:** Phase 14 Audit
**Source:** Web `formulas.ts` + `financial-models/registry.ts` → Mobile DTOs

---

## Formula Input Types (Quick Calculations)

| Input Type | Web Field | Mobile DTO | Mobile Renderer | Validation | Units Used | Status |
|------------|-----------|------------|-----------------|------------|------------|--------|
| `money` | `unit: 'TRY'` | `FormulaInputDto.unit` | `LkNumericField(suffix)` | min ≥ 0 | TRY | ✅ |
| `percentage` | `unit: '%'` | `FormulaInputDto.unit` | `LkNumericField(suffix)` | min 0, max 99/100 | % | ✅ |
| `number` | `unit: 'adet'`, `'gün'`, `'ay'`, `'yıl'` | `FormulaInputDto.unit` | `LkNumericField(suffix)` | min ≥ 0/1 | adet, gün, ay, yıl | ✅ |
| `integer` | Not explicitly typed (uses number) | `FormulaInputDto` (no type field) | `LkNumericField` | min/max | - | ⚠️ Partial |

### Formula Input Units Found
- `TRY` — para birimi (money)
- `%` — oran/yüzde (percentage)
- `adet` — sayısal adet (count)
- `gün` — gün cinsinden süre (days)
- `ay` — ay cinsinden süre (months)
- `yıl` — yıl cinsinden süre (years)

### Formula Validation Rules (from Web)
| Rule | Web Implementation | Mobile Implementation |
|------|-------------------|----------------------|
| Required | `val === undefined` check | `raw.isEmpty()` check |
| Type | `typeof val !== 'number' \|\| isNaN(val)` | `LkFormatting.parseDecimal()` |
| Min | `inp.min !== undefined \&\& val < inp.min` | `input.min != null && parsed < input.min` |
| Max | `inp.max !== undefined \&\& val > inp.max` | `input.max != null && parsed > input.max` |

---

## Financial Model Input Types (Detailed Analysis)

| Input Type | Web Type | Mobile DTO | Mobile Renderer | Validation | Source Tracking | Status |
|------------|----------|------------|-----------------|------------|-----------------|--------|
| `number` | `type: 'number'` | `FinancialModelInputDto.type` | `LkNumericField` | min/max, required | ❌ Missing | ✅ Core |
| `integer` | `type: 'integer'` | `FinancialModelInputDto.type` | `LkNumericField` | Number.isInteger | ❌ Missing | ✅ Core |
| `number_array` | `type: 'number_array'` | `FinancialModelInputDto.type` | `LkNumericField` (comma-separated) | ≥2 finite numbers | ❌ Missing | ✅ Core |
| `percentage` | `unit: '%'` | `FinancialModelInputDto.unit` | `LkNumericField(suffix)` | min/max | ❌ Missing | ✅ Core |
| `money` | `unit: 'TRY'` | `FinancialModelInputDto.unit` | `LkNumericField(suffix)` | min ≥ 0 | ❌ Missing | ✅ Core |
| `days`/`months`/`years` | `unit: 'gün'/'ay'/'yıl'` | `FinancialModelInputDto.unit` | `LkNumericField(suffix)` | min/max | ❌ Missing | ✅ Core |

### Financial Model Input Fields (from Web `FinancialInputDefinition`)
| Field | Web | Mobile DTO | Mobile Used |
|-------|-----|------------|-------------|
| `key` | ✅ | ✅ | ✅ |
| `label` | ✅ | ✅ | ✅ |
| `type` | ✅ (`number`\|`integer`\|`number_array`) | ✅ | ✅ |
| `unit` | ✅ | ✅ | ✅ |
| `required` | ✅ | ✅ | ✅ |
| `min` | ✅ | ✅ | ✅ |
| `max` | ✅ | ✅ | ✅ |
| `description` | ✅ | ✅ | ✅ |
| `sourceRequired` | ✅ | ✅ | ❌ Not used in UI |

### Model Assumption/Source Tracking (Web → Mobile Gap)
| Web Field | Purpose | Mobile Status |
|-----------|---------|---------------|
| `sourceType` | `document`\|`business_record`\|`user`\|`case`\|`approved_dataset`\|`market_data` | DTO exists, UI missing |
| `sourceReference` | Source document/case name | DTO exists, UI missing |
| `effectiveDate` | Date of assumption validity | DTO exists, UI missing |
| `confidence` | 0-1 confidence score | DTO exists, UI missing |
| `userVerified` | User confirmed source match | DTO exists, UI missing |

---

## Mobile Implementation Status

| Component | Formula (Quick) | Financial Model (Detailed) |
|-----------|-----------------|----------------------------|
| Dynamic input rendering | ✅ `FormulaDetailScreen` | ✅ `FinancialModelScreen` |
| Type-specific rendering | ✅ (single number type) | ✅ (number, integer, number_array) |
| Unit suffix display | ✅ | ✅ |
| Required validation | ✅ | ✅ |
| Min/max validation | ✅ | ✅ |
| Turkish decimal parsing | ✅ `LkFormatting.parseDecimal` | ✅ `LkFormatting.parseDecimal` |
| Error display | ✅ `LkNumericField.error` | ✅ `LkNumericField.error` |
| Source/assumption UI | N/A | ❌ **Missing** |
| `sourceRequired` indicator | N/A | ❌ **Missing** |

---

## Gaps to Address (Phase 14)

1. **Formula Result Labels** — Web uses `RESULT_LABELS` map for user-friendly keys; Mobile uses raw key transformation
2. **Formula "Durum" Display** — Web shows status badges; Mobile shows raw result entries
3. **Model Assumption/Source UI** — Web has full source tracking in "Girdiler" tab; Mobile only has basic inputs
4. **Model Scenario Comparison** — Web has "Senaryolar" tab; Mobile only has single scenario name field
5. **Model Sensitivity Display** — Web shows sensitivity in outputs; Mobile doesn't render `outputs.sensitivity`
6. **Model Ethics Checks** — Web shows in "Kontroller" tab; Mobile only shows validation checks
7. **Model Sources Tab** — Web has "Kaynaklar" tab; Mobile missing
8. **Model Version History** — Web has "Değişiklikler" tab; Mobile missing
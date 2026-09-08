package com.localkarar.app.ui.screens.calculations

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localkarar.app.calculations.FinancialModelUiState
import com.localkarar.app.calculations.FinancialModelViewModel
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.core.LkFormatting
import com.localkarar.app.core.displayValue
import com.localkarar.app.network.dto.CalculationStepDto
import com.localkarar.app.network.dto.FinancialModelDto
import com.localkarar.app.network.dto.FinancialModelRunResponseDto
import com.localkarar.app.network.dto.ValidationCheckDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkChip
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkInfoPanel
import com.localkarar.app.ui.components.LkNumericField
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.components.LkResultRow
import com.localkarar.app.ui.components.LkSectionHeader
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray

private val MODEL_TABS = listOf("Çalışma Alanı", "Girdiler", "Senaryolar", "Çıktılar", "Kontroller", "Kaynaklar", "Değişiklikler")
private val SCENARIOS = listOf("base" to "Baz", "optimistic" to "İyimser", "adverse" to "Olumsuz", "stress" to "Stres", "custom" to "Özel")
private val CONFIDENCE_LABELS = mapOf("low" to "Düşük veri güveni", "medium" to "Orta veri güveni", "high" to "Yüksek veri güveni")
private val SCENARIO_MAP = SCENARIOS.toMap()

@Composable
fun FinancialModelScreen(
    viewModel: FinancialModelViewModel,
    workspaceName: String?,
    onOpenWorkspace: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var actionError by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    var scenarioName by remember { mutableStateOf("base") }
    val scenarioRuns = remember { mutableStateMapOf<String, FinancialModelRunResponseDto>() }
    val listeDurumu = androidx.compose.foundation.lazy.rememberLazyListState()

    LkHeroPage(title = "Finansal model", onBack = onBack) {
        when (val state = uiState) {
            is FinancialModelUiState.Loading -> com.localkarar.app.ui.components.LkLoadingState(desen = com.localkarar.app.ui.components.LkLoadingDesen.DETAY)
            is FinancialModelUiState.Error -> LkErrorState(
                message = state.message,
                onRetry = { viewModel.load() }
            )
            is FinancialModelUiState.Content -> {
                val model = state.model
                val runResult = state.runResult
                if (runResult != null) {
                    scenarioRuns[runResult.scenarioName ?: "base"] = runResult
                }

                val inputValues = remember(model.code) { mutableStateMapOf<String, String>() }
                val inputErrors = remember(model.code) { mutableStateMapOf<String, String>() }

                /*
                 * 🔴 BU EKRAN KAYDIRMIYORDU — `Column(verticalScroll)`
                 * ICERIGI KESIYORDU.
                 *
                 * Olculdu (08.09.2026, emulator): `scrollState.maxValue`
                 * yalnizca 732 piksel donuyordu; oysa bes girdi, ipucu
                 * kutusu ve aciklamalarla icerik uc bin pikseli asiyor.
                 * Sonuc: son iki girdi ve "Modeli Çalıştır" dugmesi
                 * ULASILAMIYORDU -- yani ekran fiilen calismiyordu.
                 *
                 * ⚠️ Once sekme satirindaki `fillMaxWidth()` kaldirildi
                 * (yatay kaydirmada sonsuz genislik isteniyordu); o
                 * duzeltme kaydirmayi baslatti ama kesilmeyi bitirmedi.
                 *
                 * Cozum: uygulamanin ZATEN CALISAN deseni. Kayitlar,
                 * Belgeler ve Kayit Detayi ekranlari `LkHeroPage` icinde
                 * `LazyColumn` kullaniyor ve uzun icerikte sorunsuz
                 * kaydiriyor. `LazyColumn` her ogeyi sonsuz yukseklik
                 * kisitiyla olcuyor, dolayisiyla icerik kesilmiyor.
                 *
                 * ⚠️ Ogeler bilerek IRI: sekme icerikleri kendi icinde
                 * bir butun ve `ColumnScope` bekliyor. Amac tembel cizim
                 * degil, DOGRU OLCUM.
                 */
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listeDurumu,
                    contentPadding = PaddingValues(LkSpacing.Space4),
                    verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                ) {
                    // Header
                    item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = LkSpacing.Space2)) {
                                Text(text = "${modelCategoryLabel(model.category)} · v${model.engineVersion ?: "1.0.0"}", style = LkTypography.getMicro(), color = LkTextSecondary)
                                Spacer(modifier = Modifier.height(LkSpacing.Space1))
                                Text(text = model.name, style = LkTypography.getSectionTitle(), color = LkTextPrimary)
                                Text(text = model.purpose, style = LkTypography.getBodySmall(), color = LkTextSecondary)
                            }
                            if (workspaceName != null) {
                                LkChip(text = workspaceName, background = LkPrimary.copy(alpha = 0.15f), contentColor = LkPrimary)
                            }
                        }
                        if (!model.formula.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(LkSpacing.Space3))
                            LkInfoPanel(title = "Formül") {
                                Text(text = model.formula, style = LkTypography.getBody(), color = LkTextPrimary)
                            }
                        }
                    }
                    }

                    item {
                    if (workspaceName == null) {
                        LkInfoPanel(title = "İşletme gerekli", icon = Icons.Outlined.Info) {
                            Text(text = "Bu modeli çalıştırmak için bir işletme seçmeniz gerekir.", style = LkTypography.getBodySmall(), color = LkTextSecondary)
                            Spacer(modifier = Modifier.height(LkSpacing.Space3))
                            LkButton(text = "İşletme Seç", onClick = onOpenWorkspace, modifier = Modifier.fillMaxWidth())
                        }
                    }
                    }

                    item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = LkSpacing.Space2),
                        horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)
                    ) {
                        MODEL_TABS.forEachIndexed { index, tab ->
                            val selected = selectedTab == index
                            Text(
                                text = tab,
                                style = LkTypography.getBodySmall().copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal),
                                color = if (selected) LkPrimary else LkTextSecondary,
                                /*
                                 * 🔴 BURADA `fillMaxWidth()` VARDI ve EKRAN
                                 * HIC KAYDIRMIYORDU.
                                 *
                                 * Bu Row YATAY kaydiriliyor; yatay kaydirmada
                                 * cocuklar SONSUZ genislik kisitiyla olculuyor.
                                 * `fillMaxWidth()` "kisitin tamamini kapla"
                                 * demek, yani sonsuzu istemek. Yedi sekme birden
                                 * bunu isteyince satirin olculen genisligi
                                 * tasiyor ve onu iceren DIKEY kaydirmali Column
                                 * bozuluyordu.
                                 *
                                 * Olculen sonuc (08.09.2026, emulator): 1600
                                 * piksellik kaydirmadan sonra "Model Girdileri"
                                 * basligi TAM AYNI yerde kaliyordu; bes girdinin
                                 * yalnizca ikisine ve "Modeli Çalıştır"
                                 * dugmesine HIC ulasilamiyordu. Yani finansal
                                 * model ekrani mobilde fiilen calismiyordu.
                                 *
                                 * Sekme kendi genisligi kadar yer kaplamali --
                                 * zaten istenen de bu.
                                 */
                                modifier = Modifier
                                    .padding(horizontal = LkSpacing.Space3, vertical = LkSpacing.Space2)
                                    .background(if (selected) LkPrimary.copy(alpha = 0.1f) else LkSurfacePanel, LkShapes.MD)
                                    .border(1.dp, if (selected) LkPrimary else LkLineStrong, LkShapes.MD)
                                    .clickable { selectedTab = index }
                            )
                        }
                    }

                    }

                    item {
                    if (actionError != null) {
                        Text(text = actionError!!, style = LkTypography.getBodySmall(), color = LkDanger)
                        Spacer(modifier = Modifier.height(LkSpacing.Space2))
                    }

                    }

                    item {
                    when (selectedTab) {
                        0 -> WorkbenchTab(model, workspaceName, inputValues, inputErrors, scenarioName, state.isRunning, scenarioRuns, onOpenWorkspace, viewModel, { actionError = it })
                        1 -> InputsTab(model, workspaceName, inputValues, inputErrors, scenarioName, state.isRunning, onOpenWorkspace, viewModel, { actionError = it })
                        2 -> ScenariosTab(model, scenarioName, scenarioRuns, state.isRunning, onOpenWorkspace, viewModel, { actionError = it }, { scenarioName = it })
                        3 -> OutputsTab(model, runResult)
                        4 -> ChecksTab(model, runResult)
                        5 -> SourcesTab(model)
                        6 -> VersionsTab(model)
                    }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkbenchTab(
    model: FinancialModelDto,
    workspaceName: String?,
    inputValues: MutableMap<String, String>,
    inputErrors: MutableMap<String, String>,
    scenarioName: String,
    isRunning: Boolean,
    scenarioRuns: MutableMap<String, FinancialModelRunResponseDto>,
    onOpenWorkspace: () -> Unit,
    viewModel: FinancialModelViewModel,
    setActionError: (String?) -> Unit
) {
    val latestRun = scenarioRuns[scenarioName] ?: scenarioRuns.values.firstOrNull()

    val ipucu by viewModel.ipucu.collectAsState()
    val kaynaklar by viewModel.kaynaklar.collectAsState()

    /* Ipucu bir kez soruluyor; her yeniden cizimde istek atmak gereksiz. */
    LaunchedEffect(model.code) { viewModel.ipucuYukle() }

    /*
     * BELGEDEN ON DOLDURMA (madde 18) — bir kez, ekran acilirken.
     *
     * ⚠️ Kullanicinin YAZDIGI deger EZILMIYOR: yalnizca bos alanlar
     * dolduruluyor. Ekrana donup elle duzelttigi bir rakami belgeden
     * gelenle degistirmek, kullanicinin duzeltmesini yok saymak olurdu.
     */
    LaunchedEffect(model.code) {
        viewModel.belgedenOnDoldur().forEach { (alan, deger) ->
            if (inputValues[alan].isNullOrBlank()) {
                inputValues[alan] = LkFormatting.formatNumber(deger)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        /*
         * PAZARYERI IPUCU — kullanicinin KENDI satis verisi.
         *
         * 🔴 Mobilde HIC YOKTU. Webde model ekraninin ustunde duruyor
         * (`FinancialModelWorkspace.jsx`): son 90 gunun gercek siparis
         * kalemlerinden ortalama fiyat ve komisyon.
         *
         * ⚠️ Kutu YALNIZ doldurulacak alan varsa ciziliyor. Baglantisi
         * olmayan ya da `PRODUCT_PROFITABILITY` disindaki bir modelde
         * "veriniz yok" demek yerine hic gorunmuyor.
         */
        val ipucuAlanlari = viewModel.ipucuDegerleri()
        if (ipucu != null && ipucuAlanlari.isNotEmpty()) {
            PazaryeriIpucuKutusu(
                ipucu = ipucu!!,
                onDoldur = {
                    ipucuAlanlari.forEach { (alan, deger) ->
                        inputValues[alan] = LkFormatting.formatNumber(deger)
                        inputErrors.remove(alan)
                    }
                    viewModel.ipucuKaynagiIsaretle(ipucuAlanlari.keys)
                }
            )
            Spacer(modifier = Modifier.height(LkSpacing.Space3))
        }

        LkSectionHeader(title = "Model Girdileri", subtitle = "${model.inputs.size} alan")
        Spacer(modifier = Modifier.height(LkSpacing.Space2))
        model.inputs.forEach { input ->
            LkNumericField(
                value = inputValues[input.key] ?: "",
                onValueChange = { newValue ->
                    inputValues[input.key] = newValue
                    inputErrors.remove(input.key)
                },
                label = input.label,
                placeholder = if (input.type == "number_array") "Virgülle ayırarak girin" else "Değer girin",
                error = inputErrors[input.key],
                suffix = input.unit.ifBlank { null }
            )

            /*
             * KAYNAK KUNYESI — yalniz degeri kullanici KOYMADIYSA.
             *
             * Kendi yazdigi sayi icin "bu nereden geldi" diye sormak
             * anlamsiz; webdeki girdi basina dort alanlik kunye formu
             * telefonda da yapilabilir bir is degil. Onun yerine:
             * nereden geldigi YAZILIYOR, belgeden geldiyse
             * "doğruladım" isteniyor.
             */
            kaynaklar[input.key]?.let { kaynak ->
                GirdiKaynagiSatiri(
                    kaynak = kaynak,
                    onDogrula = { viewModel.dogrulamayiDegistir(input.key, it) }
                )
            }

            if (input.description.isNotBlank()) {
                Text(text = input.description, style = LkTypography.getMetadata(), color = LkTextMuted, modifier = Modifier.padding(bottom = LkSpacing.Space2))
            }
        }

        Spacer(modifier = Modifier.height(LkSpacing.Space3))
        LkButton(
            text = if (isRunning) "Çalıştırılıyor..." else "Modeli Çalıştır",
            onClick = {
                setActionError(null)
                val inputs = mutableMapOf<String, JsonElement>()
                var valid = true
                model.inputs.forEach { input ->
                    val raw = inputValues[input.key]?.trim().orEmpty()
                    if (raw.isEmpty()) {
                        if (input.required) {
                            inputErrors[input.key] = "Değer girin"
                            valid = false
                        }
                    } else if (input.type == "number_array") {
                        val parts = raw.split(',').map { it.trim() }
                        val values = mutableListOf<JsonPrimitive>()
                        parts.forEach { part ->
                            val parsed = LkFormatting.parseDecimal(part)
                            if (parsed == null) {
                                inputErrors[input.key] = "Geçersiz sayı: $part"
                                valid = false
                            } else {
                                values.add(JsonPrimitive(parsed))
                            }
                        }
                        if (valid && values.isNotEmpty()) {
                            inputs[input.key] = buildJsonArray { values.forEach { add(it) } }
                        }
                    } else {
                        val parsed = LkFormatting.parseDecimal(raw)
                        if (parsed == null) {
                            inputErrors[input.key] = "Geçersiz sayı"
                            valid = false
                        } else {
                            if (input.min != null && parsed < input.min) {
                                inputErrors[input.key] = "En az ${input.min} olmalı"
                                valid = false
                            } else if (input.max != null && parsed > input.max) {
                                inputErrors[input.key] = "En fazla ${input.max} olmalı"
                                valid = false
                            } else {
                                inputs[input.key] = JsonPrimitive(parsed)
                            }
                        }
                    }
                }
                if (valid) {
                    viewModel.run(inputs, scenarioName) { error ->
                        setActionError(error)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(LkSpacing.Space4))
        LkSectionHeader(title = "Son Çalıştırma")
        Spacer(modifier = Modifier.height(LkSpacing.Space2))
        latestRun?.let { run ->
            val outputs = run.outputs.filterKeys { it !in setOf("sensitivity", "checks", "warnings", "trace", "confidence", "ethics", "normalizedInputs") }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LkSurfacePanel, LkShapes.MD)
                    .border(1.dp, LkLineStrong, LkShapes.MD)
                    .padding(LkSpacing.PadPanel)
            ) {
                outputs.entries.forEach { (key, value) ->
                    val definition = model.outputs.find { it.key == key }
                    LkResultRow(
                        label = definition?.label ?: key.replace('_', ' ').replaceFirstChar { it.uppercaseChar() },
                        value = value.displayValue() + if (!definition?.unit.isNullOrBlank() && value.displayValue().isNotBlank()) " ${definition.unit}" else ""
                    )
                }
                Spacer(modifier = Modifier.height(LkSpacing.Space2))
                Text(
                    text = "${SCENARIO_MAP[run.scenarioName ?: "base"] ?: run.scenarioName} · ${run.createdAt?.let { LkDateUtils.formatDateTime(it) } ?: ""}",
                    style = LkTypography.getMicro(),
                    color = LkTextMuted
                )
            }

            /*
             * KARAR GUNLUGU — "bu sonuca dayanarak ne karar verdim".
             *
             * 🔴 Mobilde HIC YOKTU. Model calisiyor, sonuc goruluyor, ama
             * karar hicbir yere yazilamiyordu.
             *
             * ⚠️ YALNIZ CALISMA VARSA ciziliyor: sunucu `modelRunId`
             * istiyor. Calistirilmamis bir modelden karar kaydetmek,
             * dayanaksiz bir kayit uretmek olurdu.
             */
            Spacer(modifier = Modifier.height(LkSpacing.Space3))
            KararKaydetBolumu(viewModel = viewModel)
        } ?: Text(
            text = "Henüz çalışma yok. Girdileri doldurup modeli çalıştırın.",
            style = LkTypography.getBodySmall(),
            color = LkTextMuted
        )
    }
}

@Composable
private fun InputsTab(
    model: FinancialModelDto,
    workspaceName: String?,
    inputValues: MutableMap<String, String>,
    inputErrors: MutableMap<String, String>,
    scenarioName: String,
    isRunning: Boolean,
    onOpenWorkspace: () -> Unit,
    viewModel: FinancialModelViewModel,
    setActionError: (String?) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LkSectionHeader(title = "Girdiler ve Doğrulama", subtitle = "${model.inputs.size} zorunlu alan")
        model.inputs.forEach { input ->
            LkInfoPanel(title = input.label) {
                Text(text = input.description, style = LkTypography.getBodySmall(), color = LkTextSecondary)
                Spacer(modifier = Modifier.height(LkSpacing.Space2))
                LkNumericField(
                    value = inputValues[input.key] ?: "",
                    onValueChange = { newValue ->
                        inputValues[input.key] = newValue
                        inputErrors.remove(input.key)
                    },
                    label = input.label,
                    placeholder = if (input.type == "number_array") "Virgülle ayırarak girin" else "Değer girin",
                    error = inputErrors[input.key],
                    suffix = input.unit.ifBlank { null }
                )
                if (input.sourceRequired == true) {
                    Spacer(modifier = Modifier.height(LkSpacing.Space2))
                    Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                        LkChip(text = "Kaynak gerekli", background = LkWarning.copy(alpha = 0.15f), contentColor = LkWarning)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(LkSpacing.Space3))
        LkButton(
            text = if (isRunning) "Çalıştırılıyor..." else "Modeli Çalıştır",
            onClick = {
                setActionError(null)
                val inputs = mutableMapOf<String, JsonElement>()
                var valid = true
                model.inputs.forEach { input ->
                    val raw = inputValues[input.key]?.trim().orEmpty()
                    if (raw.isEmpty()) {
                        if (input.required) {
                            inputErrors[input.key] = "Değer girin"
                            valid = false
                        }
                    } else if (input.type == "number_array") {
                        val parts = raw.split(',').map { it.trim() }
                        val values = mutableListOf<JsonPrimitive>()
                        parts.forEach { part ->
                            val parsed = LkFormatting.parseDecimal(part)
                            if (parsed == null) {
                                inputErrors[input.key] = "Geçersiz sayı: $part"
                                valid = false
                            } else {
                                values.add(JsonPrimitive(parsed))
                            }
                        }
                        if (valid && values.isNotEmpty()) {
                            inputs[input.key] = buildJsonArray { values.forEach { add(it) } }
                        }
                    } else {
                        val parsed = LkFormatting.parseDecimal(raw)
                        if (parsed == null) {
                            inputErrors[input.key] = "Geçersiz sayı"
                            valid = false
                        } else {
                            if (input.min != null && parsed < input.min) {
                                inputErrors[input.key] = "En az ${input.min} olmalı"
                                valid = false
                            } else if (input.max != null && parsed > input.max) {
                                inputErrors[input.key] = "En fazla ${input.max} olmalı"
                                valid = false
                            } else {
                                inputs[input.key] = JsonPrimitive(parsed)
                            }
                        }
                    }
                }
                if (valid && inputs.isNotEmpty() && workspaceName != null) {
                    viewModel.run(inputs, scenarioName) { message ->
                        setActionError(message)
                    }
                } else if (workspaceName == null) {
                    setActionError("Finansal model çalıştırmak için önce bir işletme seçin.")
                }
            },
            enabled = !isRunning && workspaceName != null,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ScenariosTab(
    model: FinancialModelDto,
    scenarioName: String,
    scenarioRuns: MutableMap<String, FinancialModelRunResponseDto>,
    isRunning: Boolean,
    onOpenWorkspace: () -> Unit,
    viewModel: FinancialModelViewModel,
    setActionError: (String?) -> Unit,
    onScenarioSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LkSectionHeader(title = "Senaryo Laboratuvarı", subtitle = "Aynı modelin farklı varsayımlarını ayrı çalışma olarak kaydedin")

        // Scenario selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)
        ) {
            SCENARIOS.forEach { (id, label) ->
                val count = if (scenarioRuns[id] != null) 1 else 0
                val selected = scenarioName == id
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(LkSpacing.Space2)
                        .background(if (selected) LkPrimary.copy(alpha = 0.1f) else LkSurfacePanel, LkShapes.MD)
                        .border(1.dp, if (selected) LkPrimary else LkLineStrong, LkShapes.MD)
                        .padding(LkSpacing.Space3)
                        .clickable { onScenarioSelected(id) }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = label, style = LkTypography.getBodySmall(), color = if (selected) LkPrimary else LkTextPrimary)
                        Text(text = "$count kayıt", style = LkTypography.getMicro(), color = LkTextMuted)
                    }
                }
            }
        }

        // Comparison
        if (scenarioRuns.isNotEmpty()) {
            Spacer(modifier = Modifier.height(LkSpacing.Space3))
            LkSectionHeader(title = "Senaryo Karşılaştırması")
            scenarioRuns.entries.forEach { (scenario, run) ->
                val label = SCENARIO_MAP[scenario] ?: scenario
                val outputs = run.outputs.filterKeys { it !in setOf("sensitivity", "checks", "warnings", "trace", "confidence", "ethics", "normalizedInputs") }
                Box(modifier = Modifier.fillMaxWidth().padding(LkSpacing.Space3).background(LkSurfacePanel, LkShapes.MD)) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = label, style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                            Text(text = run.createdAt?.let { LkDateUtils.formatDateTime(it) } ?: "", style = LkTypography.getMicro(), color = LkTextMuted)
                        }
                        outputs.entries.take(3).forEach { (key, value) ->
                            val definition = model.outputs.find { it.key == key }
                            Spacer(modifier = Modifier.height(LkSpacing.Space1))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = definition?.label ?: key.replace('_', ' ').replaceFirstChar { it.uppercaseChar() }, style = LkTypography.getBodySmall(), color = LkTextSecondary)
                                Text(text = value.displayValue() + if (!definition?.unit.isNullOrBlank()) " ${definition.unit}" else "", style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                            }
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(LkSpacing.Space3))
            Text(text = "Karşılaştırma için henüz çalışma yok.", style = LkTypography.getBodySmall(), color = LkTextMuted)
        }

        Spacer(modifier = Modifier.height(LkSpacing.Space3))
        LkButton(
            text = "Seçili senaryo girdilerini hazırla",
            onClick = { /* Could switch to Inputs tab */ },
            variant = LkButtonVariant.SECONDARY,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun OutputsTab(model: FinancialModelDto, runResult: FinancialModelRunResponseDto?) {
    runResult?.let { run ->
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "OUTPUT DASHBOARD", style = LkTypography.getMicro(), color = LkTextSecondary)
                        Text(text = model.name, style = LkTypography.getSectionTitle(), color = LkTextPrimary)
                        Text(text = "${SCENARIO_MAP[run.scenarioName ?: "base"] ?: run.scenarioName} senaryo · ${run.createdAt?.let { LkDateUtils.formatDateTime(it) } ?: ""}", style = LkTypography.getBodySmall(), color = LkTextSecondary)
                    }
                    run.confidence?.let { confidence ->
                        Column(horizontalAlignment = Alignment.End) {
                            LkChip(
                                text = "${LkFormatting.formatNumber(confidence.score * 100)}% (${CONFIDENCE_LABELS[confidence.label] ?: confidence.label})",
                                background = when (confidence.label) {
                                    "high" -> LkSuccess.copy(alpha = 0.15f)
                                    "medium" -> LkWarning.copy(alpha = 0.15f)
                                    else -> LkDanger.copy(alpha = 0.15f)
                                },
                                contentColor = when (confidence.label) {
                                    "high" -> LkSuccess
                                    "medium" -> LkWarning
                                    else -> LkDanger
                                }
                            )
                        }
                    }
                }
            }

            // Metrics
            val outputs = run.outputs.filterKeys { it !in setOf("sensitivity", "checks", "warnings", "trace", "confidence", "ethics", "normalizedInputs") }
            val outputEntries = outputs.entries.toList()
            Column(modifier = Modifier.fillMaxWidth()) {
                outputEntries.forEach { (key, value) ->
                    val definition = model.outputs.find { it.key == key }
                    LkResultRow(
                        label = definition?.label ?: key.replace('_', ' ').replaceFirstChar { it.uppercaseChar() },
                        value = value.displayValue() + if (!definition?.unit.isNullOrBlank() && value.displayValue().isNotBlank()) " ${definition.unit}" else ""
                    )
                }
            }

            // Sensitivity
            run.outputs["sensitivity"]?.let { sensitivity ->
                val sensObj = sensitivity as? JsonObject
                sensObj?.let {
                    Spacer(modifier = Modifier.height(LkSpacing.Space4))
                    LkSectionHeader(title = "Hassasiyet Görünümü")
                    it.entries.forEach { (name, values) ->
                        val valObj = values as? JsonObject
                        valObj?.let {
                            LkInfoPanel(title = name) {
                                it.entries.forEach { (key, value) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = key.replace('_', ' ').replaceFirstChar { it.uppercaseChar() }, style = LkTypography.getBodySmall(), color = LkTextSecondary)
                                        Text(text = value.displayValue(), style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } ?: Column(modifier = Modifier.fillMaxWidth()) {
        LkInfoPanel(title = "Sonuç", icon = Icons.Outlined.Science) {
            Text(text = "Henüz sonuç yok", style = LkTypography.getBodyStrong(), color = LkTextPrimary)
            Text(text = "Girdileri hazırlayıp modeli çalıştırın.", style = LkTypography.getBodySmall(), color = LkTextSecondary)
        }
    }
}

@Composable
private fun ChecksTab(model: FinancialModelDto, runResult: FinancialModelRunResponseDto?) {
    runResult?.let { run ->
        Column(modifier = Modifier.fillMaxWidth()) {
            // Combined validation + ethics checks (matching Web)
            val allChecks = (run.checks + run.ethics)
            if (allChecks.isNotEmpty()) {
                LkSectionHeader(title = "Doğrulama ve etik kontrolleri")
                allChecks.forEach { check ->
                    LkValidationCheckRowPublic(check)
                    Spacer(modifier = Modifier.height(LkSpacing.Space2))
                }
            }

            // Trace
            if (run.trace.isNotEmpty()) {
                Spacer(modifier = Modifier.height(LkSpacing.Space4))
                LkSectionHeader(title = "Hesap İzı")
                var traceExpanded by remember { mutableStateOf(false) }
                val steps = if (traceExpanded) run.trace else run.trace.take(3)
                steps.forEach { step ->
                    LkCalculationStepRowPublic(step)
                    Spacer(modifier = Modifier.height(LkSpacing.Space2))
                }
                if (run.trace.size > 3) {
                    LkButton(
                        text = if (traceExpanded) "Daha Az Göster" else "Tüm Adımları Göster (${run.trace.size})",
                        onClick = { traceExpanded = !traceExpanded },
                        variant = LkButtonVariant.QUIET,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Confidence components
            run.confidence?.let { confidence ->
                Spacer(modifier = Modifier.height(LkSpacing.Space4))
                LkSectionHeader(title = "Veri Güven Bileşenleri")
                confidence.components.forEach { component ->
                    Column(modifier = Modifier.fillMaxWidth().padding(LkSpacing.Space2)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = component.label, style = LkTypography.getBodySmall(), color = LkTextPrimary)
                            Text(text = "${component.score}/100", style = LkTypography.getBodyStrong(), color = LkPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        /*
                         * Renk Canvas'in DISINDA hesaplaniyor: `onDraw` bir
                         * DrawScope lambdasi, composable degil. Tema tokenlari
                         * artik @Composable oldugu icin orada okunamaz.
                         */
                        val barColor = when {
                            component.score > 60 -> LkSuccess
                            component.score > 30 -> LkWarning
                            else -> LkDanger
                        }
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            onDraw = {
                                val barWidth = (component.score / 100.0) * size.width
                                drawRect(
                                    color = barColor,
                                    topLeft = Offset.Zero,
                                    size = Size(barWidth.toFloat(), size.height)
                                )
                            }
                        )
                        Text(text = component.reason, style = LkTypography.getMicro(), color = LkTextMuted)
                    }
                }
                if (confidence.disclaimer.isNotBlank()) {
                    Spacer(modifier = Modifier.height(LkSpacing.Space2))
                    Text(text = confidence.disclaimer, style = LkTypography.getMetadata(), color = LkTextMuted)
                }
            }

            // Warnings + Limitations
            if (run.warnings.isNotEmpty() || model.limitations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(LkSpacing.Space4))
                LkSectionHeader(title = "Uyarılar ve Sınırlamalar")
                (run.warnings + model.limitations).forEach { item ->
                    Text(text = "• $item", style = LkTypography.getBodySmall(), color = LkWarning, modifier = Modifier.fillMaxWidth().padding(bottom = LkSpacing.Space1))
                }
            }
        }
    } ?: Column(modifier = Modifier.fillMaxWidth()) {
        LkInfoPanel(title = "Kontroller", icon = Icons.Outlined.Science) {
            Text(text = "Kontrolleri görmek için modeli çalıştırın.", style = LkTypography.getBodySmall(), color = LkTextSecondary)
        }
    }
}

@Composable
private fun SourcesTab(model: FinancialModelDto) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LkSectionHeader(title = "Metodolojik Kaynaklar")
        model.sources.forEach { source ->
            Column(modifier = Modifier.fillMaxWidth().padding(LkSpacing.Space3).background(LkSurfacePanel, LkShapes.MD)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        LkChip(
                            text = source.authority.uppercase(),
                            background = when (source.authority) {
                                "official" -> LkDanger.copy(alpha = 0.15f)
                                "academic" -> LkPrimary.copy(alpha = 0.15f)
                                "professional" -> LkSuccess.copy(alpha = 0.15f)
                                else -> LkSurfaceRaised
                            },
                            contentColor = when (source.authority) {
                                "official" -> LkDanger
                                "academic" -> LkPrimary
                                "professional" -> LkSuccess
                                else -> LkTextSecondary
                            }
                        )
                        Text(text = source.title, style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                        Text(text = source.usage, style = LkTypography.getBodySmall(), color = LkTextSecondary)
                    }
                    Icon(imageVector = Icons.Outlined.ArrowForward, contentDescription = null, tint = LkTextMuted, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(LkSpacing.Space2))
        }
        Text(text = "Kaynaklar yöntem içindir. LocalKarar kaynak metinleri, ücretli şablonları veya telifli soruları kopyalamaz.", style = LkTypography.getMetadata(), color = LkTextMuted, modifier = Modifier.fillMaxWidth().padding(top = LkSpacing.Space3))
    }
}

@Composable
private fun VersionsTab(model: FinancialModelDto) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LkSectionHeader(title = "Model Değişiklik Günlüğü")
        if (model.versions.isEmpty()) {
            Text(text = "Henüz sürüm geçmişi yok.", style = LkTypography.getBodySmall(), color = LkTextMuted)
        } else {
            model.versions.forEach { version ->
                Column(modifier = Modifier.fillMaxWidth().padding(LkSpacing.Space3).background(LkSurfacePanel, LkShapes.MD)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "v${version.version}", style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                            Text(text = version.changeSummary, style = LkTypography.getBodySmall(), color = LkTextSecondary)
                        }
                        Text(text = version.createdAt?.let { LkDateUtils.formatDateTime(it) } ?: "", style = LkTypography.getMicro(), color = LkTextMuted)
                    }
                }
                Spacer(modifier = Modifier.height(LkSpacing.Space2))
            }
        }
    }
}

@Composable
fun LkValidationCheckRowPublic(check: ValidationCheckDto) {
    val (icon, color) = when {
        check.passed -> Icons.Outlined.CheckCircle to LkSuccess
        check.severity == "error" -> Icons.Outlined.Error to LkDanger
        check.severity == "warning" -> Icons.Outlined.Warning to LkWarning
        else -> Icons.Outlined.Info to LkTextSecondary
    }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = check.label.ifBlank { check.code }, style = LkTypography.getBodySmall(), color = LkTextPrimary)
            if (check.detail.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = check.detail, style = LkTypography.getMetadata(), color = LkTextSecondary)
            }
        }
    }
}

@Composable
fun LkCalculationStepRowPublic(step: CalculationStepDto) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = step.label.ifBlank { step.key }, style = LkTypography.getBodySmall(), color = LkTextPrimary)
        if (step.formula.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = step.formula, style = LkTypography.getMicro(), color = LkTextMuted)
        }
        if (step.result != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "= ${step.result.displayValue()}", style = LkTypography.getBodyStrong(), color = LkPrimary)
        }
    }
}

/**
 * PAZARYERI IPUCU KUTUSU.
 *
 * Kullanicinin kendi satis verisinden ortalama fiyat ve komisyon.
 *
 * ⚠️ SAYILAR SUNUCUDAN; hicbiri burada hesaplanmiyor ya da tahmin
 * edilmiyor. Komisyon orani gelmemisse sunucunun kendi notu yaziliyor
 * ("Komisyon orani bu baglantida saglanmadi") -- sifir varsaymak
 * komisyonu yok saymak olurdu.
 */
@Composable
private fun PazaryeriIpucuKutusu(
    ipucu: com.localkarar.app.network.dto.HesaplamaIpucuDto,
    onDoldur: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LkShapes.SM)
            .background(LkPrimary.copy(alpha = 0.08f))
            .padding(LkSpacing.Space3)
    ) {
        Text(
            text = "PAZARYERİ VERİNİZ",
            style = LkTypography.getMicro(),
            color = LkPrimary
        )
        Spacer(Modifier.height(LkSpacing.Space1))

        val fiyat = ipucu.avgUnitPrice
        Text(
            text = buildString {
                append("Son 90 günde ${ipucu.sampleSize} sipariş kaleminde ")
                append("ortalama satış fiyatı ")
                append(LkFormatting.formatMoney(fiyat, ipucu.currency))
                append(".")
                ipucu.avgCommissionPercent?.let {
                    append(" Ortalama komisyon %${LkFormatting.formatNumber(it)}.")
                }
            },
            style = LkTypography.getMetadata(),
            color = LkTextSecondary
        )

        /* Sunucunun kendi notu — komisyon yoksa ne yapilacagini soyluyor. */
        if (!ipucu.note.isNullOrBlank()) {
            Spacer(Modifier.height(LkSpacing.Space1))
            Text(ipucu.note, style = LkTypography.getMicro(), color = LkTextMuted)
        }

        Spacer(Modifier.height(LkSpacing.Space2))
        LkButton(
            text = "Bu değerlerle doldur",
            variant = LkButtonVariant.SECONDARY,
            onClick = onDoldur,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Bir girdinin nereden geldigi + belgeden geldiyse dogrulama.
 *
 * ⚠️ BELGEDEN GELEN DEGER DOGRULANMADAN MODEL CALISMIYOR: sunucu
 * reddediyor ("OCR belge verileri modelde kullanilmadan once kullanici
 * tarafindan dogrulanmalidir"). Bu kutu o kapinin arayuz tarafi --
 * kullaniciyi zorlamak icin degil, okunan rakamin yanlis olabilecegini
 * ona soylemek icin.
 */
@Composable
private fun GirdiKaynagiSatiri(
    kaynak: com.localkarar.app.calculations.GirdiKaynagi,
    onDogrula: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = LkSpacing.Space2)) {
        Text(
            text = kaynak.referans,
            style = LkTypography.getMicro(),
            color = LkTextMuted
        )
        /* Dogrulama kutusu YALNIZ belgeden gelen degerlerde: pazaryeri
           verisi sunucunun kendi kaydindan geliyor, okunmus bir metin
           degil. */
        if (kaynak.tur == "document") {
            Spacer(Modifier.height(LkSpacing.Space1))
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material.Checkbox(
                    checked = kaynak.dogrulandi == true,
                    onCheckedChange = onDogrula
                )
                Text(
                    text = "Bu değeri belgeyle karşılaştırdım",
                    style = LkTypography.getMetadata(),
                    color = LkTextSecondary
                )
            }
        }
    }
}

/**
 * KARAR GUNLUGU KAYDI.
 *
 * Model calismasinin altinda: "bu sonuca dayanarak ne karar verdim ve
 * ne bekliyorum". Web ile ayni iki alan.
 *
 * ⚠️ Form KAPALI basliyor. Her calistirmadan sonra iki bos kutu
 * acmak, kullaniciya doldurmasi gereken bir odev gibi gorunurdu; karar
 * kaydetmek istege bagli.
 */
@Composable
private fun KararKaydetBolumu(viewModel: FinancialModelViewModel) {
    var acik by remember { mutableStateOf(false) }
    var karar by remember { mutableStateOf("") }
    var beklenen by remember { mutableStateOf("") }
    val kaydediliyor by viewModel.kararKaydediliyor.collectAsState()

    if (!acik) {
        LkButton(
            text = "Bu sonuca göre karar kaydet",
            variant = LkButtonVariant.SECONDARY,
            onClick = { acik = true },
            modifier = Modifier.fillMaxWidth()
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)) {
        Text("KARAR GÜNLÜĞÜ", style = LkTypography.getMicro(), color = LkTextMuted)
        LkTextField(
            value = karar,
            onValueChange = { karar = it },
            label = "Ne karar verdin?"
        )
        LkTextField(
            value = beklenen,
            onValueChange = { beklenen = it },
            label = "Ne olmasını bekliyorsun?"
        )
        Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
            LkButton(
                text = if (kaydediliyor) "Kaydediliyor..." else "Kaydet",
                onClick = {
                    viewModel.kararKaydet(karar, beklenen) {
                        acik = false; karar = ""; beklenen = ""
                    }
                },
                /* Sunucu ikisi icin de en az uc karakter istiyor; kapiyi
                   burada da tutmak, dolduramadigi bir formu gonderip
                   hata almasini onluyor. */
                enabled = !kaydediliyor && karar.trim().length >= 3 && beklenen.trim().length >= 3,
                modifier = Modifier.weight(1f)
            )
            LkButton(
                text = "Vazgeç",
                variant = LkButtonVariant.SECONDARY,
                onClick = { acik = false },
                enabled = !kaydediliyor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

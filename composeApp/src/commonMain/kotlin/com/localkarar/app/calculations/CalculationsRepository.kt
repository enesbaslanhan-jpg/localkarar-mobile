package com.localkarar.app.calculations

import com.localkarar.app.network.ApiConfig
import com.localkarar.app.network.SafeApiClient
import com.localkarar.app.network.dto.FinancialModelDto
import com.localkarar.app.network.dto.FinancialModelListResponseDto
import com.localkarar.app.network.dto.FinancialModelRunDetailDto
import com.localkarar.app.network.dto.FinancialModelRunListItemDto
import com.localkarar.app.network.dto.FinancialModelRunListResponseDto
import com.localkarar.app.network.dto.FinancialModelRunResponseDto
import com.localkarar.app.network.dto.HesaplamaIpucuDto
import com.localkarar.app.network.dto.KararGunluguDto
import com.localkarar.app.network.dto.KararGunluguListesiDto
import com.localkarar.app.network.dto.KararGunluguIstegiDto
import com.localkarar.app.network.dto.FormulaCalculateRequestDto
import com.localkarar.app.network.dto.FormulaCalculateResponseDto
import com.localkarar.app.network.dto.FormulaCalculationDto
import com.localkarar.app.network.dto.FormulaDto
import com.localkarar.app.network.dto.ModelRunRequestDto

class CalculationsRepository(private val api: SafeApiClient) {

    private val base = ApiConfig.baseUrl

    suspend fun getFormulas(): Result<List<FormulaDto>> {
        return api.get("$base/formulas")
    }

    suspend fun calculateFormula(formulaId: String, inputs: Map<String, Double>): Result<FormulaCalculateResponseDto> {
        return api.post("$base/formulas/$formulaId/calculate", FormulaCalculateRequestDto(inputs))
    }

    suspend fun getFormulaHistory(): Result<List<FormulaCalculationDto>> {
        return api.get("$base/formula-calculations")
    }

    suspend fun getModels(): Result<FinancialModelListResponseDto> {
        return api.get("$base/financial-models")
    }

    suspend fun getModel(code: String): Result<FinancialModelDto> {
        return api.get("$base/financial-models/$code")
    }

    /*
     * PAZARYERI HESAPLAMA IPUCU.
     *
     * 🔴 Mobilde HIC YOKTU. Webde model ekraninin ustunde "son 90 gunde
     * N siparis kaleminde ortalama fiyat X" diyen bir kutu ve "Bu
     * degerlerle doldur" dugmesi var (`FinancialModelWorkspace.jsx`).
     * Kullanicinin kendi satis verisini hesaba tasimanin en kisa yolu bu.
     */
    suspend fun getCalculationHints(workspaceId: String): Result<HesaplamaIpucuDto> {
        return api.get("$base/marketplace/calculation-hints?workspaceId=$workspaceId")
    }

    /*
     * KARAR GUNLUGU — model calismasindan karar kaydi.
     *
     * 🔴 Mobilde HIC YOKTU. Model calisiyor, sonuc goruluyor, ama "bu
     * sonuca dayanarak ne karar verdim" hicbir yere yazilamiyordu.
     */
    suspend fun kararKaydet(
        workspaceId: String,
        istek: KararGunluguIstegiDto
    ): Result<KararGunluguDto> {
        return api.post("$base/workspaces/$workspaceId/decision-journal", istek)
    }

    /*
     * KARAR GUNLUGUNU GERI OKUMA.
     *
     * 🔴 Yalniz yazma vardi. Kullanici kararini ve sonucunu giriyor,
     * o veriyi bir daha hicbir yerde goremiyordu -- sunucuda okuma
     * ucu bile yoktu.
     */
    suspend fun kararGunlugu(workspaceId: String): Result<KararGunluguListesiDto> {
        return api.get("$base/workspaces/$workspaceId/decision-journal")
    }

    suspend fun runModel(workspaceId: String, code: String, request: ModelRunRequestDto): Result<FinancialModelRunResponseDto> {
        return api.post("$base/workspaces/$workspaceId/financial-models/$code/runs", request)
    }

    suspend fun getModelRuns(workspaceId: String, modelCode: String? = null): Result<FinancialModelRunListResponseDto> {
        val suffix = if (modelCode != null) "?modelCode=${modelCode.uppercase()}" else ""
        return api.get("$base/workspaces/$workspaceId/financial-model-runs$suffix")
    }

    suspend fun getRunDetail(workspaceId: String, runId: String): Result<FinancialModelRunDetailDto> {
        return api.get("$base/workspaces/$workspaceId/financial-model-runs/$runId")
    }
}
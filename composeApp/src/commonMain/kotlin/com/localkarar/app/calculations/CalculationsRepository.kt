package com.localkarar.app.calculations

import com.localkarar.app.network.ApiConfig
import com.localkarar.app.network.SafeApiClient
import com.localkarar.app.network.dto.FinancialModelDto
import com.localkarar.app.network.dto.FinancialModelListResponseDto
import com.localkarar.app.network.dto.FinancialModelRunDetailDto
import com.localkarar.app.network.dto.FinancialModelRunListItemDto
import com.localkarar.app.network.dto.FinancialModelRunListResponseDto
import com.localkarar.app.network.dto.FinancialModelRunResponseDto
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
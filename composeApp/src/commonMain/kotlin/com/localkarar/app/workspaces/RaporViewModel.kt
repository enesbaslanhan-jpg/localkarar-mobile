package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.TrackerReportDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/*
 * RAPOR — donem raporu (Faz 2, 15.09.2026).
 *
 * Ekran hesap yapmaz: `tracker/report` ucu (sunucu tracker-periods.ts)
 * satirlari ve toplamlari hazir verir. Web Rapor sayfasi ve Genel Bakis
 * ayni tanimlari okur; mobilde farkli bir sayi cikmasi imkansiz olsun.
 */
sealed interface RaporUiState {
    data object Loading : RaporUiState
    data class Error(val message: String) : RaporUiState
    data class Content(val rapor: TrackerReportDto) : RaporUiState
}

/** Donem secimi: bugun / bu hafta (Pzt-Paz) / bu ay — Istanbul takvimi. */
enum class RaporDonemi(val key: String, val etiket: String) {
    BUGUN("today", "Bugün"),
    HAFTA("week", "Bu hafta"),
    AY("month", "Bu ay")
}

class RaporViewModel(
    private val workspaceId: String,
    private val repository: WorkspaceRepository
) : ViewModel() {
    private val _donem = MutableStateFlow(RaporDonemi.AY)
    val donem: StateFlow<RaporDonemi> = _donem.asStateFlow()

    private val _uiState = MutableStateFlow<RaporUiState>(RaporUiState.Loading)
    val uiState: StateFlow<RaporUiState> = _uiState.asStateFlow()

    init { load() }

    fun donemSec(d: RaporDonemi) {
        if (_donem.value == d) return
        _donem.value = d
        load()
    }

    fun load() {
        _uiState.value = RaporUiState.Loading
        viewModelScope.launch {
            repository.getTrackerReport(workspaceId, _donem.value.key)
                .onSuccess { _uiState.value = RaporUiState.Content(it) }
                .onFailure { _uiState.value = RaporUiState.Error(it.message ?: "Rapor yüklenemedi.") }
        }
    }
}

package com.localkarar.app.ui.screens.decision

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.decision.DecisionSessionUiState
import com.localkarar.app.decision.DecisionSessionViewModel
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingDesen
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.components.decision.LkDecisionInput
import com.localkarar.app.ui.components.decision.LkDecisionResultPanel
import com.localkarar.app.ui.components.decision.LkKararMakbuzu
import com.localkarar.app.ui.components.decision.LkKararTakibi
import com.localkarar.app.ui.theme.LkSpacing
import com.localkarar.app.ui.theme.*
import kotlinx.serialization.json.JsonElement

@Composable
fun DecisionSessionScreen(
    viewModel: DecisionSessionViewModel,
    /** Makbuzdaki "Mentora sor" -- baglam metniyle mentor ekranina gider. */
    onMentoraSor: (String) -> Unit = {},
    /**
     * Karar takibi. `null` ise bolum "once isletme secin" diyor --
     * takip bir isletmenin gorevi olarak aciliyor.
     */
    followUpViewModel: com.localkarar.app.decision.DecisionFollowUpViewModel? = null,
    isletmeSecili: Boolean = false,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var actionError by remember { mutableStateOf<String?>(null) }

    LkHeroPage(
        title = (uiState as? DecisionSessionUiState.Content)?.session?.decisionCheckTitle ?: "Karar Aracı",
        onBack = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (actionError != null) {
                Text(
                    text = actionError!!,
                    color = LkDanger,
                    style = LkTypography.getBodySmall(),
                    modifier = Modifier.padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space2)
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when (val state = uiState) {
                    is DecisionSessionUiState.Loading -> LkLoadingState(desen = LkLoadingDesen.DETAY)
                    is DecisionSessionUiState.Error -> LkErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadSession() }
                    )
                    is DecisionSessionUiState.Content -> {
                        val session = state.session
                        var step by androidx.compose.runtime.saveable.rememberSaveable(session.id) { mutableStateOf(0) }
                        val questions = session.definition
                        val currentStep = step.coerceIn(0, (questions.size - 1).coerceAtLeast(0))
                        /** Secenekli soru varsa adim adim; yoksa tek form. */
                        val adimAdim = questions.any { it.type == "choice" }
                        LaunchedEffect(state.errors) {
                            val invalid = questions.indexOfFirst { it.code in state.errors }
                            if (invalid >= 0) step = invalid
                        }
                        
                        Column(Modifier.fillMaxSize().imePadding()) {
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(LkSpacing.Space4),
                            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                        ) {
                            if (state.result != null) {
                                /*
                                 * KARAR MAKBUZU — ayrintili panelin
                                 * USTUNDE. Webde de fis sonucun yaninda
                                 * durup onun yerine gecmiyor: makbuz
                                 * "ne karar verdim"i, panel "nasil
                                 * hesaplandi"yi anlatiyor. Once karar,
                                 * sonra hesap.
                                 */
                                item {
                                    LkKararMakbuzu(
                                        snapshot = state.result.snapshot,
                                        baslik = session.decisionCheckTitle,
                                        tamamlanmaZamani = null,
                                        onMentoraSor = onMentoraSor
                                    )
                                }
                                item {
                                    LkKararTakibi(
                                        viewModel = followUpViewModel,
                                        kararBasligi = session.decisionCheckTitle,
                                        isletmeSecili = isletmeSecili
                                    )
                                }
                                item {
                                    LkDecisionResultPanel(
                                        snapshot = state.result.snapshot,
                                        toolCode = session.decisionCheckCode,
                                        onRestart = {
                                            actionError = null
                                            viewModel.restartSession(session.decisionCheckCode, onError = { actionError = it })
                                        },
                                        onListClick = onBack
                                    )
                                }
                            } else {
                                /*
                                 * 🔴 HER SORU AYRI EKRANDAYDI VE EKRAN BOS DURUYORDU.
                                 *
                                 * Mockup'in "tek soru, tek ekran" kurali SECENEKLI
                                 * sorular icin: uc secenek, her biri dolu zeminli
                                 * bir kart, ekran doluyor ve karar tek tek
                                 * agirliklandiriliyor. Sayisal girdilerde ayni
                                 * desen dort ekran boyunca tek bir kutu gosteriyor;
                                 * geri kalan yer bos kaliyor ve kullanici sekiz
                                 * rakami girmek icin sekiz kez "Devam" diyor.
                                 *
                                 * Kural: secenekli soru VARSA adim adim (mockup
                                 * "Karar 2"), yoksa hepsi TEK formda. Karar tipini
                                 * ekran degil sorunun kendi turu belirliyor.
                                 */
                                if (adimAdim) {
                                    item {
                                        Text("Adım ${if (questions.isEmpty()) 0 else currentStep + 1} / ${questions.size}",
                                            style = LkTypography.getBodyStrong(), color = LkPrimary)
                                    }
                                }
                                val gorunenler =
                                    if (adimAdim) questions.drop(currentStep).take(1) else questions
                                items(gorunenler, key = { it.code }) { question ->
                                    val currentAnswer = session.answers.find { it.questionCode == question.code }
                                    LkDecisionInput(
                                        question = question,
                                        value = currentAnswer?.valueJson,
                                        isUnknown = currentAnswer?.isUnknown == true,
                                        error = state.errors[question.code],
                                        onValueChange = { newValue ->
                                            viewModel.updateAnswer(question.code, newValue, currentAnswer?.isUnknown == true)
                                        },
                                        onUnknownChange = { unknown ->
                                            viewModel.updateAnswer(question.code, currentAnswer?.valueJson, unknown)
                                        }
                                    )
                                }
                            }
                        }
                        if (state.result == null && questions.isNotEmpty()) {
                            Row(
                                Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                    if (adimAdim && currentStep > 0) {
                                        LkButton(
                                            text = "Önceki",
                                            onClick = { step -= 1 },
                                            variant = com.localkarar.app.ui.components.LkButtonVariant.SECONDARY,
                                            enabled = !state.isSubmitting,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    LkButton(
                                        text = if (adimAdim && currentStep < questions.lastIndex) "Devam et"
                                            else "Sonucu hesapla",
                                        onClick = {
                                            actionError = null
                                            if (adimAdim && currentStep < questions.lastIndex) step += 1
                                            else viewModel.completeSession(onError = { actionError = it })
                                        },
                                        enabled = !state.isSubmitting,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                        }
                        }
                    }
                }
            }
        }
    }
}

package com.localkarar.app.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.localkarar.app.network.dto.KurulumProfiliDto
import com.localkarar.app.onboarding.KurulumUiState
import com.localkarar.app.onboarding.OnboardingViewModel
import com.localkarar.app.ui.components.*
import com.localkarar.app.ui.theme.*

/*
 * Sunucu enum'lariyla BIREBIR. Etiketler webdeki karsiliklariyla ayni
 * anlamda; degerler sunucunun kabul ettigi dizeler
 * (`onboarding.ts` -> BUSINESS_STAGES / SALES_CHANNELS / CHALLENGES).
 *
 * ⚠️ Buraya listede olmayan bir deger yazmak 422 doner; enum sunucuda.
 */
private val ASAMALAR = listOf(
    "startup" to "Yeni kurulmuş",
    "growth" to "Büyüyor",
    "mature" to "Oturmuş"
)

private val KANALLAR = listOf(
    "retail_store" to "Dükkân",
    "ecommerce" to "Kendi sitem",
    "marketplace" to "Pazaryeri",
    "wholesale" to "Toptan",
    "export" to "İhracat",
    "service" to "Hizmet",
    "other" to "Diğer"
)

private val ZORLUKLAR = listOf(
    "cash_flow" to "Nakit akışı",
    "customer_acquisition" to "Müşteri bulma",
    "cost_control" to "Maliyet kontrolü",
    "competition" to "Rekabet",
    "employee_finding" to "Eleman bulma",
    "digital_skills" to "Dijital beceriler",
    "technology_adoption" to "Teknolojiye uyum",
    "regulation" to "Mevzuat",
    "other" to "Diğer"
)

/**
 * ISLETME KURULUMU — mobilde ilk kez.
 *
 * 🔴 Web `/app/onboarding` sunuyor, mobilde karsiligi YOKTU. Mobilden
 * kaydolan kullanici isletme profilini hic kuramiyordu; mentor ve
 * hesaplamalar o profile dayandigi icin deneyim yarim kaliyordu.
 *
 * ⚠️ WEBDEKI DORT ADIM UCE INDI: isletme / kanallar / hedefler. Webdeki
 * dorduncu "özet" adimi telefonda yalniz bir kaydirma daha demek --
 * girilen degerler zaten formda gorunuyor. HICBIR ALAN ATILMADI.
 *
 * ⚠️ ATLANABILIR. Web de zorunlu tutmuyor: kurulum bir kolaylik, kapi
 * degil. Atlayan kullanici uygulamayi yine kullanabiliyor.
 */
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onBitti: () -> Unit,
    onAtla: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var adim: Int by rememberSaveable { mutableStateOf(0) }

    LkHeroPage(title = "İşletmeni tanıyalım", onBack = onAtla) {
        when (val durum = uiState) {
            is KurulumUiState.Yukleniyor -> LkLoadingState()
            is KurulumUiState.Hata -> LkErrorState(
                message = durum.mesaj,
                onRetry = { viewModel.yukle() }
            )
            is KurulumUiState.Icerik -> {
                val p = durum.profil
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(LkSpacing.Space4),
                    verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                ) {
                    item {
                        Text(
                            text = "Adım ${adim + 1} / 3",
                            style = LkTypography.getMicro(),
                            color = LkTextMuted
                        )
                    }

                    when (adim) {
                        0 -> item { IsletmeAdimi(p, viewModel) }
                        1 -> item { KanalAdimi(p, viewModel) }
                        else -> item { HedefAdimi(p, viewModel) }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                            if (adim > 0) {
                                LkButton(
                                    text = "Geri",
                                    variant = LkButtonVariant.SECONDARY,
                                    onClick = { adim-- },
                                    enabled = !durum.kaydediliyor,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            LkButton(
                                text = when {
                                    durum.kaydediliyor -> "Kaydediliyor..."
                                    adim < 2 -> "Devam"
                                    else -> "Kaydet ve başla"
                                },
                                onClick = {
                                    if (adim < 2) adim++
                                    else viewModel.kaydetVeTamamla(onBitti)
                                },
                                enabled = !durum.kaydediliyor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        /* Atlama HER ADIMDA acik: kurulum kapi degil. */
                        LkButton(
                            text = "Şimdilik atla",
                            variant = LkButtonVariant.QUIET,
                            onClick = onAtla,
                            enabled = !durum.kaydediliyor,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IsletmeAdimi(p: KurulumProfiliDto, viewModel: OnboardingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)) {
        LkSectionHeader(title = "İşletmen", subtitle = "Adı ve ne iş yaptığı")
        LkTextField(
            value = p.name.orEmpty(),
            onValueChange = { yeni -> viewModel.guncelle { it.copy(name = yeni) } },
            label = "İşletme adı"
        )
        LkTextField(
            value = p.sector.orEmpty(),
            onValueChange = { yeni -> viewModel.guncelle { it.copy(sector = yeni) } },
            label = "Sektör"
        )
        LkTextField(
            value = p.city.orEmpty(),
            onValueChange = { yeni -> viewModel.guncelle { it.copy(city = yeni) } },
            label = "Şehir"
        )

        Text("İŞLETMENİN AŞAMASI", style = LkTypography.getMicro(), color = LkTextMuted)
        SecimSeridi(
            secenekler = ASAMALAR,
            secili = setOfNotNull(p.businessStage),
            onSec = { deger ->
                /* Tekli secim: ayni degere tekrar dokunmak TEMIZLIYOR --
                   kullanici yanlis sectiyse geri alabilmeli. */
                viewModel.guncelle {
                    it.copy(businessStage = if (it.businessStage == deger) null else deger)
                }
            }
        )

        LkNumericField(
            value = p.employeeCount?.toString().orEmpty(),
            onValueChange = { yeni ->
                viewModel.guncelle { it.copy(employeeCount = yeni.trim().toIntOrNull()) }
            },
            label = "Çalışan sayısı",
            placeholder = "Kendin dahil"
        )
    }
}

@Composable
private fun KanalAdimi(p: KurulumProfiliDto, viewModel: OnboardingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)) {
        LkSectionHeader(title = "Nereden satıyorsun?", subtitle = "Birden fazla seçebilirsin")
        SecimSeridi(
            secenekler = KANALLAR,
            secili = p.salesChannels.toSet(),
            onSec = { deger ->
                viewModel.guncelle { profil ->
                    val yeni = if (deger in profil.salesChannels) {
                        profil.salesChannels - deger
                    } else {
                        profil.salesChannels + deger
                    }
                    profil.copy(salesChannels = yeni)
                }
            }
        )

        Spacer(Modifier.height(LkSpacing.Space2))
        LkSectionHeader(title = "Aylık büyüklük", subtitle = "Yaklaşık değerler yeterli")
        /*
         * ⚠️ Bu iki alan ISTEGE BAGLI ve bos birakilabilir. Sunucu da
         * zorunlu tutmuyor. Bilmeyen kullaniciyi bir sayi uydurmaya
         * zorlamak, sonraki butun hesaplari o uydurma sayiya
         * dayandirmak olurdu.
         */
        LkNumericField(
            value = p.monthlySales?.toString().orEmpty(),
            onValueChange = { yeni ->
                viewModel.guncelle { it.copy(monthlySales = yeni.trim().toDoubleOrNull()) }
            },
            label = "Aylık satış",
            placeholder = "Bilmiyorsan boş bırak"
        )
        LkNumericField(
            value = p.monthlyExpenses?.toString().orEmpty(),
            onValueChange = { yeni ->
                viewModel.guncelle { it.copy(monthlyExpenses = yeni.trim().toDoubleOrNull()) }
            },
            label = "Aylık gider",
            placeholder = "Bilmiyorsan boş bırak"
        )
    }
}

@Composable
private fun HedefAdimi(p: KurulumProfiliDto, viewModel: OnboardingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)) {
        LkSectionHeader(title = "Ne yapmak istiyorsun?", subtitle = "Öncelikli hedefin")
        LkTextField(
            value = p.primaryGoal.orEmpty(),
            onValueChange = { yeni -> viewModel.guncelle { it.copy(primaryGoal = yeni) } },
            label = "Birincil hedef",
            placeholder = "Örnek: kârlılığı artırmak"
        )

        Spacer(Modifier.height(LkSpacing.Space2))
        Text("SENİ EN ÇOK NE ZORLUYOR?", style = LkTypography.getMicro(), color = LkTextMuted)
        SecimSeridi(
            secenekler = ZORLUKLAR,
            secili = p.challenges.toSet(),
            onSec = { deger ->
                viewModel.guncelle { profil ->
                    val yeni = if (deger in profil.challenges) {
                        profil.challenges - deger
                    } else {
                        profil.challenges + deger
                    }
                    profil.copy(challenges = yeni)
                }
            }
        )
    }
}

/**
 * Secim haplari.
 *
 * ⚠️ Secili durum renkle DEGIL, renk + golge ile veriliyor;
 * renk tek basina tasiyici olmamali.
 */
@Composable
private fun SecimSeridi(
    secenekler: List<Pair<String, String>>,
    secili: Set<String>,
    onSec: (String) -> Unit
) {
    FlowSatiri {
        secenekler.forEach { (deger, etiket) ->
            val aktif = deger in secili
            /*
             * ⚠️ `LkChip`in SECIM asiri yuklemesi kullaniliyor
             * (`text/selected/onClick`), renk veren digeri DEGIL.
             *
             * Iki sebep: (1) secili durumu renk + GOLGE ile veriyor;
             * renk tek basina tasiyici olmamali (§8.1). (2) Dokunma
             * hedefini 44dp'ye yukseltiyor (§19). Disaridan
             * `Modifier.clickable` takmak ikisini de atlardi -- ilk
             * yazimda oyleydi ve secimler kaydolmuyordu (olculdu
             * 08.09.2026, emulator).
             */
            LkChip(
                text = etiket,
                selected = aktif,
                onClick = { onSec(deger) },
                modifier = Modifier.padding(end = LkSpacing.Space2, bottom = LkSpacing.Space2)
            )
        }
    }
}

/** Basit sarmalayan satir — haplar tek satira sigmiyor. */
@Composable
private fun FlowSatiri(icerik: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(modifier = Modifier.fillMaxWidth()) {
        icerik()
    }
}

package pl.nagrzyby.app.ui.detail

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.nagrzyby.app.data.local.VerdictHistoryEntity
import pl.nagrzyby.app.data.model.ForestDistrict
import pl.nagrzyby.app.ui.components.EnvironmentMetricCard
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBack: () -> Unit,
    onShowMap: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val district = uiState.district
    val environment = uiState.environment
    val verdict = uiState.verdict

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szczegóły") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Wróć",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onShowMap) {
                        Icon(Icons.Default.Map, contentDescription = "Mapa")
                    }
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (uiState.isFavorite) {
                                Icons.Filled.Star
                            } else {
                                Icons.Outlined.StarOutline
                            },
                            contentDescription = "Ulubione",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    IconButton(
                        onClick = {
                            val text = viewModel.shareVerdictText()
                            if (text != null) {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(intent, "Udostępnij werdykt"))
                            }
                        },
                        enabled = verdict != null,
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Udostępnij")
                    }
                    IconButton(
                        onClick = { viewModel.refreshWeather() },
                        enabled = district != null && !environment.isLoading,
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Odśwież pogodę")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        },
    ) { padding ->
        if (district == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = environment.errorMessage ?: "Nie znaleziono nadleśnictwa",
                    style = MaterialTheme.typography.bodyLarge,
                )
                TextButton(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Wróć")
                }
            }
            return@Scaffold
        }

        DetailContent(
            modifier = Modifier.padding(padding),
            district = district,
            environment = environment,
            verdict = verdict,
            verdictHistory = uiState.verdictHistory,
            bdlForestSummary = uiState.bdlForestSummary,
            isLoadingBdlForest = uiState.isLoadingBdlForest,
            isAnalyzing = uiState.isAnalyzing,
            onAnalyze = viewModel::analyze,
            onShowMap = onShowMap,
            formatHistoryDate = viewModel::formatHistoryDate,
        )
    }
}

@Composable
private fun DetailContent(
    district: ForestDistrict,
    environment: pl.nagrzyby.app.data.model.ForestEnvironmentData,
    verdict: pl.nagrzyby.app.data.model.MushroomForecastVerdict?,
    verdictHistory: List<VerdictHistoryEntity>,
    bdlForestSummary: String?,
    isLoadingBdlForest: Boolean,
    isAnalyzing: Boolean,
    onAnalyze: () -> Unit,
    onShowMap: () -> Unit,
    formatHistoryDate: (Long) -> String,
    modifier: Modifier = Modifier,
) {
    val isLoading = environment.isLoading
    val locale = Locale.getDefault()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = district.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = district.region,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "GPS: ${formatCoord(district.latitude)}, ${formatCoord(district.longitude)}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val weatherSource = buildString {
            append("Pogoda: Open-Meteo")
            if (environment.fromCache) append(" (cache 30 min)")
            if (district.id.startsWith("bdl_")) append(" · Nadleśnictwo: BDL")
        }
        Text(
            text = weatherSource,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedButton(
            onClick = onShowMap,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text("Pokaż na mapie (OpenStreetMap)")
        }

        environment.errorMessage?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Dane środowiskowe",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        EnvironmentMetricCard(
            title = "Suma opadów (ostatnie 4 dni)",
            value = environment.rainfallLast4DaysMm?.let {
                "%.1f mm".format(locale, it)
            },
            isLoading = isLoading,
        )
        EnvironmentMetricCard(
            title = "Wilgotność ściółki / podłoża",
            value = environment.litterMoisturePercent?.let {
                "%.0f %%".format(locale, it)
            },
            subtitle = "Szacunek z wilgotności gleby lub powietrza (Open-Meteo)",
            isLoading = isLoading,
        )
        EnvironmentMetricCard(
            title = "Średnia temperatura (4 dni)",
            value = environment.averageTemperatureCelsius?.let {
                "%.1f °C".format(locale, it)
            },
            isLoading = isLoading,
        )
        EnvironmentMetricCard(
            title = "Główny drzewostan",
            value = environment.dominantTreeSpecies,
            subtitle = when {
                bdlForestSummary != null -> bdlForestSummary
                isLoadingBdlForest -> "Ładowanie danych leśnictw z BDL…"
                else -> "Szacunek lokalny — szczegóły z warstwy leśnictw BDL"
            },
            isLoading = isLoading || isLoadingBdlForest,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Werdykt algorytmu",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                when {
                    isAnalyzing -> {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text("Obliczanie…", style = MaterialTheme.typography.bodyMedium)
                    }
                    verdict != null -> {
                        verdict.scorePercent?.let { score ->
                            Text(
                                text = "Szansa: $score%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        verdict.summary?.let {
                            Text(text = it, style = MaterialTheme.typography.titleMedium)
                        }
                        verdict.recommendation?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = "Naciśnij „Analizuj”, aby ocenić warunki na podstawie pobranej pogody.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }

                Button(
                    onClick = onAnalyze,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && environment.errorMessage == null && !isAnalyzing,
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                    Text("Analizuj")
                }
            }
        }

        if (verdictHistory.isNotEmpty()) {
            Text(
                text = "Historia analiz",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp),
            )
            verdictHistory.forEach { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    ),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "${entry.scorePercent}% — ${entry.summary}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = formatHistoryDate(entry.createdAtEpochMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun formatCoord(value: Double): String =
    "%.4f°".format(Locale.getDefault(), value)

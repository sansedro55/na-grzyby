package pl.nagrzyby.app.ui.detail

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.nagrzyby.app.data.local.VerdictHistoryEntity
import pl.nagrzyby.app.data.model.ForestDistrict
import pl.nagrzyby.app.domain.PredictedMushroom
import pl.nagrzyby.app.domain.edibilityDisplay
import pl.nagrzyby.app.ui.components.EnvironmentMetricCard
import pl.nagrzyby.app.ui.components.TopBarIconBtn
import pl.nagrzyby.app.ui.theme.ScoreGreen
import pl.nagrzyby.app.ui.theme.ScoreRed
import pl.nagrzyby.app.ui.theme.ScoreYellow
import pl.nagrzyby.app.ui.theme.TopBarBg
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBack: () -> Unit,
    onShowMap: () -> Unit,
    onNavigateToHome: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val district = uiState.district
    val environment = uiState.environment
    val verdict = uiState.verdict

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    TopBarIconBtn(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Wróć",
                        onClick = onBack,
                    )
                },
                actions = {
                    TopBarIconBtn(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Strona główna",
                        onClick = onNavigateToHome,
                    )
                    TopBarIconBtn(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Mapa",
                        onClick = onShowMap,
                    )
                    TopBarIconBtn(
                        imageVector = if (uiState.isFavorite) Icons.Filled.Star
                            else Icons.Outlined.StarOutline,
                        contentDescription = "Ulubione",
                        onClick = { viewModel.toggleFavorite() },
                    )
                    TopBarIconBtn(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Udostępnij",
                        onClick = {
                            viewModel.shareVerdictText()?.let { text ->
                                context.startActivity(
                                    Intent.createChooser(
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, text)
                                        },
                                        "Udostępnij werdykt",
                                    ),
                                )
                            }
                        },
                    )
                    TopBarIconBtn(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Odśwież pogodę",
                        onClick = {
                            if (district != null && !environment.isLoading) {
                                viewModel.refreshWeather()
                            }
                        },
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TopBarBg,
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
            speciesComposition = uiState.speciesComposition,
            isLoadingSpecies = uiState.isLoadingSpecies,
            isAnalyzing = uiState.isAnalyzing,
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
    speciesComposition: String?,
    isLoadingSpecies: Boolean,
    isAnalyzing: Boolean,
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = district.name.uppercase(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Text(
            text = district.region,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        OutlinedButton(
            onClick = onShowMap,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text("Pokaż na mapie")
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

        Text(
            text = "Dane środowiskowe",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        EnvironmentMetricCard(
            title = "Opady (ostatnie 4 dni)",
            value = environment.rainfallLast4DaysMm?.let { "${"%.1f".format(locale, it)} mm" },
            icon = Icons.Default.WaterDrop,
            isLoading = isLoading,
        )
        EnvironmentMetricCard(
            title = "Wilgotność podłoża",
            value = environment.litterMoisturePercent?.let { "${"%.0f".format(locale, it)}%" },
            icon = Icons.Default.Opacity,
            isLoading = isLoading,
        )
        EnvironmentMetricCard(
            title = "Temperatura (średnia 4 dni)",
            value = environment.averageTemperatureCelsius?.let { "${"%.1f".format(locale, it)}°C" },
            icon = Icons.Default.DeviceThermostat,
            isLoading = isLoading,
        )
        EnvironmentMetricCard(
            title = "Drzewostan",
            value = speciesComposition ?: environment.dominantTreeSpecies,
            icon = Icons.Default.Forest,
            subtitle = when {
                isLoadingSpecies -> "Ładowanie danych z BDL…"
                speciesComposition != null -> "Skład gatunkowy z wydzieleń BDL"
                bdlForestSummary != null -> bdlForestSummary
                isLoadingBdlForest -> "Ładowanie danych leśnictw z BDL…"
                else -> "Szacunek lokalny"
            },
            isLoading = isLoading || isLoadingSpecies || isLoadingBdlForest,
        )

        if (environment.forecast.isNotEmpty()) {
            Text(
                text = "Prognoza na 3 dni",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    environment.forecast.forEach { day ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f),
                        ) {
                            val dateParts = day.date.split("-")
                            val label = if (dateParts.size == 3) dateParts[2] else day.date
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            day.temperatureMean?.let {
                                Text(
                                    text = "${"%.0f".format(locale, it)}°",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            day.precipitationSum?.let {
                                Text(
                                    text = "${"%.1f".format(locale, it)} mm",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Szanse na grzyby",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                when {
                    isAnalyzing -> {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text("Obliczanie…", style = MaterialTheme.typography.bodyLarge)
                    }
                    verdict != null -> {
                        val score = verdict.scorePercent ?: 0
                        val scoreColor = when {
                            score >= 70 -> ScoreGreen
                            score >= 45 -> ScoreYellow
                            else -> ScoreRed
                        }
                        val scoreBg = when {
                            score >= 70 -> Color(0xFFE8F5E9)
                            score >= 45 -> Color(0xFFFFF8E1)
                            else -> Color(0xFFFFEBEE)
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = scoreBg),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = "${score}%",
                                    fontSize = 56.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = scoreColor,
                                )
                                verdict.summary?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = scoreColor,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }
                            }
                        }

                        LinearProgressIndicator(
                            progress = { score / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp),
                            color = scoreColor,
                            trackColor = Color(0xFFE0E0E0),
                        )

                        verdict.recommendation?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = "Pobieranie danych pogodowych…",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }

        val mushrooms = verdict?.predictedMushrooms.orEmpty()
        if (mushrooms.isNotEmpty()) {
            Text(
                text = "Przewidywane owocniki",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = "na podstawie drzewostanu, pogody i pory roku",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    mushrooms.take(6).forEachIndexed { index, mushroom ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        val barColor = when {
                            mushroom.likelihoodPercent >= 70 -> ScoreGreen
                            mushroom.likelihoodPercent >= 50 -> ScoreYellow
                            else -> Color(0xFF9E9E9E)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mushroom.species.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = mushroom.species.latinName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                text = "${mushroom.likelihoodPercent}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = barColor,
                                modifier = Modifier.width(48.dp),
                                textAlign = TextAlign.End,
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = mushroom.species.edibilityDisplay(),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (mushroom.species.edibility == "choice")
                                    ScoreGreen else ScoreYellow,
                            )
                        }
                        mushroom.species.description.takeIf { it.isNotEmpty() }?.let { desc ->
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                        LinearProgressIndicator(
                            progress = { mushroom.likelihoodPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .height(8.dp),
                            color = barColor,
                            trackColor = Color(0xFFE0E0E0),
                        )
                    }
                }
            }
        }

        if (verdictHistory.isNotEmpty()) {
            Text(
                text = "Historia analiz",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp),
            )
            verdictHistory.forEach { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${entry.scorePercent}% — ${entry.summary}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = formatHistoryDate(entry.createdAtEpochMs),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

package pl.nagrzyby.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min

data class WeatherForecastData(
    val dayDate: LocalDate,
    val maxTempCelsius: Double,
    val minTempCelsius: Double,
    val precipitationMm: Double,
    val relativeHumidityPercent: Int,
)

/**
 * Komponent wykresu prognozy pogody na 4 dni.
 * Wyświetla temperaturę, opady i wilgotność w formie wizualnej.
 */
@Composable
fun WeatherForecastChart(
    forecasts: List<WeatherForecastData>,
    modifier: Modifier = Modifier,
) {
    if (forecasts.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Brak danych pogodowych",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        return
    }

    val tempRange = calculateTempRange(forecasts)
    val maxPrecip = forecasts.maxOfOrNull { it.precipitationMm } ?: 0.0

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Prognoza pogody (4 dni)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                forecasts.forEach { forecast ->
                    WeatherDayCard(
                        forecast = forecast,
                        tempRange = tempRange,
                        maxPrecip = maxPrecip,
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherDayCard(
    forecast: WeatherForecastData,
    tempRange: Pair<Double, Double>,
    maxPrecip: Double,
) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEE\ndd.MM")
    val dayLabel = forecast.dayDate.format(dateFormatter)

    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Data
            Text(
                text = dayLabel,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 4.dp),
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Wykres temperatury
            TemperatureBar(
                maxTemp = forecast.maxTempCelsius,
                minTemp = forecast.minTempCelsius,
                tempRange = tempRange,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Temperatury tekstowe
            Text(
                text = "${forecast.maxTempCelsius.toInt()}°C",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                text = "${forecast.minTempCelsius.toInt()}°C",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Opady
            if (forecast.precipitationMm > 0.0) {
                PrecipitationBar(
                    precipitation = forecast.precipitationMm,
                    maxPrecip = maxPrecip,
                )
                Text(
                    text = "${forecast.precipitationMm.toInt()}mm",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.surfaceTint,
                )
            } else {
                Text(
                    text = "0mm",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Wilgotność
            Text(
                text = "💧 ${forecast.relativeHumidityPercent}%",
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun TemperatureBar(
    maxTemp: Double,
    minTemp: Double,
    tempRange: Pair<Double, Double>,
) {
    val (minInRange, maxInRange) = tempRange
    val rangeSpan = maxInRange - minInRange
    val scaledMin = if (rangeSpan > 0) ((minTemp - minInRange) / rangeSpan) else 0.0
    val scaledMax = if (rangeSpan > 0) ((maxTemp - minInRange) / rangeSpan) else 1.0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.small,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(scaledMax.toFloat())
                .height(24.dp)
                .background(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    shape = MaterialTheme.shapes.small,
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(scaledMin.toFloat())
                .height(24.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    shape = MaterialTheme.shapes.small,
                ),
        )
    }
}

@Composable
private fun PrecipitationBar(
    precipitation: Double,
    maxPrecip: Double,
) {
    val ratio = if (maxPrecip > 0.0) (precipitation / maxPrecip).toFloat() else 0f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .background(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.small,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(ratio.coerceIn(0f, 1f))
                .height(12.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.8f),
                    shape = MaterialTheme.shapes.small,
                ),
        )
    }
}

private fun calculateTempRange(forecasts: List<WeatherForecastData>): Pair<Double, Double> {
    val minTemp = forecasts.minOfOrNull { it.minTempCelsius } ?: 0.0
    val maxTemp = forecasts.maxOfOrNull { it.maxTempCelsius } ?: 0.0

    val margin = (maxTemp - minTemp) * 0.1
    return Pair(
        (minTemp - margin).coerceAtMost(0.0),
        maxTemp + margin,
    )
}

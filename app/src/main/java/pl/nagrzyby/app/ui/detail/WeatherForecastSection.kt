package pl.nagrzyby.app.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.nagrzyby.app.ui.components.WeatherForecastChart
import pl.nagrzyby.app.ui.components.WeatherForecastData
import java.time.LocalDate

/**
 * Integracja wykresu pogody w ekranie szczegółów nadleśnictwa.
 */
@Composable
fun WeatherForecastSection(
    forecasts: List<WeatherForecastData>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        WeatherForecastChart(forecasts = forecasts)
    }
}

/**
 * Konwertuje dane pogodowe z API Open-Meteo do formatu wykresu.
 */
fun convertOpenMeteoToForecastData(
    dailyPrecipitation: List<Double?>,
    dailyTempMax: List<Double?>,
    dailyTempMin: List<Double?>,
    dailyHumidity: List<Int?>,
): List<WeatherForecastData> {
    val forecastList = mutableListOf<WeatherForecastData>()
    val maxDays = minOf(4, dailyPrecipitation.size, dailyTempMax.size, dailyTempMin.size)

    for (i in 0 until maxDays) {
        val dayDate = LocalDate.now().plusDays(i.toLong())
        val maxTemp = dailyTempMax.getOrNull(i) ?: continue
        val minTemp = dailyTempMin.getOrNull(i) ?: continue
        val precip = dailyPrecipitation.getOrNull(i) ?: 0.0
        val humidity = (dailyHumidity.getOrNull(i) ?: 50).coerceIn(0, 100)

        if (maxTemp != null && minTemp != null) {
            forecastList.add(
                WeatherForecastData(
                    dayDate = dayDate,
                    maxTempCelsius = maxTemp,
                    minTempCelsius = minTemp,
                    precipitationMm = precip ?: 0.0,
                    relativeHumidityPercent = humidity,
                )
            )
        }
    }

    return forecastList
}

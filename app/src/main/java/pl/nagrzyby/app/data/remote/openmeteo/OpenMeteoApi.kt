package pl.nagrzyby.app.data.remote.openmeteo

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open-Meteo Forecast API — bez klucza API.
 * @see <a href="https://open-meteo.com/en/docs">Dokumentacja</a>
 */
interface OpenMeteoApi {

    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("daily") daily: String = "precipitation_sum,temperature_2m_mean",
        @Query("hourly") hourly: String = "relative_humidity_2m,soil_moisture_0_to_7cm",
        @Query("past_days") pastDays: Int = 4,
        @Query("forecast_days") forecastDays: Int = 3,
        @Query("timezone") timezone: String = "auto",
    ): OpenMeteoForecastResponse
}

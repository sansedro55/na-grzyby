package pl.nagrzyby.app.data.remote.openmeteo

import com.google.gson.annotations.SerializedName

data class OpenMeteoForecastResponse(
    val daily: DailyUnits? = null,
    val hourly: HourlyUnits? = null,
)

data class DailyUnits(
    val time: List<String>? = null,
    @SerializedName("precipitation_sum")
    val precipitationSum: List<Double?>? = null,
    @SerializedName("temperature_2m_mean")
    val temperature2mMean: List<Double?>? = null,
)

data class HourlyUnits(
    @SerializedName("relative_humidity_2m")
    val relativeHumidity2m: List<Int?>? = null,
    @SerializedName("soil_moisture_0_to_7cm")
    val soilMoisture0To7cm: List<Double?>? = null,
)

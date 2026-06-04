package pl.nagrzyby.app.data.remote.nominatim

import com.google.gson.annotations.SerializedName

data class NominatimPlace(
    @SerializedName("display_name")
    val displayName: String? = null,
    val lat: String? = null,
    val lon: String? = null,
    val type: String? = null,
)

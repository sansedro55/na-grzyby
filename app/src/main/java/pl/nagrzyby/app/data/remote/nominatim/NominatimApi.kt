package pl.nagrzyby.app.data.remote.nominatim

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/**
 * Geokodowanie miejscowości — OpenStreetMap Nominatim.
 * @see <a href="https://nominatim.org/release-docs/develop/api/Search/">API</a>
 */
interface NominatimApi {

    @Headers("Accept-Language: pl")
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 1,
        @Query("countrycodes") countryCodes: String = "pl",
    ): List<NominatimPlace>
}

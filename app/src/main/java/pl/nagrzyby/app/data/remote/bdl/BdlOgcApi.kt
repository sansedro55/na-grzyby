package pl.nagrzyby.app.data.remote.bdl

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * OGC API Features — Bank Danych o Lasach.
 * Kolekcja [nadlesnictwa]: granice nadleśnictw (pole inspectorate_name).
 */
interface BdlOgcApi {

    @GET("collections/nadlesnictwa/items")
    suspend fun getDistrictItems(
        @Query("f") format: String = "json",
        @Query("limit") limit: Int = 25,
        @Query("bbox") bbox: String? = null,
        @Query("filter") filter: String? = null,
    ): BdlFeatureCollectionResponse

    @GET("collections/lesnictwa/items")
    suspend fun getLesnictwaItems(
        @Query("f") format: String = "json",
        @Query("limit") limit: Int = 15,
        @Query("bbox") bbox: String,
    ): BdlLesnictwaCollectionResponse
}

package pl.nagrzyby.app.data.remote

import okhttp3.OkHttpClient
import pl.nagrzyby.app.data.remote.bdl.BdlOgcApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object BdlClient {

    private const val BASE_URL = "https://ogcapi.bdl.lasy.gov.pl/"

    val api: BdlOgcApi by lazy {
        val httpClient = OkHttpClient.Builder()
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BdlOgcApi::class.java)
    }
}

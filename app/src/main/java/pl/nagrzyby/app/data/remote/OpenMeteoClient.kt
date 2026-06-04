package pl.nagrzyby.app.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pl.nagrzyby.app.BuildConfig
import pl.nagrzyby.app.data.remote.openmeteo.OpenMeteoApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object OpenMeteoClient {

    private const val BASE_URL = "https://api.open-meteo.com/"

    val api: OpenMeteoApi by lazy {
        val httpClientBuilder = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            httpClientBuilder.addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC },
            )
        }
        val httpClient = httpClientBuilder.build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenMeteoApi::class.java)
    }
}

package pl.nagrzyby.app.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import pl.nagrzyby.app.BuildConfig
import pl.nagrzyby.app.data.remote.nominatim.NominatimApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NominatimClient {

    private const val BASE_URL = "https://nominatim.openstreetmap.org/"

    val api: NominatimApi by lazy {
        val userAgentInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "NaGrzyby/${BuildConfig.VERSION_NAME} (${BuildConfig.APPLICATION_ID})")
                .build()
            chain.proceed(request)
        }
        val httpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(userAgentInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NominatimApi::class.java)
    }
}

package pl.nagrzyby.app.di

import android.content.Context
import pl.nagrzyby.app.data.local.AppDatabase
import pl.nagrzyby.app.data.local.DistrictDao
import pl.nagrzyby.app.data.local.WeatherCacheDao
import pl.nagrzyby.app.data.prefs.UserPreferences
import pl.nagrzyby.app.data.repository.BdlForestRepository
import pl.nagrzyby.app.data.repository.BdlLesnictwaRepository
import pl.nagrzyby.app.data.repository.BdlWydzieleniaRepository
import pl.nagrzyby.app.data.repository.ForestRepository
import pl.nagrzyby.app.data.repository.PlaceForestSearchRepository
import pl.nagrzyby.app.data.repository.VerdictHistoryRepository
import pl.nagrzyby.app.data.repository.WeatherRepository

class AppContainer(context: Context) {

    private val database = AppDatabase.get(context)
    val districtDao: DistrictDao = database.districtDao()
    val weatherCacheDao: WeatherCacheDao = database.weatherCacheDao()

    val userPreferences = UserPreferences(context)
    val forestRepository = ForestRepository(districtDao)
    val bdlForestRepository = BdlForestRepository(forestRepository)
    val placeForestSearchRepository = PlaceForestSearchRepository(
        bdlForestRepository = bdlForestRepository,
        forestRepository = forestRepository,
    )
    val bdlLesnictwaRepository = BdlLesnictwaRepository()
    val bdlWydzieleniaRepository = BdlWydzieleniaRepository()
    val weatherRepository = WeatherRepository(weatherCacheDao, forestRepository)
    val verdictHistoryRepository = VerdictHistoryRepository(database.verdictHistoryDao())
}

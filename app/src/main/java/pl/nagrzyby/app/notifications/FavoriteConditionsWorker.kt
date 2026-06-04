package pl.nagrzyby.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import pl.nagrzyby.app.NaGrzybyApplication
import pl.nagrzyby.app.domain.MushroomForecastAnalyzer

class FavoriteConditionsWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? NaGrzybyApplication ?: return Result.failure()
        val container = app.container
        val prefs = container.userPreferences

        if (!prefs.areNotificationsEnabled()) return Result.success()

        val favoriteIds = prefs.getFavoriteIdsSnapshot()
        if (favoriteIds.isEmpty()) return Result.success()

        var notified = 0
        for (districtId in favoriteIds) {
            val district = container.forestRepository.getDistrictById(districtId) ?: continue
            try {
                val env = container.weatherRepository.fetchEnvironmentData(
                    districtId = districtId,
                    latitude = district.latitude,
                    longitude = district.longitude,
                    forceRefresh = false,
                )
                val verdict = MushroomForecastAnalyzer.analyze(env)
                val score = verdict.scorePercent ?: continue
                if (score >= GOOD_SCORE_THRESHOLD) {
                    MushroomNotificationHelper.showGoodConditions(
                        context = applicationContext,
                        districtId = districtId,
                        districtName = district.name,
                        scorePercent = score,
                        summary = verdict.summary.orEmpty(),
                    )
                    notified++
                }
            } catch (_: Exception) {
                // Pomijamy nadleśnictwo przy błędzie sieci / API
            }
        }

        return if (notified > 0) Result.success() else Result.success()
    }

    companion object {
        const val WORK_NAME = "favorite_mushroom_conditions"
        private const val GOOD_SCORE_THRESHOLD = 70
    }
}

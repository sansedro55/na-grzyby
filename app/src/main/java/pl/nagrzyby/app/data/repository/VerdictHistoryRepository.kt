package pl.nagrzyby.app.data.repository

import pl.nagrzyby.app.data.local.VerdictHistoryDao
import pl.nagrzyby.app.data.local.VerdictHistoryEntity
import pl.nagrzyby.app.data.model.MushroomForecastVerdict

class VerdictHistoryRepository(
    private val dao: VerdictHistoryDao,
) {

    suspend fun save(
        districtId: String,
        districtName: String,
        verdict: MushroomForecastVerdict,
    ) {
        val score = verdict.scorePercent ?: return
        val summary = verdict.summary ?: return
        dao.deleteForDistrict(districtId)
        dao.insert(
            VerdictHistoryEntity(
                districtId = districtId,
                districtName = districtName,
                scorePercent = score,
                summary = summary,
                recommendation = verdict.recommendation.orEmpty(),
                createdAtEpochMs = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun getForDistrict(districtId: String) = dao.getForDistrict(districtId)

    suspend fun getRecent(limit: Int = 10) = dao.getRecent(limit)

    suspend fun getAll() = dao.getAll()

    suspend fun getAllDistinct(): List<VerdictHistoryEntity> {
        val all = dao.getAll()
        return all.distinctBy { it.districtId }
    }
}

package pl.nagrzyby.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface VerdictHistoryDao {

    @Insert
    suspend fun insert(entry: VerdictHistoryEntity)

    @Query(
        """
        SELECT * FROM verdict_history
        WHERE districtId = :districtId
        ORDER BY createdAtEpochMs DESC
        LIMIT :limit
        """,
    )
    suspend fun getForDistrict(districtId: String, limit: Int = 5): List<VerdictHistoryEntity>

    @Query(
        """
        SELECT * FROM verdict_history
        ORDER BY createdAtEpochMs DESC
        LIMIT :limit
        """,
    )
    suspend fun getRecent(limit: Int = 10): List<VerdictHistoryEntity>

    @Query(
        """
        SELECT * FROM verdict_history
        ORDER BY createdAtEpochMs DESC
        """,
    )
    suspend fun getAll(): List<VerdictHistoryEntity>

    @Query("DELETE FROM verdict_history WHERE districtId = :districtId")
    suspend fun deleteForDistrict(districtId: String)
}

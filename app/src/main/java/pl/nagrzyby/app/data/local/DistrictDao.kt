package pl.nagrzyby.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DistrictDao {

    @Query("SELECT * FROM districts ORDER BY name")
    suspend fun getAll(): List<DistrictEntity>

    @Query("SELECT * FROM districts WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): DistrictEntity?

    @Query(
        """
        SELECT * FROM districts
        WHERE name LIKE '%' || :query || '%' COLLATE NOCASE
           OR region LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY name
        """,
    )
    suspend fun search(query: String): List<DistrictEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(districts: List<DistrictEntity>)
}

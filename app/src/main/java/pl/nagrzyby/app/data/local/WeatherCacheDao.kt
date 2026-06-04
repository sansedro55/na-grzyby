package pl.nagrzyby.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherCacheDao {

    @Query("SELECT * FROM weather_cache WHERE districtId = :districtId LIMIT 1")
    suspend fun get(districtId: String): WeatherCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WeatherCacheEntity)

    @Query("DELETE FROM weather_cache WHERE districtId = :districtId")
    suspend fun delete(districtId: String)
}

package pl.nagrzyby.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "verdict_history",
    indices = [Index(value = ["districtId"])],
)
data class VerdictHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val districtId: String,
    val districtName: String,
    val scorePercent: Int,
    val summary: String,
    val recommendation: String,
    val createdAtEpochMs: Long,
)

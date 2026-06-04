package pl.nagrzyby.app.ui

data class RecentVerdictUi(
    val districtId: String,
    val districtName: String,
    val scorePercent: Int,
    val summary: String,
    val createdAtEpochMs: Long,
)

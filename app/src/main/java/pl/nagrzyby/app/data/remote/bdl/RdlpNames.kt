package pl.nagrzyby.app.data.remote.bdl

object RdlpNames {

    fun fromRegionCode(code: String?): String {
        if (code.isNullOrBlank()) return "Las Państwowy"
        return regionNames[code] ?: "RDLP (kod $code)"
    }

    private val regionNames = mapOf(
        "01" to "RDLP Białystok",
        "02" to "RDLP Katowice",
        "03" to "RDLP Kraków",
        "04" to "RDLP Krosno",
        "05" to "RDLP Lublin",
        "06" to "RDLP Łódź",
        "07" to "RDLP Olsztyn",
        "08" to "RDLP Piła",
        "09" to "RDLP Poznań",
        "10" to "RDLP Radom",
        "11" to "RDLP Szczecin",
        "12" to "RDLP Toruń",
        "13" to "RDLP Warszawa",
        "14" to "RDLP Wrocław",
        "15" to "RDLP Gdańsk",
        "16" to "RDLP Szczecinek",
        "17" to "RDLP Zielona Góra",
    )
}

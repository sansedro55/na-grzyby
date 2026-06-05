package pl.nagrzyby.app.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Eksportuje dane nadleśnictwa i lokalizację użytkownika do formatu GPX.
 * GPX (GPS Exchange Format) jest standardem do przechowywania i wymiany danych GPS.
 */
object GpxExporter {

    private const val FILE_PROVIDER_AUTHORITY = "pl.nagrzyby.app.fileprovider"

    /**
     * Generuje zawartość pliku GPX z punktem nadleśnictwa.
     */
    fun generateGpxContent(
        districtName: String,
        latitude: Double,
        longitude: Double,
        elevation: Double = 0.0,
    ): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }.format(Date())

        return buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine("""<gpx version="1.1" creator="Na grzyby" xmlns="http://www.topografix.com/GPX/1/1">""")
            appendLine("""  <metadata>""")
            appendLine("""    <name>$districtName - Na grzyby</name>""")
            appendLine("""    <time>$timestamp</time>""")
            appendLine("""    <bounds minlat="$latitude" minlon="$longitude" maxlat="$latitude" maxlon="$longitude" />""")
            appendLine("""  </metadata>""")
            appendLine("""  <wpt lat="$latitude" lon="$longitude">""")
            appendLine("""    <ele>$elevation</ele>""")
            appendLine("""    <name>$districtName</name>""")
            appendLine("""    <desc>Nadleśnictwo zarejestrowane w aplikacji Na grzyby</desc>""")
            appendLine("""    <type>Forest</type>""")
            appendLine("""  </wpt>""")
            appendLine("""</gpx>""")
        }
    }

    /**
     * Eksportuje GPX do pliku i zwraca URI dla udostępnienia.
     */
    fun exportToFile(
        context: Context,
        gpxContent: String,
        districtName: String,
    ): Uri {
        val cacheDir = context.cacheDir
        val gpxFile = File(cacheDir, "export_${System.currentTimeMillis()}.gpx")

        gpxFile.writeText(gpxContent, Charsets.UTF_8)

        return FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, gpxFile)
    }

    /**
     * Generuje i eksportuje plik GPX z nazwaną ścieżką.
     */
    fun createNamedGpxFile(
        context: Context,
        districtName: String,
        latitude: Double,
        longitude: Double,
    ): Uri {
        val gpxContent = generateGpxContent(districtName, latitude, longitude)
        return exportToFile(context, gpxContent, districtName)
    }

    /**
     * Generuje plik GPX z wieloma punktami (trasa na pieszą wędrówkę).
     */
    fun generateWalkingRouteGpx(
        districtName: String,
        waypoints: List<WaypointData>,
    ): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }.format(Date())

        return buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine("""<gpx version="1.1" creator="Na grzyby - Walking Route" xmlns="http://www.topografix.com/GPX/1/1">""")
            appendLine("""  <metadata>""")
            appendLine("""    <name>Trasa piesza - $districtName</name>""")
            appendLine("""    <time>$timestamp</time>""")
            if (waypoints.isNotEmpty()) {
                val minLat = waypoints.minOf { it.latitude }
                val maxLat = waypoints.maxOf { it.latitude }
                val minLon = waypoints.minOf { it.longitude }
                val maxLon = waypoints.maxOf { it.longitude }
                appendLine("""    <bounds minlat="$minLat" minlon="$minLon" maxlat="$maxLat" maxlon="$maxLon" />""")
            }
            appendLine("""  </metadata>""")

            // Punkty trasy
            appendLine("""  <trk>""")
            appendLine("""    <name>Trasa piesza w $districtName</name>""")
            appendLine("""    <trkseg>""")
            waypoints.forEach { point ->
                appendLine("""      <trkpt lat="${point.latitude}" lon="${point.longitude}">""")
                if (point.elevation > 0.0) {
                    appendLine("""        <ele>${point.elevation}</ele>""")
                }
                appendLine("""        <name>${point.name}</name>""")
                appendLine("""      </trkpt>""")
            }
            appendLine("""    </trkseg>""")
            appendLine("""  </trk>""")
            appendLine("""</gpx>""")
        }
    }

    data class WaypointData(
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val elevation: Double = 0.0,
    )
}

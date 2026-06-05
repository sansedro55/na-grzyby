package pl.nagrzyby.app.data.remote.bdl

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

/**
 * Szacuje środek ciężkości z geometrii GeoJSON (Polygon / MultiPolygon).
 */
object GeometryCentroid {

    fun fromGeometry(geometry: JsonElement?): Pair<Double, Double>? {
        if (geometry == null || geometry.isJsonNull) return null
        if (!geometry.isJsonObject) return null
        val obj = geometry.asJsonObject
        val type = obj.get("type")?.asString ?: return null
        val coordinates = obj.get("coordinates") ?: return null
        return when (type) {
            "Polygon" -> centroidFromPolygon(coordinates.asJsonArray)
            "MultiPolygon" -> {
                val multi = coordinates.asJsonArray
                if (multi.size() == 0) return null
                centroidFromPolygon(multi[0].asJsonArray)
            }
            else -> null
        }
    }

    fun isInside(latitude: Double, longitude: Double, geometry: JsonElement?): Boolean {
        if (geometry == null || geometry.isJsonNull || !geometry.isJsonObject) return false
        val obj = geometry.asJsonObject
        val type = obj.get("type")?.asString ?: return false
        val coordinates = obj.get("coordinates") ?: return false
        return when (type) {
            "Polygon" -> pointInPolygon(latitude, longitude, coordinates.asJsonArray)
            "MultiPolygon" -> {
                val multi = coordinates.asJsonArray
                for (i in 0 until multi.size()) {
                    if (pointInPolygon(latitude, longitude, multi[i].asJsonArray)) return true
                }
                false
            }
            else -> false
        }
    }

    /** Ray-casting: czy punkt (lat, lon) znajduje się w wielokącie GeoJSON. */
    private fun pointInPolygon(latitude: Double, longitude: Double, rings: JsonArray): Boolean {
        if (rings.size() == 0) return false
        val outer = rings[0].asJsonArray
        var inside = false
        var j = outer.size() - 1
        for (i in 0 until outer.size()) {
            val pi = outer[i].asJsonArray
            val pj = outer[j].asJsonArray
            if (pi.size() < 2 || pj.size() < 2) { j = i; continue }
            // GeoJSON: [lon, lat]
            val xi = pi[0].asDouble
            val yi = pi[1].asDouble
            val xj = pj[0].asDouble
            val yj = pj[1].asDouble
            if ((yi > latitude) != (yj > latitude) &&
                longitude < (xj - xi) * (latitude - yi) / (yj - yi) + xi
            ) {
                inside = !inside
            }
            j = i
        }
        return inside
    }

    private fun centroidFromPolygon(rings: JsonArray): Pair<Double, Double>? {
        if (rings.size() == 0) return null
        val outer = rings[0].asJsonArray
        var sumLat = 0.0
        var sumLon = 0.0
        var count = 0
        val step = (outer.size() / 200).coerceAtLeast(1)
        var i = 0
        while (i < outer.size()) {
            val point = outer[i].asJsonArray
            if (point.size() >= 2) {
                sumLon += point[0].asDouble
                sumLat += point[1].asDouble
                count++
            }
            i += step
        }
        if (count == 0) return null
        return sumLat / count to sumLon / count
    }
}

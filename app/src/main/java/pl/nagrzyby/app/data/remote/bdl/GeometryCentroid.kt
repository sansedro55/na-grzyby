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

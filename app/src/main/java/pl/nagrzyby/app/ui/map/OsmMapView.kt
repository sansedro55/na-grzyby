package pl.nagrzyby.app.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import pl.nagrzyby.app.logging.DownloadsErrorLogger
import pl.nagrzyby.app.map.OsmdroidInitializer

@Composable
fun OsmMapView(
    districtLatitude: Double,
    districtLongitude: Double,
    districtName: String,
    userLatitude: Double?,
    userLongitude: Double?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        OsmdroidInitializer.init(context)
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            isTilesScaledToDpi = true
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            mapView.onResume()
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onDetach()
        }
    }

    DisposableEffect(
        districtLatitude,
        districtLongitude,
        userLatitude,
        userLongitude,
        districtName,
    ) {
        try {
            mapView.overlays.clear()
            val districtPoint = GeoPoint(districtLatitude, districtLongitude)
            val districtMarker = Marker(mapView).apply {
                position = districtPoint
                title = districtName
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            mapView.overlays.add(districtMarker)

            val userLat = userLatitude
            val userLon = userLongitude
            if (userLat != null && userLon != null) {
                val userPoint = GeoPoint(userLat, userLon)
                val userMarker = Marker(mapView).apply {
                    position = userPoint
                    title = "Twoja lokalizacja"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                }
                mapView.overlays.add(userMarker)

                val centerLat = (districtLatitude + userLat) / 2.0
                val centerLon = (districtLongitude + userLon) / 2.0
                mapView.controller.setCenter(GeoPoint(centerLat, centerLon))
                val latSpan = kotlin.math.abs(districtLatitude - userLat)
                val lonSpan = kotlin.math.abs(districtLongitude - userLon)
                val span = maxOf(latSpan, lonSpan, 0.05)
                val zoom = when {
                    span > 2.0 -> 7.0
                    span > 1.0 -> 8.0
                    span > 0.5 -> 9.0
                    span > 0.2 -> 10.0
                    else -> 11.0
                }
                mapView.controller.setZoom(zoom)
            } else {
                mapView.controller.setCenter(districtPoint)
                mapView.controller.setZoom(11.0)
            }
            mapView.invalidate()
        } catch (e: Exception) {
            DownloadsErrorLogger.log(
                context = context,
                level = "ERROR",
                tag = "OsmMapView",
                message = "Błąd konfiguracji mapy OSM",
                throwable = e,
            )
        }
        onDispose { }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { view ->
            view.invalidate()
        },
    )
}

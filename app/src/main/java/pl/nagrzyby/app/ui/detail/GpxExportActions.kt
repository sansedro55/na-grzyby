package pl.nagrzyby.app.ui.detail

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import pl.nagrzyby.app.util.GpxExporter

/**
 * Komponenty akcji eksportu i udostępniania GPS.
 */
@Composable
fun GpxExportActions(
    districtName: String,
    latitude: Double,
    longitude: Double,
    context: Context,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        // Przycisk eksportu GPX
        Button(
            onClick = {
                exportAndShareGpx(context, districtName, latitude, longitude)
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp),
            )
            Text("Eksportuj GPX")
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Przycisk udostępniania
        Button(
            onClick = {
                shareGpxFile(context, districtName, latitude, longitude)
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp),
            )
            Text("Udostępnij")
        }
    }
}

/**
 * Eksportuje plik GPX i wyświetla potwierdzenie.
 */
private fun exportAndShareGpx(
    context: Context,
    districtName: String,
    latitude: Double,
    longitude: Double,
) {
    try {
        val gpxContent = GpxExporter.generateGpxContent(
            districtName = districtName,
            latitude = latitude,
            longitude = longitude,
        )
        val uri = GpxExporter.exportToFile(context, gpxContent, districtName)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/gpx+xml"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Nadleśnictwo: $districtName")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Udostępnij plik GPX"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Udostępnia plik GPX za pośrednictwem dostępnych aplikacji.
 */
private fun shareGpxFile(
    context: Context,
    districtName: String,
    latitude: Double,
    longitude: Double,
) {
    exportAndShareGpx(context, districtName, latitude, longitude)
}

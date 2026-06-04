package pl.nagrzyby.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.nagrzyby.app.data.model.ForestDistrict
import java.util.Locale

@Composable
fun ForestDistrictCard(
    district: ForestDistrict,
    onClick: () -> Unit,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = district.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            supportingContent = {
                Column {
                    Text(
                        text = district.region,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    district.searchViaPlace?.let { place ->
                        Text(
                            text = "Dla miejscowości: $place",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    district.distanceKm?.let { km ->
                        val distanceLabel = if (district.searchViaPlace != null) {
                            "Odległość od miejscowości: ${"%.1f".format(Locale.getDefault(), km)} km"
                        } else {
                            "Odległość: ${"%.1f".format(Locale.getDefault(), km)} km"
                        }
                        Text(
                            text = distanceLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    if (district.id.startsWith("bdl_")) {
                        Text(
                            text = "Źródło: BDL",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Forest,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            trailingContent = {
                Column {
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = if (isFavorite) "Usuń z ulubionych" else "Dodaj do ulubionych",
                            tint = if (isFavorite) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                    )
                }
            },
        )
    }
}

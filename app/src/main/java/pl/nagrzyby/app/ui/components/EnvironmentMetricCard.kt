package pl.nagrzyby.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun EnvironmentMetricCard(
    title: String,
    value: String?,
    subtitle: String? = null,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = 12.dp),
                        strokeWidth = 2.dp,
                    )
                }
                value != null -> {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
                else -> {
                    Text(
                        text = "—",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

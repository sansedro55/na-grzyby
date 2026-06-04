package pl.nagrzyby.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.nagrzyby.app.ui.RecentVerdictUi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecentVerdictCard(
    verdict: RecentVerdictUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateText = SimpleDateFormat("d MMM, HH:mm", Locale("pl", "PL"))
        .format(Date(verdict.createdAtEpochMs))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = verdict.districtName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "${verdict.scorePercent}% — ${verdict.summary}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = dateText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

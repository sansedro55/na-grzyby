package pl.nagrzyby.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.dp
import androidx.glance.unit.sp

/**
 * Odbiornik widgetu do rejestracji w systemie Android.
 */
class MushroomChanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MushroomChanceWidget()
}

/**
 * Główny komponent widgetu na ekranie głównym.
 * Wyświetla szanse na grzybobranie dla wybranych nadleśnictw.
 */
class MushroomChanceWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            MushroomChanceWidgetContent()
        }
    }
}

/**
 * Zawartość widgetu z informacjami o szansach na grzybobranie.
 */
@Composable
private fun MushroomChanceWidgetContent() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(12.dp),
    ) {
        Text(
            text = "🍄 Na grzyby",
            style = TextStyle(
                fontSize = 16.sp,
            ),
        )

        Spacer(modifier = GlanceModifier.width(8.dp))

        // Placeholder dla danych ulubionych nadleśnictw
        Text(
            text = "Załadowanie danych pogodowych...",
            style = TextStyle(
                fontSize = 12.sp,
            ),
        )

        Spacer(modifier = GlanceModifier.width(8.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
        ) {
            Text(
                text = "Aktualizuj za:",
                style = TextStyle(
                    fontSize = 10.sp,
                ),
            )
            Spacer(modifier = GlanceModifier.width(4.dp))
            Text(
                text = "~12h",
                style = TextStyle(
                    fontSize = 10.sp,
                ),
            )
        }
    }
}

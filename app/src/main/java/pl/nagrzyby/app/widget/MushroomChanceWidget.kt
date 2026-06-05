package pl.nagrzyby.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Widget na ekran główny pokazujący prognozy dla ulubionych nadleśnictw.
 * Aktualizuje się co 12 godzin lub na żądanie użytkownika.
 */
class MushroomChanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MushroomChanceWidget()
}

/**
 * Główny komponent widgetu z widokami na grzybobranie.
 */
class MushroomChanceWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: Int) {
        // Implementacja będzie w następnym kroku z Compose Glance
    }
}

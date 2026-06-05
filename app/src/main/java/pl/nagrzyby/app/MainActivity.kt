package pl.nagrzyby.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import pl.nagrzyby.app.logging.DownloadsErrorLogger
import pl.nagrzyby.app.navigation.NaGrzybyNavGraph
import pl.nagrzyby.app.notifications.MushroomNotificationHelper
import pl.nagrzyby.app.ui.theme.NaGrzybyTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
            val districtId = intent.getStringExtra(MushroomNotificationHelper.EXTRA_DISTRICT_ID)
            setContent {
                val app = LocalContext.current.applicationContext as NaGrzybyApplication
                val themeMode by app.container.userPreferences.themeMode.collectAsState("system")
                NaGrzybyTheme(themeMode = themeMode) {
                    NaGrzybyNavGraph(startDistrictId = districtId)
                }
            }
        } catch (e: Exception) {
            DownloadsErrorLogger.log(
                context = this,
                level = "ERROR",
                tag = "MainActivity",
                message = "Błąd podczas onCreate / setContent",
                throwable = e,
            )
            throw e
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val districtId = intent.getStringExtra(MushroomNotificationHelper.EXTRA_DISTRICT_ID)
        if (!districtId.isNullOrBlank()) {
            recreate()
        }
    }
}

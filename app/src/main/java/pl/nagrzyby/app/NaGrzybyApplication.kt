package pl.nagrzyby.app

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import pl.nagrzyby.app.di.AppContainer
import pl.nagrzyby.app.logging.DownloadsErrorLogger
import pl.nagrzyby.app.map.OsmdroidInitializer
import pl.nagrzyby.app.notifications.NotificationScheduler

class NaGrzybyApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        OsmdroidInitializer.init(this)
        container = AppContainer(this)
        applicationScope.launch {
            if (container.userPreferences.areNotificationsEnabled()) {
                NotificationScheduler.schedule(this@NaGrzybyApplication)
            }
        }
        try {
            DownloadsErrorLogger.installCrashHandler(this)
            DownloadsErrorLogger.log(
                context = this,
                level = "INFO",
                tag = "Application",
                message = "Aplikacja uruchomiona. Log błędów: ${DownloadsErrorLogger.getLogDirectoryHint()}",
            )
        } catch (e: Exception) {
            android.util.Log.e("NaGrzybyApplication", "Błąd inicjalizacji loggera", e)
        }
    }
}

package pl.nagrzyby.app.map

import android.content.Context
import org.osmdroid.config.Configuration
import pl.nagrzyby.app.BuildConfig
import java.io.File

object OsmdroidInitializer {

    @Volatile
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            val appContext = context.applicationContext
            val config = Configuration.getInstance()
            val basePath = File(appContext.cacheDir, "osmdroid").apply { mkdirs() }
            val tileCache = File(basePath, "tiles").apply { mkdirs() }
            config.osmdroidBasePath = basePath
            config.osmdroidTileCache = tileCache
            config.userAgentValue = BuildConfig.APPLICATION_ID
            config.load(
                appContext,
                appContext.getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE),
            )
            initialized = true
        }
    }
}

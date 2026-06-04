package pl.nagrzyby.app.logging

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Zapisuje logi błędów w publicznym katalogu Pobrane (Download)/NaGrzyby/.
 * Na Androidzie 10+ używa MediaStore; starsze wersje — zapis bezpośredni do pliku.
 */
object DownloadsErrorLogger {

    private const val TAG = "DownloadsErrorLogger"
    private const val LOG_SUBDIR = "NaGrzyby"
    private const val LOG_FILE_NAME = "nagrzyby_errors.log"

    fun installCrashHandler(context: Context) {
        val appContext = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            log(
                appContext,
                level = "CRASH",
                tag = "UncaughtException",
                message = "Wątek: ${thread.name}",
                throwable = throwable,
            )
            previous?.uncaughtException(thread, throwable)
        }
    }

    fun log(
        context: Context,
        level: String,
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        val entry = buildLogEntry(level, tag, message, throwable)
        Log.e(tag, entry.trim(), throwable)
        writeToDownloads(context.applicationContext, entry)
    }

    fun getLogDirectoryHint(): String =
        "Pobrane (Download)/$LOG_SUBDIR/$LOG_FILE_NAME"

    private fun buildLogEntry(
        level: String,
        tag: String,
        message: String,
        throwable: Throwable?,
    ): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
            .format(Date())
        val builder = StringBuilder()
            .append("[").append(timestamp).append("] ")
            .append(level).append(" / ").append(tag).append(": ")
            .append(message).append('\n')
        throwable?.let {
            builder.append(Log.getStackTraceString(it)).append('\n')
        }
        builder.append("---\n")
        return builder.toString()
    }

    private fun writeToDownloads(context: Context, text: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appendViaMediaStore(context, text)
            } else {
                appendLegacy(context, text)
            }
        } catch (primary: Exception) {
            Log.e(TAG, "Zapis do Pobranych nie powiódł się, używam katalogu aplikacji", primary)
            appendFallback(context, text, primary)
        }
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.Q)
    private fun appendViaMediaStore(context: Context, text: String) {
        val resolver = context.contentResolver
        val existingUri = findExistingLogUri(context)
        if (existingUri != null) {
            resolver.openOutputStream(existingUri, "wa")?.use { stream ->
                stream.write(text.toByteArray(Charsets.UTF_8))
            } ?: throw IllegalStateException("Nie można otworzyć istniejącego pliku logu")
            return
        }

        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, LOG_FILE_NAME)
            put(MediaStore.Downloads.MIME_TYPE, "text/plain")
            put(
                MediaStore.Downloads.RELATIVE_PATH,
                "${Environment.DIRECTORY_DOWNLOADS}/$LOG_SUBDIR",
            )
            put(MediaStore.Downloads.IS_PENDING, 0)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("MediaStore.insert zwróciło null")
        resolver.openOutputStream(uri)?.use { stream ->
            stream.write(text.toByteArray(Charsets.UTF_8))
        } ?: throw IllegalStateException("Nie można utworzyć pliku logu")
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.Q)
    private fun findExistingLogUri(context: Context): Uri? {
        val resolver = context.contentResolver
        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Downloads._ID)
        val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ? AND " +
            "${MediaStore.Downloads.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf(
            LOG_FILE_NAME,
            "%${Environment.DIRECTORY_DOWNLOADS}%$LOG_SUBDIR%",
        )
        resolver.query(collection, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                return ContentUris.withAppendedId(collection, id)
            }
        }
        return null
    }

    @Suppress("DEPRECATION")
    private fun appendLegacy(context: Context, text: String) {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            LOG_SUBDIR,
        )
        if (!dir.exists() && !dir.mkdirs()) {
            throw IllegalStateException("Nie można utworzyć katalogu: ${dir.absolutePath}")
        }
        val file = File(dir, LOG_FILE_NAME)
        FileOutputStream(file, true).use { stream ->
            stream.write(text.toByteArray(Charsets.UTF_8))
        }
    }

    private fun appendFallback(context: Context, text: String, primary: Exception) {
        val fallbackDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: context.getExternalFilesDir(null)
            ?: return
        val file = File(fallbackDir, LOG_FILE_NAME)
        val header = buildLogEntry(
            level = "WARN",
            tag = TAG,
            message = "Kopia zapasowa — zapis do Pobranych nieudany: ${primary.message}",
            throwable = null,
        )
        file.appendText(header + text)
    }
}

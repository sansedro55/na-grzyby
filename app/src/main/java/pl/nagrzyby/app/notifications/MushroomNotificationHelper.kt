package pl.nagrzyby.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import pl.nagrzyby.app.MainActivity
import pl.nagrzyby.app.R

object MushroomNotificationHelper {

    const val CHANNEL_ID = "mushroom_conditions"
    private const val NOTIFICATION_ID_BASE = 4000

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Warunki grzybobrania",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Powiadomienia o dobrych warunkach w ulubionych nadleśnictwach"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    fun showGoodConditions(
        context: Context,
        districtId: String,
        districtName: String,
        scorePercent: Int,
        summary: String,
    ) {
        createChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DISTRICT_ID, districtId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            districtId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_mushroom)
            .setContentTitle("Dobre warunki: $districtName")
            .setContentText("$summary ($scorePercent%)")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$summary Szansa: $scorePercent%. Otwórz aplikację po szczegóły."),
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(
            NOTIFICATION_ID_BASE + districtId.hashCode().and(0xFFFF),
            notification,
        )
    }

    const val EXTRA_DISTRICT_ID = "extra_district_id"
}

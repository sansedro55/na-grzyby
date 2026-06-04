package pl.nagrzyby.app.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Pobiera współrzędne użytkownika przez FusedLocationProviderClient.
 */
class LocationProvider(context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Pair<Double, Double> = suspendCancellableCoroutine { cont ->
        val cancellation = CancellationTokenSource()
        cont.invokeOnCancellation { cancellation.cancel() }

        fusedClient
            .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellation.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    cont.resume(location.latitude to location.longitude)
                } else {
                    cont.resumeWithException(LocationUnavailableException())
                }
            }
            .addOnFailureListener { error ->
                cont.resumeWithException(error)
            }
    }
}

class LocationUnavailableException :
    Exception("Nie udało się ustalić lokalizacji. Sprawdź GPS i spróbuj ponownie.")

package com.udacity.locationreminder.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.udacity.locationreminder.locationreminders.data.ReminderDataSource
import com.udacity.locationreminder.locationreminders.data.dto.ReminderDTO
import com.udacity.locationreminder.locationreminders.data.dto.Result
import com.udacity.locationreminder.locationreminders.reminderslist.ReminderDataItem
import com.udacity.locationreminder.utils.sendNotification
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private val remindersLocalRepository: ReminderDataSource by inject()
    private val TAG = "GeofenceTransitionsJobIntentService"
    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = geofencingEvent.errorCode
            Log.e(TAG, errorMessage.toString())
            return
        }
        if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofencingEvent.geofenceTransition ==Geofence.GEOFENCE_TRANSITION_DWELL) {
            Log.e(TAG, "GEOFENCE TRANSITION ENTER")
            sendNotification(geofencingEvent.triggeringGeofences)
        }

    }

    private fun sendNotification(triggeringGeofences: List<Geofence>) {
        val requestId = when {
            triggeringGeofences.isNotEmpty() ->
            {
                Log.d(TAG, "sendNotification: " + triggeringGeofences[0].requestId)
                triggeringGeofences[0].requestId
            }

            else -> {
                Log.e(TAG, "No Geofence Trigger Found !")
                return
            }
        }

        if(requestId.isNullOrEmpty()) return
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            val result = remindersLocalRepository.getReminder(requestId)
            if (result is Result.Success<ReminderDTO>) {
                val reminderDTO = result.data
                sendNotification(
                    this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                        reminderDTO.title,
                        reminderDTO.description,
                        reminderDTO.location,
                        reminderDTO.latitude,
                        reminderDTO.longitude,
                        reminderDTO.id
                    )
                )
            }
        }
    }
}
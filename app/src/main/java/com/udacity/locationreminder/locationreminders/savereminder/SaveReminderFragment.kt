package com.udacity.locationreminder.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.locationreminder.R
import com.udacity.locationreminder.base.BaseFragment
import com.udacity.locationreminder.base.NavigationCommand
import com.udacity.locationreminder.databinding.FragmentSaveReminderBinding
import com.udacity.locationreminder.locationreminders.data.dto.ReminderDTO
import com.udacity.locationreminder.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.locationreminder.locationreminders.reminderslist.ReminderDataItem
import com.udacity.locationreminder.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

class SaveReminderFragment : BaseFragment() {
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    lateinit var geofencingClient: GeofencingClient
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                    NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value
            val reminderDTO = _viewModel.validateAndSaveReminder(
                    ReminderDataItem(
                            title = title,
                            description = description,
                            latitude = latitude,
                            longitude = longitude,
                            location = location
                    )
            )
            if (reminderDTO != null) {
                addGeofenceForClue(reminderDTO)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofenceForClue(reminder: ReminderDTO) {
        val geofence = Geofence.Builder()
                .setRequestId(reminder.id)
                .setCircularRegion(reminder.latitude!!, reminder.longitude!!, 100f)
                .setExpirationDuration(TimeUnit.HOURS.toMillis(1))
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()
        val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()
        
        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
            addOnSuccessListener {
            }
            addOnFailureListener {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewModel.onClear()
    }
}

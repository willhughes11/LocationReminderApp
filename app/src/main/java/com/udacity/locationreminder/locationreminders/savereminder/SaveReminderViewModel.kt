package com.udacity.locationreminder.locationreminders.savereminder

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.locationreminder.R
import com.udacity.locationreminder.base.BaseViewModel
import com.udacity.locationreminder.base.NavigationCommand
import com.udacity.locationreminder.locationreminders.data.ReminderDataSource
import com.udacity.locationreminder.locationreminders.data.dto.ReminderDTO
import com.udacity.locationreminder.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch

class SaveReminderViewModel(val app: Application, val dataSource: ReminderDataSource) :
        BaseViewModel(app) {
    val reminderTitle = MutableLiveData<String>()
    val reminderDescription = MutableLiveData<String>()
    val reminderSelectedLocationStr = MutableLiveData<String>()
    val selectedPOI = MutableLiveData<PointOfInterest>()
    val latitude = MutableLiveData<Double>()
    val longitude = MutableLiveData<Double>()
    private var _currentLocationSet = MutableLiveData<Boolean>()
    val currentLocationSet: LiveData<Boolean>
        get() = _currentLocationSet

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        reminderSelectedLocationStr.value = null
        selectedPOI.value = null
        latitude.value = null
        longitude.value = null
    }

    init {
        _currentLocationSet.value = false
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun setCurrentLocationObtained(isCurrentLocationObtained: Boolean) {
        _currentLocationSet.value = isCurrentLocationObtained
    }

    fun validateAndSaveReminder(reminderData: ReminderDataItem): ReminderDTO? {
        if (validateEnteredData(reminderData)) {
            return saveReminder(reminderData)
        } else
            return null
    }

    /**
     * Save the reminder to the data source
     */
    fun saveReminder(reminderData: ReminderDataItem): ReminderDTO {
        val reminderDTO = ReminderDTO(
                reminderData.title,
                reminderData.description,
                reminderData.location,
                reminderData.latitude,
                reminderData.longitude,
                reminderData.id
        )
        showLoading.value = true
        viewModelScope.launch {
            dataSource.saveReminder(
                    reminderDTO
            )
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
            navigationCommand.value = NavigationCommand.Back
        }
        return reminderDTO
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        return true
    }
}
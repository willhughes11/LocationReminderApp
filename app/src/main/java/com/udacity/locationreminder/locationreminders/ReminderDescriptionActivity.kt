package com.udacity.locationreminder.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.locationreminder.R
import com.udacity.locationreminder.databinding.ActivityReminderDescriptionBinding
import com.udacity.locationreminder.locationreminders.reminderslist.ReminderDataItem

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    private lateinit var binding: ActivityReminderDescriptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
                this,
                R.layout.activity_reminder_description
        )
        if (intent.hasExtra(EXTRA_ReminderDataItem)) {
            val reminderDataItem:ReminderDataItem = intent.getSerializableExtra(
                    EXTRA_ReminderDataItem) as ReminderDataItem
            binding.reminderDataItem = reminderDataItem
            binding.executePendingBindings()
        }
    }
}

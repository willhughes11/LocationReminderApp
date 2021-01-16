package com.udacity.locationreminder.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.locationreminder.locationreminders.data.dto.ReminderDTO
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var database: RemindersDatabase

    @Before
    fun initDB() {
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDB() = database.close()

    @Test
    fun saveReminderAndGetById() = runBlockingTest {
        val reminder = ReminderDTO(
                title = "Basketball",
                description = "Don't get crossed up or dunked on!",
                location = "B-Ball Court",
                latitude = 75.1234,
                longitude = 3333.1234
        )
        database.reminderDao().saveReminder(reminder)
        val loaded = database.reminderDao().getReminderById(reminder.id)
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun deleteAllRemindersAndReminders() = runBlockingTest {
        val reminder = ReminderDTO(
                title = "Basketball",
                description = "Don't get crossed up or dunked on!",
                location = "B-Ball Court",
                latitude = 75.1234,
                longitude = 3333.1234
        )

        database.reminderDao().saveReminder(reminder)
        database.reminderDao().deleteAllReminders()
        val reminders = database.reminderDao().getReminders()
        assertThat(reminders.isEmpty(), `is`(true))

    }

    @Test
    fun noRemindersFoundGetReminderById() = runBlockingTest {
        val reminder = database.reminderDao().getReminderById("3")

        assertThat(reminder, nullValue())

    }
}
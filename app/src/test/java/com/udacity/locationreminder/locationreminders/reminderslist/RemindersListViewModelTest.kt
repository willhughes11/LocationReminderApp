package com.udacity.locationreminder.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.locationreminder.locationreminders.MainCoroutineRule
import com.udacity.locationreminder.locationreminders.data.FakeDataSource
import com.udacity.locationreminder.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest {

    @get: Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    var mainCoroutineRule = MainCoroutineRule()
    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var datasource: FakeDataSource

    @Before
    fun setUp() {
        stopKoin()
        datasource = FakeDataSource()
        remindersListViewModel =
                RemindersListViewModel(ApplicationProvider.getApplicationContext(), datasource)

    }

    @Test
    fun check_loading() = mainCoroutineRule.runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        assertThat(
                remindersListViewModel.showLoading.getOrAwaitValue(),
                `is`(true)
        )
        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun shouldReturnError() = mainCoroutineRule.runBlockingTest {
        datasource.setReturnError(true)
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`("Test Exception"))
    }
}
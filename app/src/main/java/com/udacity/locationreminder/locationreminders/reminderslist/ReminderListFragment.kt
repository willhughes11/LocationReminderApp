package com.udacity.locationreminder.locationreminders.reminderslist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.udacity.locationreminder.R
import com.udacity.locationreminder.authentication.AuthenticationActivity
import com.udacity.locationreminder.base.BaseFragment
import com.udacity.locationreminder.base.NavigationCommand
import com.udacity.locationreminder.databinding.FragmentRemindersBinding
import com.udacity.locationreminder.utils.setDisplayHomeAsUpEnabled
import com.udacity.locationreminder.utils.setTitle
import com.udacity.locationreminder.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {
    override val _viewModel: RemindersListViewModel by viewModel()
    private lateinit var binding: FragmentRemindersBinding
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding =
                DataBindingUtil.inflate(
                        inflater,
                        R.layout.fragment_reminders, container, false
                )
        binding.viewModel = _viewModel

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        binding.refreshLayout.setOnRefreshListener { _viewModel.loadReminders() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }
    }

    override fun onResume() {
        super.onResume()
        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        _viewModel.navigationCommand.postValue(
                NavigationCommand.To(
                        ReminderListFragmentDirections.toSaveReminder()
                )
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
        }
        binding.remindersRecyclerView.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                Log.i("onOptionsItemSelected","LogOut clicked")
                AuthUI.getInstance().signOut(requireContext())
                startActivity(Intent(activity, AuthenticationActivity::class.java))
                requireActivity().finish()
            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_menu, menu)
    }
}

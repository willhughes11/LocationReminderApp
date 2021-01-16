package com.udacity.locationreminder.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.locationreminder.BuildConfig
import com.udacity.locationreminder.R
import com.udacity.locationreminder.base.BaseFragment
import com.udacity.locationreminder.databinding.FragmentSelectLocationBinding
import com.udacity.locationreminder.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.locationreminder.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.lang.Exception

private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val REQUEST_LOCATION_PERMISSION = 1
private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 2
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 3

class SelectLocationFragment : BaseFragment() {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var binding: FragmentSelectLocationBinding

    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    private val callback = OnMapReadyCallback { gMap ->
        Log.e("OnMapReadyCallback","OnMapReadyCallback")
        googleMap = gMap
        setMapStyle(googleMap)
        foregroundAndBackgroundLocationPermission()
        zoomToDeviceLocation()
        addPOI(googleMap)
        addMapClik(googleMap)
    }

    override val _viewModel: SaveReminderViewModel by inject()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(callback)
        fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireContext())
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        if (grantResults.isEmpty() || grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED
                || (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                        grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                        PackageManager.PERMISSION_DENIED)
        ) {
            Snackbar.make(
                    binding.constraintLayout,
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.settings) {
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }.show()
        } else {
            Log.e("onRequestPermissionsRes","onRequestPermissionsResult else called")
            checkDeviceLocationSettingsAndStartGeofence()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.i("onStart","foregroundAndBackgroundLocationPermission")

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("onActivityResult","ResultCode is $resultCode and requestCode is $requestCode")
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            Log.e("onActivityResult","onActivityResult REQUEST_TURN_DEVICE_LOCATION_ON ")
            checkDeviceLocationSettingsAndStartGeofence(false)
        }
    }

    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved =
                (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ))
        val backgroundLocationApproved =
                if (runningQOrLater) {

                    PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                } else {
                    true
                }
        return foregroundLocationApproved && backgroundLocationApproved
    }

    @SuppressLint("MissingPermission")
    fun zoomToDeviceLocation() {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
            if (location != null) {
                val userLatLng = LatLng(location.latitude, location.longitude)
                val zoomLevel = 15f
                googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                                userLatLng,
                                zoomLevel
                        )
                )
            }
        }
    }

    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermission() {
        Log.e("foregroundBackground","foregroundAndBackgroundLocationPermissionApproved is ${foregroundAndBackgroundLocationPermissionApproved()}")
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            checkDeviceLocationSettingsAndStartGeofence()
        }
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        Log.e("runningQOrLater","runningQOrLater is $runningQOrLater")
        val resultCode = when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else ->
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        Log.e("resultCode","resultCode is $resultCode")
        ActivityCompat.requestPermissions(
                requireActivity(),
                permissionsArray,
                resultCode
        )
    }

    private fun onLocationSelected(poi: PointOfInterest) {
        val latLng = poi.latLng
        _viewModel.reminderSelectedLocationStr.value = poi.name
        _viewModel.latitude.value = latLng.latitude
        _viewModel.longitude.value = latLng.longitude
        findNavController().popBackStack()
    }

    @SuppressLint("MissingPermission")
    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
                settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    Log.e("StartGeofence"," exception.startResolutionForResult")
                    startIntentSenderForResult(
                            exception.resolution.intentSender,
                            REQUEST_TURN_DEVICE_LOCATION_ON,
                            null,
                            0,
                            0,
                            0,
                            null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.e("StartGeofence","Error getting location settings resolution: ${sendEx.message}")
                }
            } else {
                Log.e("StartGeofence","Error getting location settings resolution: showing snackbar")
                Snackbar.make(
                        binding.constraintLayout,
                        R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    Log.e("StartGeofence","setAction oK")
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                googleMap.isMyLocationEnabled = true
            }
        }
    }

    private fun addPOI(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            binding.saveLocation.visibility = View.VISIBLE
            binding.saveLocation.setOnClickListener {
                onLocationSelected(poi)
            }
            val poiMarker = map.addMarker(
                    MarkerOptions().position(poi.latLng).title(poi.name)
            )
            poiMarker.showInfoWindow()
        }

    }

    private fun addMapClik(map: GoogleMap) {
        map.setOnMapClickListener {
            binding.saveLocation.visibility = View.VISIBLE
            binding.saveLocation.setOnClickListener { view ->
                _viewModel.latitude.value = it.latitude
                _viewModel.longitude.value = it.longitude
                _viewModel.reminderSelectedLocationStr.value = "Custom location used"
                findNavController().popBackStack()
            }

            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(it, 15f)
            map.moveCamera(cameraUpdate)
            val poiMarker = map.addMarker(MarkerOptions().position(it))
            poiMarker.showInfoWindow()
        }

    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            context,
                            R.raw.map_style
                    )
            )
        } catch (exc: Exception) {
            Log.e("setMapStyle","Exception getting the file")
        }
    }
}
package com.bubelov.coins.util

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.support.v4.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

/**
 * @author Igor Bubelov
 */

@SuppressLint("MissingPermission")
class LocationLiveData(context: Context, updateIntervalMillis: Long) : LiveData<Location>() {
    private val locationProvider = LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        interval = updateIntervalMillis
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            val location = result?.lastLocation

            if (location != null) {
                value = location
            }
        }
    }

    val hasLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    init {
        if (hasLocationPermission) {
            onLocationPermissionGranted()
        }
    }

    override fun onActive() {
        if (hasLocationPermission) {
            locationProvider.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    override fun onInactive() {
        locationProvider.removeLocationUpdates(locationCallback)
    }

    fun onLocationPermissionGranted() {
        locationProvider.lastLocation.addOnCompleteListener {
            if (value == null && it.result != null) {
                value = it.result
            }
        }

        if (hasActiveObservers()) {
            locationProvider.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }
}
package com.example.guniattendance.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import com.jianastrero.capiche.doIHave
import com.jianastrero.capiche.iNeed

class AccessMapLocation(val requireActivity:Activity, val requireContext:Context) {
    private var curLocation: Location? = null
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private var customProgressDialog:CustomProgressDialog?=null
    fun markAttendance(Latitude:Double,Longitude:Double,Range:Long):Boolean
    {
        requireActivity.doIHave(
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
            onGranted = {
                curLocation = getLocation()

            },
            onDenied = {
                requestPermission()
            })
        if (curLocation != null) {
            Log.d(
                "TAG_LOCATION",
                "subscribeToObserve: ${curLocation?.latitude}\t${curLocation?.longitude}"
            )
            val results = FloatArray(1)
            Location.distanceBetween(
                Latitude,
                Longitude,
                curLocation!!.latitude,
                curLocation!!.longitude,
                results
            )
            val distanceInMeters = results[0]

            return distanceInMeters <= Range.toFloat()

        } else {
            curLocation = getLocation()
            throw Exception("Fetching location, Please try again")
        }
    }
    private fun requestPermission() {
        requireActivity.iNeed(
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
            onGranted = {
                curLocation = getLocation()
            },
            onDenied = {
                throw Exception("Location permission needed to access features")
            }
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(): Location? {
        if(customProgressDialog==null)
        {
            customProgressDialog=CustomProgressDialog(requireContext)
        }
        customProgressDialog!!.start("Preparing Location...")
        locationManager =
            requireActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager


        locationListener = object : LocationListener {

            override fun onLocationChanged(p0: Location) {
                curLocation = p0
            }

            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1L,
                0f,
                locationListener
            )
            var curLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            customProgressDialog!!.stop()
            return curLoc
        } catch (e: Exception) {
            Log.d("TAG_ERROR", "getLocation: ${e.message}")
            customProgressDialog!!.stop()
            return null
        }

    }
}
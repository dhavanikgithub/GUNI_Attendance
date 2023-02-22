package com.example.guniattendance.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity

class PermissionsUtils {

    companion object{
        fun isOnline(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
            return false
        }

        fun checkPermission(context: Context): Boolean
        {
            // ContextCompat.checkSelfPermission() - is used to check the dangerous permission.
            val cameraPermission =
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            val readStoragePermission =
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
            val accessFinePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            val accessCoarsePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            val internetPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET)

            // if permission is already granted, then its 0, if not then its -1. So if cameraPermission is 0 then it's true, other wise its false.
            if(
                cameraPermission == PackageManager.PERMISSION_GRANTED
                &&
                readStoragePermission == PackageManager.PERMISSION_GRANTED
                &&
                accessFinePermission  == PackageManager.PERMISSION_GRANTED
                &&
                accessCoarsePermission == PackageManager.PERMISSION_GRANTED
                &&
                internetPermission == PackageManager.PERMISSION_GRANTED
            )
            {
                return true
            }
            return false

        }

        fun requestPermission(reqActivity: Activity)
        {
            val PERMISSION_CODE = 200

            requestPermissions(reqActivity, arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.INTERNET), PERMISSION_CODE)
        }

        fun checkUserRequestedDontAskAgain(reqActivity: Activity):Boolean
        {
            val rationalFlagCAMERA =
                reqActivity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
            val rationalFlagREAD =
                reqActivity.shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
            val rationalFlagINTERNET =
                reqActivity.shouldShowRequestPermissionRationale(Manifest.permission.INTERNET)
            val rationalFlagLOCATIONCOARSE =
                reqActivity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
            val rationalFlagLOCATIONFINE =
                reqActivity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
            if (!rationalFlagREAD
                &&
                !rationalFlagCAMERA
                &&
                !rationalFlagINTERNET
                &&
                !rationalFlagLOCATIONCOARSE
                &&
                !rationalFlagLOCATIONFINE)
            {
                return false
            }
            return true
        }

        fun onRequestPermissionResult(context: Context, reqActivity: Activity, grantResults: IntArray){
            if (grantResults.isNotEmpty())
            {
                val cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val readStoragePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED
                val accessFinePermission = grantResults[2] == PackageManager.PERMISSION_GRANTED
                val accessCoarsePermission = grantResults[3] == PackageManager.PERMISSION_GRANTED
                val internetPermission = grantResults[4] == PackageManager.PERMISSION_GRANTED
                if (!cameraPermission || !readStoragePermission || !accessFinePermission || !accessCoarsePermission || !internetPermission)
                {
                    if(!checkUserRequestedDontAskAgain(reqActivity))
                    {
                        val alertDialogBuilder = AlertDialog.Builder(context)
                        alertDialogBuilder
                            .setMessage("Click Settings to manually set permissions.")
                            .setCancelable(false)
                            .setPositiveButton("SETTINGS")
                            { dialog, id ->
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri = Uri.fromParts("package",context.packageName,null)
                                intent.data = uri
                                startActivity(context, intent, null)
                            }

                        val alertDialog = alertDialogBuilder.create()
                        alertDialog.show()
                    }
                    else
                    {
                        requestPermission(reqActivity)
                    }
                }
            }
        }
    }

}
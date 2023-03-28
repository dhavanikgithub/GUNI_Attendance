package com.example.guniattendance.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity


class PermissionsUtils {

    companion object{
        /*fun isOnline(context: Context): Boolean {
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
        }*/

        fun checkPermission(context: Context): Boolean
        {
            // ContextCompat.checkSelfPermission() - is used to check the dangerous permission.
            val cameraPermission =
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            val sdkVersion = Build.VERSION.SDK_INT
            val writeStoragePermission:Boolean = if(sdkVersion<=28) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
            val readPermission: Boolean = if(32>=sdkVersion) {
                val readStoragePermission =
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)


                readStoragePermission==PackageManager.PERMISSION_GRANTED

            } else{
                true
            }


            val accessFinePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            val accessCoarsePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            val internetPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET)

            // if permission is already granted, then its 0, if not then its -1. So if cameraPermission is 0 then it's true, other wise its false.
            if(
                cameraPermission == PackageManager.PERMISSION_GRANTED
                &&
                readPermission
                &&
                accessFinePermission  == PackageManager.PERMISSION_GRANTED
                &&
                accessCoarsePermission == PackageManager.PERMISSION_GRANTED
                &&
                internetPermission == PackageManager.PERMISSION_GRANTED
                &&
                writeStoragePermission
            )
            {
                return true
            }
            return false

        }

        fun requestPermission(reqActivity: Activity)
        {
            val PERMISSION_CODE = 200

            val sdkVersion = Build.VERSION.SDK_INT
            if(sdkVersion<=28)
            {
                requestPermissions(reqActivity, arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_CODE)
            }
            else if(sdkVersion<=32)
            {
                requestPermissions(reqActivity, arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET,
                    Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_CODE)
            }
            else{
                requestPermissions(reqActivity, arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET), PERMISSION_CODE)
            }

        }

        private fun checkUserRequestedDontAskAgain(reqActivity: Activity):Boolean
        {
            val sdkVersion = Build.VERSION.SDK_INT
            val rationalFlagWRITE:Boolean = if(sdkVersion<=28) {
                reqActivity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else{
                true
            }
            val rationalFlagREAD:Boolean = if(sdkVersion<=32) {
                reqActivity.shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else{
                true
            }
            val rationalFlagCAMERA =
                reqActivity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
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
                !rationalFlagLOCATIONFINE
                &&
                !rationalFlagWRITE)
            {
                return false
            }
            return true

        }

        fun onRequestPermissionResult(context: Context, reqActivity: Activity, grantResults: IntArray){
            if (grantResults.isNotEmpty())
            {
                val sdkVersion = Build.VERSION.SDK_INT
                val readStoragePermission:Boolean = if(sdkVersion<=32) {
                    grantResults[4] == PackageManager.PERMISSION_GRANTED
                } else{
                    true
                }
                val writeStoragePermission:Boolean = if(sdkVersion<=28) {
                    grantResults[5] == PackageManager.PERMISSION_GRANTED

                } else{
                    true
                }
                val cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val accessFinePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED
                val accessCoarsePermission = grantResults[2] == PackageManager.PERMISSION_GRANTED
                val internetPermission = grantResults[3] == PackageManager.PERMISSION_GRANTED
                if (!cameraPermission || !readStoragePermission || !accessFinePermission || !accessCoarsePermission || !internetPermission || !writeStoragePermission)
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
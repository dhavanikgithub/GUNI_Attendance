package com.example.guniattendance.authorization.authfragments.ui.splashscreen

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.guniattendance.R
import com.example.guniattendance.authorization.DownloadModel
import com.example.guniattendance.utils.PermissionsUtils
import kotlinx.coroutines.*
import org.json.JSONArray
import java.util.*


@SuppressLint("CustomSplashScreen")
class SplashScreenFragment : Fragment(R.layout.fragment_splash_screen) {

    private lateinit var progress: LottieAnimationView
    private val TAG="SplashScreenFragment"

    override fun onResume() {
        super.onResume()
        if(!PermissionsUtils.checkPermission(requireContext()))
        {
            PermissionsUtils.requestPermission(requireActivity())
        }
        else{
            if(!PermissionsUtils.isOnline(requireContext())){
                Log.i(ContentValues.TAG, "onCreate: 2")
                AlertDialog.Builder(requireActivity()).setTitle("No Internet")
                    .setMessage("Your devices is not connected to internet. " +
                            "Please check your internet connection and try again..")
                    .setPositiveButton("Settings"){ dialog, _ ->
                        startActivity(Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS
                        ))
                        dialog.dismiss()
                    }
                    .setNegativeButton("Retry"){ dialog, _ ->
                        findNavController().navigate(SplashScreenFragmentDirections.actionSplashScreenFragmentSelf())
                        dialog.dismiss()
                    }
                    .create().show()
            }
            else{
                val pm: PackageManager = requireContext().getPackageManager()
                val pkgName: String = requireContext().getPackageName()
                var pkgInfo: PackageInfo? = null
                try {
                    pkgInfo = pm.getPackageInfo(pkgName, 0)
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
                val applicationVersion = pkgInfo!!.versionName
                GlobalScope.launch {
                    var versionFileURL = "https://script.google.com/macros/s/AKfycbzRZq71El7QfSmce5IGNY7yEHhoEpOYFpQeoYcDwPLE_RnzQIhYI68C8NAOejH6ayLOwA/exec"
                    val mRequestQueue = Volley.newRequestQueue(context)
                    val request = object : StringRequest(
                        Method.GET, versionFileURL,
                        { response ->
                            val jsonArray = JSONArray(response)
                            Log.i(TAG,jsonArray.toString(4))
                            if(jsonArray.getJSONObject(0).getString("student")==applicationVersion)
                            {
                                MainScope().launch {
                                    findNavController().navigate(
                                        SplashScreenFragmentDirections
                                            .actionSplashScreenFragmentToLauncherScreenFragment()
                                    )
                                }
                            }
                            else{
                                AlertDialog.Builder(requireActivity()).setTitle("App Update")
                                    .setMessage("Your Application version is old please Update the APP")
                                    .setPositiveButton("UPDATE"){ dialog, _ ->
                                        val openURL = Intent(Intent.ACTION_VIEW)
                                        openURL.data = Uri.parse("https://drive.google.com/drive/folders/1jwyzjhzpUuPvUZzvJ4I-fn58sRVjBSwQ?usp=sharing")
                                        startActivity(openURL)
                                        dialog.dismiss()
                                    }
                                    .setCancelable(false)
                                    .create().show()
                            }

                        },
                        { error ->
                            Log.e(TAG,error.toString())
                        }){}
                    mRequestQueue.add(request)
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progress = view.findViewById(R.id.lottieAnimation)

    }

    override fun onDestroy() {
        DownloadModel.destroyObject()
        super.onDestroy()
    }

    override fun onDestroyView() {
        DownloadModel.destroyObject()
        super.onDestroyView()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionsUtils.onRequestPermissionResult(requireContext(), requireActivity(), grantResults)
    }
}
package com.example.guniattendance.authorization.authfragments.ui.splashscreen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.example.guniattendance.R
import com.example.guniattendance.data.entity.Faculty
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@SuppressLint("CustomSplashScreen")
class SplashScreenFragment : Fragment(R.layout.fragment_splash_screen) {


    private val faculties = FirebaseFirestore.getInstance().collection("faculty")
    private lateinit var progress: LottieAnimationView

    override fun onResume() {
        super.onResume()

        val defaultsRate: HashMap<String, Any> = HashMap()
        defaultsRate["new_version_code"] = java.lang.String.valueOf(getVersionCode())

        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(10) // change to 3600 on published app
            .build()

        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        firebaseRemoteConfig.setDefaultsAsync(defaultsRate)

        firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(
            requireActivity()
        ) { task ->
            if (task.isSuccessful) {
                val newVersionCode: String =
                    firebaseRemoteConfig.getString("new_version_code")
                val newAppLink: String = firebaseRemoteConfig.getString("new_app_link")
                if (newVersionCode.toInt() > getVersionCode()) {
                    progress.isVisible = false
                    showTheDialog(
                        newAppLink,
                        newVersionCode
                    )
                } else {
                    goAhead()
                }
            } else Log.e("MYLOG", "mFirebaseRemoteConfig.fetchAndActivate() NOT Successful")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progress = view.findViewById(R.id.lottieAnimation)

    }

    private fun goAhead() {

        if (FirebaseAuth.getInstance().currentUser != null) {

            CoroutineScope(Dispatchers.Main).launch {
                val faculty =
                    faculties.document(FirebaseAuth.getInstance().currentUser!!.uid).get().await()
                        .toObject(Faculty::class.java)

                val role = if (faculty == null) {
                    "student"
                } else {
                    "faculty"
                }

                findNavController().navigate(
                    SplashScreenFragmentDirections
                        .actionSplashScreenFragmentToAppPinFragment(role)
                )
            }
        } else {
            findNavController().navigate(
                SplashScreenFragmentDirections
                    .actionSplashScreenFragmentToLauncherScreenFragment()
            )
        }

    }






    private fun showTheDialog(newAppLink: String, versionFromRemoteConfig: String) {
        val dialog: AlertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Update")
            .setMessage("This version is absolute, please update to version: $versionFromRemoteConfig")
            .setPositiveButton("UPDATE") { _, _ ->
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(newAppLink)
                ).also {
                    startActivity(it)
                }
            }
            .show()
        dialog.setCancelable(false)
    }

    private var pInfo: PackageInfo? = null
    private fun getVersionCode(): Int {
        pInfo = null
        try {
            pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.i("MYLOG", "NameNotFoundException: " + e.message)
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pInfo!!.longVersionCode.toInt()
        } else {
            pInfo!!.versionCode
        }
    }

}
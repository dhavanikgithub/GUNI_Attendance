package com.example.guniattendance.authorization.authfragments.ui.splashscreen

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.example.guniattendance.R
import com.example.guniattendance.utils.PermissionsUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@SuppressLint("CustomSplashScreen")
class SplashScreenFragment : Fragment(R.layout.fragment_splash_screen) {

    private lateinit var progress: LottieAnimationView

    override fun onResume() {
        super.onResume()
        if(!PermissionsUtils.isOnline(requireContext())){
            Log.i(ContentValues.TAG, "onCreate: 2")
            android.app.AlertDialog.Builder(requireActivity()).setTitle("No Internet")
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
//            findNavController().navigate(
//                SplashScreenFragmentDirections
//                    .actionSplashScreenFragmentToLauncherScreenFragment()
//            )
            MainScope().launch {
                delay(3000)
                findNavController().navigate(
                    SplashScreenFragmentDirections
                        .actionSplashScreenFragmentToLauncherScreenFragment()
                )
//                requireActivity().finish()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progress = view.findViewById(R.id.lottieAnimation)

    }
}
package com.example.guniattendance.authorization.authfragments.ui.splashscreen

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.guniattendance.R
import com.example.guniattendance.authorization.AuthActivity
import com.example.guniattendance.utils.BasicUtils
import com.example.guniattendance.utils.LiveNetworkMonitor
import com.example.guniattendance.utils.PermissionsUtils
import com.example.guniattendance.utils.snackbar
import kotlinx.coroutines.*
import org.json.JSONArray
import java.util.*


@Suppress("DEPRECATION")
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
            /*if(!PermissionsUtils.isOnline(requireContext())){
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
                        try{
                            findNavController().navigate(SplashScreenFragmentDirections.actionSplashScreenFragmentSelf())
                            dialog.dismiss()
                        }
                        catch (ex:Exception)
                        {
                            snackbar(ex.message.toString())
                        }

                    }
                    .create().show()
            }
            else{
                try{
                    MainScope().launch {
                        val pm: PackageManager = (requireContext()).packageManager
                        val pkgName: String = (requireContext()).packageName
                        var pkgInfo: PackageInfo? = null
                        try {
                            pkgInfo = pm.getPackageInfo(pkgName, 0)
                        } catch (e: PackageManager.NameNotFoundException) {
                            e.printStackTrace()
                        }
                        val applicationVersion = pkgInfo!!.versionName
                        val versionFileURL = "https://script.google.com/macros/s/AKfycbzRZq71El7QfSmce5IGNY7yEHhoEpOYFpQeoYcDwPLE_RnzQIhYI68C8NAOejH6ayLOwA/exec"
                        val mRequestQueue = Volley.newRequestQueue(requireContext())
                        val request = object : StringRequest(
                            Method.GET, versionFileURL,
                            { response ->
                                val jsonArray = JSONArray(response)
                                if(jsonArray.getJSONObject(0).getString("StudentAppVersion")!=applicationVersion)
                                {
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
                                else{
                                    findNavController().navigate(
                                        SplashScreenFragmentDirections
                                            .actionSplashScreenFragmentToLauncherScreenFragment()
                                    )
                                }

                            },
                            { error ->
                                Log.e("AppUpdateService",error.toString())
                            }){}
                        request.retryPolicy = object : RetryPolicy {
                            override fun getCurrentTimeout(): Int {
                                return 7000
                            }

                            override fun getCurrentRetryCount(): Int {
                                return 500
                            }

                            @Throws(VolleyError::class)
                            override fun retry(error: VolleyError) {
                            }
                        }
                        mRequestQueue.add(request)
                    }
                }
                catch (ex:Exception)
                {
                    snackbar(ex.message.toString())
                }
            }*/
            try{
                val connectivityManager = requireContext().getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
                val liveNetworkMonitor = LiveNetworkMonitor(connectivityManager)
                liveNetworkMonitor.observe(this) {
                    if (it) {
                        MainScope().launch {
                            val pm: PackageManager = (requireContext()).packageManager
                            val pkgName: String = (requireContext()).packageName
                            var pkgInfo: PackageInfo? = null
                            try {
                                pkgInfo = pm.getPackageInfo(pkgName, 0)
                            } catch (e: PackageManager.NameNotFoundException) {
                                e.printStackTrace()
                            }
                            val applicationVersion = pkgInfo!!.versionName
                            val versionFileURL =
                                "https://script.google.com/macros/s/AKfycbzRZq71El7QfSmce5IGNY7yEHhoEpOYFpQeoYcDwPLE_RnzQIhYI68C8NAOejH6ayLOwA/exec"
                            val mRequestQueue = Volley.newRequestQueue(requireContext())
                            val request = object : StringRequest(
                                Method.GET, versionFileURL,
                                { response ->
                                    val jsonArray = JSONArray(response)
                                    if (jsonArray.getJSONObject(0)
                                            .getString("StudentAppVersion") != applicationVersion
                                    ) {
                                        AlertDialog.Builder(requireActivity())
                                            .setTitle("App Update")
                                            .setMessage("Your Application version is old please Update the APP")
                                            .setPositiveButton("UPDATE") { dialog, _ ->
                                                val openURL = Intent(Intent.ACTION_VIEW)
                                                openURL.data =
                                                    Uri.parse("https://drive.google.com/drive/folders/1jwyzjhzpUuPvUZzvJ4I-fn58sRVjBSwQ?usp=sharing")
                                                startActivity(openURL)
                                                dialog.dismiss()
                                            }
                                            .setCancelable(false)
                                            .create().show()
                                    } else {
                                        try{
                                            Intent(
                                                requireActivity(),
                                                AuthActivity::class.java
                                            ).also { intent ->
                                                startActivity(intent)
                                                requireActivity().finish()
                                            }
                                        }
                                        catch (ex:Exception)
                                        {
                                            Log.e(TAG,ex.message.toString())
                                        }

                                    }

                                },
                                { error ->
                                    Log.e(TAG, error.toString())
                                    BasicUtils.errorDialogBox(requireContext(),"Connection","Internet connection is poor!")
                                }) {}
                            request.retryPolicy = object : RetryPolicy {
                                override fun getCurrentTimeout(): Int {
                                    return 7000
                                }

                                override fun getCurrentRetryCount(): Int {
                                    return 500
                                }

                                @Throws(VolleyError::class)
                                override fun retry(error: VolleyError) {
                                }
                            }
                            mRequestQueue.add(request)
                        }
                    }
                }

            }
            catch (ex:Exception)
            {
                snackbar(ex.message.toString())
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progress = view.findViewById(R.id.lottieAnimation)
    }
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionsUtils.onRequestPermissionResult(requireContext(), requireActivity(), grantResults)
    }
}
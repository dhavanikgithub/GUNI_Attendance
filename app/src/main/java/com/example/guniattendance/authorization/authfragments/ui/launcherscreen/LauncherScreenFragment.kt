package com.example.guniattendance.authorization.authfragments.ui.launcherscreen

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ProgressBar
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.guniattendance.R
import com.example.guniattendance.authorization.DownloadModel
import com.example.guniattendance.databinding.FragmentLauncherScreenBinding
import com.example.guniattendance.moodle.MoodleConfig
import com.example.guniattendance.utils.CustomProgressDialog
import com.example.guniattendance.utils.hideKeyboard
import com.example.guniattendance.utils.showProgress
import com.example.guniattendance.utils.snackbar
import com.uvpce.attendance_moodle_api_library.repo.ModelRepository

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class LauncherScreenFragment : Fragment(R.layout.fragment_launcher_screen) {
    private lateinit var binding: FragmentLauncherScreenBinding
    lateinit var progressBar: ProgressBar
    private var progressDialog: CustomProgressDialog? = null

    companion object{
        lateinit var studentEnrolment: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentLauncherScreenBinding.inflate(layoutInflater)
        val checkboxTogglePref: SharedPreferences = requireActivity().getSharedPreferences("buttonToggle", 0)
        val checkboxCheck = checkboxTogglePref.getBoolean("buttonToggle", false)

        MainScope().launch {
            if(checkboxCheck){
                showProgress(requireActivity(), true, binding.parentLayout, binding.lottieAnimation)
                MoodleConfig.getModelRepo(requireContext())
                val url = ModelRepository.getStoredMoodleUrl(requireContext()).url
                showProgress(requireActivity(), false, binding.parentLayout, binding.lottieAnimation)
                android.app.AlertDialog.Builder(requireActivity()).setTitle("Current Moodle URL")
                    .setMessage(url)
                    .setPositiveButton("Continue"){ dialog, _ ->
                        dialog.dismiss()
                    }
                    .setNegativeButton("Change URL"){ dialog, _ ->
                        findNavController().navigate(
                            LauncherScreenFragmentDirections
                                .actionLauncherScreenFragmentToSettingFragment()
                        )
                        dialog.dismiss()
                    }
                    .create().show()
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        if(!checkPermission())
        {
            requestPermission()
        }
        super.onViewStateRestored(savedInstanceState)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLauncherScreenBinding.bind(view)
        if(progressDialog==null)
        {
            progressDialog = CustomProgressDialog(requireContext(), requireActivity())
        }

//        requireActivity().onBackPressedDispatcher.addCallback(activity){
//            requireActivity().finish()
//        }

        binding.apply {
            DownloadModel.getDownloadObject(requireActivity(),progressLayout,progressText,progressBar,parentLayout).startModelFile1Download()
            /*tnCheckEnrol.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER) {

                    btnCheckEnrol.performClick()
                }
                false
            })*/
            btnCheckEnrol.setOnClickListener{
                hideKeyboard(requireActivity())
//                progressLayout.visibility = View.VISIBLE

                if(et1Enrollment.text.toString().isEmpty())
                {
                   snackbar("Enrollment number is empty!")
                }
                else
                {
                    studentEnrolment = et1Enrollment.text.toString()
                    val pattern = Regex("^[0-9]{11}$")
                    if (pattern.containsMatchIn(studentEnrolment))
                    {
                        // Coroutins on backgroud thread
                        progressDialog!!.start("Verifying Enrollment....")
                        MainScope().launch{
                            try {
                                val result = MoodleConfig.getModelRepo(requireActivity()).isStudentRegisterForFace(requireContext(), studentEnrolment)
                                progressDialog!!.stop()
                                //progressLayout.visibility = View.GONE
                                if (result.hasUserUploadImg)
                                {
                                    findNavController().navigate(
                                        LauncherScreenFragmentDirections
                                            .actionLauncherScreenFragmentToStudentRegisterFragment()
                                    )
                                }
                                else
                                {
                                    findNavController().navigate(LauncherScreenFragmentDirections.actionLauncherScreenFragmentToStudentHomeFragment())
                                }
                            }
                            catch (ex: Exception)
                            {
                                snackbar("Invalid Enrollment Number " + ex.message)
                            }
                            finally
                            {
                                progressDialog!!.stop()
                            }
                        }
                    }
                    else
                    {
                        snackbar("Invalid Enrollment Number")
                    }
                }
            }
            settingsBtn.setOnClickListener{
                findNavController().navigate(
                    LauncherScreenFragmentDirections.actionLauncherScreenFragmentToSettingFragment()
                )
            }
        }
    }

    override fun onDestroy()
    {
        DownloadModel.destroyObject()
        super.onDestroy()
    }

    override fun onDestroyView()
    {
        DownloadModel.destroyObject()
        super.onDestroyView()
    }


    fun checkPermission(): Boolean
    {
        // ContextCompat.checkSelfPermission() - is used to check the dangerous permission.
        val cameraPermission =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        val readStoragePermission =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
        val accessFinePermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        val accessCoarsePermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
        val internetPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.INTERNET)

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

    private fun requestPermission()
    {
        val PERMISSION_CODE = 200

        requestPermissions(arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET), PERMISSION_CODE)
    }

    private fun checkUserRequestedDontAskAgain():Boolean
    {
        val rationalFlagREAD =
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
        val rationalFlagCAMERA =
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
        val rationalFlagINTERNET =
            shouldShowRequestPermissionRationale(Manifest.permission.INTERNET)
        val rationalFlagLOCATIONCOARSE =
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
        val rationalFlagLOCATIONFINE =
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
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

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty())
        {
            val cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED
            val readStoragePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED
            val accessFinePermission = grantResults[2] == PackageManager.PERMISSION_GRANTED
            val accessCoarsePermission = grantResults[3] == PackageManager.PERMISSION_GRANTED
            val internetPermission = grantResults[4] == PackageManager.PERMISSION_GRANTED
            if (!cameraPermission || !readStoragePermission || !accessFinePermission || !accessCoarsePermission || !internetPermission)
            {
                if(!checkUserRequestedDontAskAgain())
                {
                    val alertDialogBuilder = AlertDialog.Builder(requireContext())
                    alertDialogBuilder
                        .setMessage("Click Settings to manually set permissions.")
                        .setCancelable(false)
                        .setPositiveButton("SETTINGS")
                        { dialog, id ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts("package",requireContext().packageName,null)
                            intent.data = uri
                            startActivity(intent)
                        }

                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                }
                else
                {
                    requestPermission()
                }
            }
        }
    }

}
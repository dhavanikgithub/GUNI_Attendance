package com.example.guniattendance.authorization.authfragments.ui.launcherscreen

import android.Manifest.permission
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.guniattendance.R
import com.example.guniattendance.SettingsActivity
import com.example.guniattendance.authorization.DownloadModel
import com.example.guniattendance.databinding.FragmentLauncherScreenBinding
import com.example.guniattendance.moodle.MoodleConfig
import com.example.guniattendance.utils.snackbar
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class LauncherScreenFragment : Fragment(R.layout.fragment_launcher_screen) {
    private lateinit var binding: FragmentLauncherScreenBinding
    lateinit var progressBar: ProgressBar
    companion object{
        lateinit var studentEnrolment: String
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLauncherScreenBinding.bind(view)
        binding.apply {
            DownloadModel.getDownloadObject(requireActivity(),progressLayout,progressText,progressBar,parentLayout).startModelFile1Download()
            btnCheckEnrol.setOnClickListener{
                if(checkPermission()){
                    if(et1Enrollment.text.toString().isEmpty()){
                        context?.let { snackbar("Enrollment number is empty!") }
                    }else {
                        studentEnrolment = et1Enrollment.text.toString()
                        // Coroutins on backgroud thread
                        MainScope().launch {
                            try{
                                progressLayout.visibility = View.VISIBLE
//                                progressBar.visibility  = View.VISIBLE
                                var result =
                                    context?.let { it1 ->
                                        MoodleConfig.getModelRepo(requireActivity()).isStudentRegisterForFace(
                                            it1,studentEnrolment)
                                    }
                                if ((result!!.hasUserUploadImg) && (progressLayout.visibility == View.VISIBLE)) {
                                    progressLayout.visibility = View.GONE
//                                    progressBar.visibility = View.GONE
                                    findNavController().navigate(LauncherScreenFragmentDirections
                                        .actionLauncherScreenFragmentToStudentRegisterFragment())
                                }
                                else {
                                    progressLayout.visibility = View.GONE
//                                    progressBar.visibility = View.GONE
                                    findNavController().navigate(LauncherScreenFragmentDirections.actionLauncherScreenFragmentToStudentHomeFragment())
                                }

                            } catch (ex:Exception){
                                snackbar("Invalid Enrollment Number "+ex.message)
                            }
                        }
                    }
                } else{
                    requestPermission()
                }
            }
            settingsBtn.setOnClickListener{
                Intent(context,SettingsActivity::class.java).also{
                    startActivity(it)
                }
            }

        }

    }

    private fun checkPermission(): Boolean{
        // ContextCompat.checkSelfPermission() - is used to check the dangerous permission.
        val cameraPermission =
            ContextCompat.checkSelfPermission(requireContext(), permission.CAMERA)
        // if permission is already granted, then its 0, if not then its -1. So if cameraPermission is 0 then it's true, other wise its false.
        return cameraPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(){
        val PERMISSION_CODE = 200
//        ActivityCompat.requestPermissions(this, arrayOf(permission.CAMERA), PERMISSION_CODE)
//        ContextCompat.requestPermission(arrayOf(permission.CAMERA), PERMISSION_CODE)
        requestPermissions(arrayOf(permission.CAMERA, permission.READ_EXTERNAL_STORAGE, permission.WRITE_EXTERNAL_STORAGE), PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size > 0) {
            val cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (cameraPermission) {
                Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        DownloadModel.destroyObject()
        super.onDestroy()
    }

    override fun onDestroyView() {
        DownloadModel.destroyObject()
        super.onDestroyView()
    }
}
package com.example.guniattendance.authorization.authfragments.ui.launcherscreen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.guniattendance.R
import com.example.guniattendance.SettingsActivity
import com.example.guniattendance.authorization.DownloadModel
import com.example.guniattendance.databinding.FragmentLauncherScreenBinding
import com.example.guniattendance.moodle.MoodleConfig
import com.example.guniattendance.utils.snackbar
import com.uvpce.attendance_moodle_api_library.repo.ClientAPI

class LauncherScreenFragment : Fragment(R.layout.fragment_launcher_screen) {
    private lateinit var binding: FragmentLauncherScreenBinding
    companion object{
        lateinit var studentEnrolment: String
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLauncherScreenBinding.bind(view)
        binding.apply {
            DownloadModel.getDownloadObject(requireActivity(),progressLayout,progressText,progressBar,parentLayout).startModelFile1Download()
            btnCheckEnrol.setOnClickListener{
                if(et1Enrollment.text.toString().isEmpty()){
                    context?.let { snackbar("Enrollment number is empty!") }
                }else {
                    studentEnrolment = et1Enrollment.text.toString()
                    MoodleConfig.getModelRepo(requireActivity()).isStudentRegisterForFace(studentEnrolment,
                        onReceive = { it ->
                            if (it) {
                                findNavController().navigate(LauncherScreenFragmentDirections
                                        .actionLauncherScreenFragmentToLoginFragment())
                            }
                            else {
                                findNavController().navigate(LauncherScreenFragmentDirections.actionLauncherScreenFragmentToScannerFragment())
//                                Intent(context, ScannerActivity::class.java).also { startActivity(it) }
                            }
                        }, onError = {
                            Log.i("TAG", "onError: $it")
                            snackbar("Invalid Enrollment Number!")
                        })
                }
            }
            settingsBtn.setOnClickListener{
                Intent(context,SettingsActivity::class.java).also{
                    startActivity(it)
                }
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
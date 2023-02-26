package com.example.guniattendance.authorization.authfragments.ui.launcherscreen

import android.content.ContentValues.TAG
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.guniattendance.R
import com.example.guniattendance.authorization.DownloadModel
import com.example.guniattendance.databinding.FragmentLauncherScreenBinding
import com.example.guniattendance.moodle.MoodleConfig
import com.example.guniattendance.utils.*
import com.guni.uvpce.moodleapplibrary.model.MoodleBasicUrl
import com.guni.uvpce.moodleapplibrary.repo.ModelRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.math.log

class LauncherScreenFragment : Fragment(R.layout.fragment_launcher_screen) {
    private lateinit var binding: FragmentLauncherScreenBinding
    private var progressDialog: CustomProgressDialog? = null

    companion object{
        lateinit var studentEnrolment: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = FragmentLauncherScreenBinding.inflate(layoutInflater)
        if(progressDialog==null)
        {
            progressDialog= CustomProgressDialog(requireContext(),requireActivity())
        }

        val checkboxTogglePref: SharedPreferences = requireActivity().getSharedPreferences("buttonToggle", 0)
        val checkboxCheck = checkboxTogglePref.getBoolean("buttonToggle", false)
        MainScope().launch {
            if(checkboxCheck){
                progressDialog!!.start("Please connection is establishing....")
                MoodleConfig.getModelRepo(requireContext())
                val url = ModelRepository.getMoodleUrlObject(requireContext())
                progressDialog!!.stop()
                android.app.AlertDialog.Builder(requireActivity()).setTitle("Current Moodle URL")
                        .setMessage(url.url)
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
        if(!PermissionsUtils.checkPermission(requireContext()))
        {
            PermissionsUtils.requestPermission(requireActivity())
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

        binding.apply {
            DownloadModel.getDownloadObject(requireActivity(),progressLayout,progressText,progressBar,parentLayout).startModelFile1Download()
            btnCheckEnrol.setOnClickListener{
                hideKeyboard(requireActivity())

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
                        progressDialog!!.start("Verifying Enrollment....")
                        MainScope().launch{
                            try {
                                Log.i(TAG, "onViewCreated: $studentEnrolment")
                                val result = MoodleConfig.getModelRepo(requireContext()).isStudentRegisterForFace(requireContext(), studentEnrolment)
                                progressDialog!!.stop()
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
                                progressDialog!!.stop()
                                snackbar("Invalid Enrollment Number " + ex.message)
                                Log.e(TAG, "onViewCreated: Invalid Enrollment Number:$ex", ex)
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
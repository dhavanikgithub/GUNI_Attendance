package com.example.guniattendance.authorization.authfragments.ui.launcherscreen

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.guniattendance.R
import com.example.guniattendance.databinding.FragmentLauncherScreenBinding
import com.example.guniattendance.moodle.MoodleConfig
import com.example.guniattendance.student.StudentActivity
import com.example.guniattendance.utils.CustomProgressDialog
import com.example.guniattendance.utils.DownloadUtils
import com.example.guniattendance.utils.hideKeyboard
import com.example.guniattendance.utils.snackbar
import com.guni.uvpce.moodleapplibrary.model.BaseUserInfo
import com.guni.uvpce.moodleapplibrary.repo.ModelRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LauncherScreenFragment : Fragment(R.layout.fragment_launcher_screen) {
    private lateinit var binding: FragmentLauncherScreenBinding
    private var progressDialog: CustomProgressDialog? = null
    private val TAG = "LauncherScreenFragment"
    companion object{
        lateinit var studentEnrolment: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = FragmentLauncherScreenBinding.inflate(layoutInflater)
        if(progressDialog==null)
        {
            progressDialog= CustomProgressDialog(requireContext())
        }

        val checkboxTogglePref: SharedPreferences = requireActivity().getSharedPreferences("buttonToggle", 0)
        val checkboxCheck = checkboxTogglePref.getBoolean("buttonToggle", false)
        MainScope().launch {
            if(checkboxCheck){
                progressDialog!!.start("Please wait connection is establishing....")
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLauncherScreenBinding.bind(view)

        /*val callback = object :OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(callback)*/

        binding.apply {
            /*DownloadModel.getDownloadObject(requireActivity(),binding.progressLayout,binding.progressText,binding.progressBar,binding.parentLayout).startModelFile1Download()*/

            val downloadUtils = DownloadUtils(
                downloadingContentText,
                parentLayout,
                progressLayout,
                progressBar,
                progressBarText,
                requireActivity(),
                requireParentFragment(),
                downloadingContentStatisticText,
                requireContext(),
                btnDownloadPause
            )
            downloadUtils.start()

            btnCheckEnrol.setOnClickListener{
                hideKeyboard(requireActivity())
                if(et1Enrollment.text.toString().isEmpty())
                {
                   snackbar("Enrollment number is empty!")
                }
                else
                {
                    studentEnrolment = et1Enrollment.text.toString()
                    val pattern = Regex("^\\d{11}$")
                    if (pattern.containsMatchIn(studentEnrolment))
                    {
                        MainScope().launch {
                            try {
                                if (checkUser()!!.hasUserUploadImg)
                                {
                                    et1Enrollment.setText("")
                                    findNavController().navigate(R.id.studentRegisterFragment)
                                }
                                else
                                {
                                    et1Enrollment.setText("")
                                    Intent(
                                        requireActivity(),
                                        StudentActivity::class.java
                                    ).also { intent ->
                                        startActivity(intent)
                                        requireActivity().finish()
                                    }
                                }
                            }
                            catch (ex: Exception)
                            {
//                                snackbar("Invalid Enrollment Number " + ex.message)
                                snackbar("Check Internet or Enrollment")
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
    /*override fun onDestroy()
    {
        DownloadModel.destroyObject()
        super.onDestroy()
    }

    override fun onDestroyView()
    {
        DownloadModel.destroyObject()
        super.onDestroyView()
    }*/

    private suspend fun checkUser(): BaseUserInfo?
    {
        var result: BaseUserInfo? =null
        progressDialog!!.start("Verifying Enrollment....")
        val parentJob= GlobalScope.launch {
            try {
                result = MoodleConfig.getModelRepo(requireContext()).isStudentRegisterForFace(studentEnrolment)
            }
            catch (ex:Exception)
            {
                return@launch
            }
            return@launch
        }
        delay(10000)
        parentJob.cancel()
        parentJob.join()
        progressDialog!!.stop()
        return result
    }
}
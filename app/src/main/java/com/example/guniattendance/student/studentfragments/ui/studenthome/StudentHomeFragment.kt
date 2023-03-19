package com.example.guniattendance.student.studentfragments.ui.studenthome


import android.content.IntentSender
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.guniattendance.R
import com.example.guniattendance.authorization.authfragments.ui.launcherscreen.LauncherScreenFragment
import com.example.guniattendance.databinding.FragmentStudentHomeBinding
import com.example.guniattendance.moodle.MoodleConfig
import com.example.guniattendance.utils.CustomProgressDialog
import com.example.guniattendance.utils.ImageUtils
import com.example.guniattendance.utils.snackbar
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task
import com.guni.uvpce.moodleapplibrary.model.BaseUserInfo
import com.guni.uvpce.moodleapplibrary.model.QRMessageData
import com.guni.uvpce.moodleapplibrary.repo.ModelRepository
import com.guni.uvpce.moodleapplibrary.util.Utility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


@AndroidEntryPoint
class StudentHomeFragment : Fragment(R.layout.fragment_student_home) {

    private lateinit var binding: FragmentStudentHomeBinding
    private lateinit var viewModel: StudentHomeViewModel
    private lateinit var userInfo: BaseUserInfo
    lateinit var imgURL:String
    private var progressDialog: CustomProgressDialog? = null
    val TAG = "StudentHomeFragment"
    var profileImage: Bitmap? = null
    private var fullMessage: QRMessageData? =null

    private lateinit var repo: ModelRepository


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[StudentHomeViewModel::class.java]

        binding = FragmentStudentHomeBinding.bind(view)

        if(progressDialog==null)
        {
            progressDialog = CustomProgressDialog(requireContext(),requireActivity())
        }
        binding.apply {
//            layoutTakeAttendance.setOnClickListener {
//                viewModel.getActiveAttendance()
//            }

            MainScope().launch {
                progressDialog!!.start("Details Fetching...")
                repo = MoodleConfig.getModelRepo(requireContext())
                userInfo = repo.getUserInfo(LauncherScreenFragment.studentEnrolment)
                imgURL = userInfo.imageUrl
                profileImage = MoodleConfig.getModelRepo(requireContext()).getURLtoBitmap(imgURL)
                ivImage.setImageBitmap(profileImage)
                tvName.text = userInfo.lastname
                tvEnrollNo.text = LauncherScreenFragment.studentEnrolment
                tvEmailId.text = userInfo.emailAddress
                progressDialog!!.stop()
            }

            btnSetting.setOnClickListener{
                findNavController().navigate(R.id.settingFragment)
            }

            btnTakeAttendance.setOnClickListener {

                val locationRequest: LocationRequest = LocationRequest.create()
                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                locationRequest.interval = 10000
                locationRequest.fastestInterval = 10000 / 2
                val locationSettingsRequestBuilder = LocationSettingsRequest.Builder()
                locationSettingsRequestBuilder.addLocationRequest(locationRequest)
                locationSettingsRequestBuilder.setAlwaysShow(true)
                val settingsClient = LocationServices.getSettingsClient(requireContext())
                val task: Task<LocationSettingsResponse> =
                    settingsClient.checkLocationSettings(locationSettingsRequestBuilder.build())
                task.addOnSuccessListener {
                    progressDialog!!.start("Preparing for attendance...")
                    MainScope().launch {
                        try{
                            Log.i(TAG, "userInfo: ${userInfo.id}")
                            val messageData = MoodleConfig.getModelRepo(requireContext()).getMessage(userInfo.id)
                            progressDialog!!.stop()
                            fullMessage=QRMessageData.getQRMessageObject(messageData.fullMessage)
                            if(verifySession(fullMessage!!))
                            {
                                val bundle = Bundle()
                                Log.i(TAG, "messageData: $messageData")
                                bundle.putString("attendanceData", fullMessage.toString())
                                bundle.putString("userInfo",(userInfo.toJsonObject()).toString())
                                bundle.putString("profileImage",ImageUtils.convertBitmaptoString(profileImage!!))
                                findNavController().navigate(R.id.attendanceInfoFragment,bundle)
                            }
                            else
                            {
                                snackbar("Attendance not longer responsible!")
                            }

                        }
                        catch(ex:Exception)
                        {
                            progressDialog!!.stop()
                            snackbar("Attendance not longer responsible!")
                            Log.e(TAG,"getMessage Error: $ex")
                        }
                    }
                }
                task.addOnFailureListener(requireActivity()) { e ->
                    if (e is ResolvableApiException) {
                        try {
                            e.startResolutionForResult(
                                requireActivity(),
                                0x1
                            )
                        } catch (sendIntentException: IntentSender.SendIntentException) {
                            sendIntentException.printStackTrace()
                        }
                    }
                }
            }
        }
    }
    private fun verifySession(data:QRMessageData):Boolean{
        val current = Utility().getCurrenMillis()/1000
        if(data.sessionStartDate <= current  && data.sessionEndDate >= current){
            if(data.attendanceStartDate <= current && data.attendanceEndDate >= current){
                return true
            }
        }
        return false
    }
}
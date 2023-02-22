package com.example.guniattendance.student.studentfragments.ui.studenthome


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
import com.guni.uvpce.moodleapplibrary.model.BaseUserInfo
import com.guni.uvpce.moodleapplibrary.model.QRMessageData
import com.guni.uvpce.moodleapplibrary.repo.ModelRepository
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
                findNavController().navigate(StudentHomeFragmentDirections.actionStudentHomeFragmentToSettingFragment())
            }

            btnTakeAttendance.setOnClickListener {
                progressDialog!!.start("Preparing for attendance...")
                MainScope().launch {
                    try{
                        Log.i(TAG, "userInfo: ${userInfo.id}")
                        val messageData = MoodleConfig.getModelRepo(requireContext()).getMessage(userInfo.id)
                        progressDialog!!.stop()
                        val bundle = Bundle()
                        Log.i(TAG, "messageData: $messageData")
                        bundle.putString("attendanceData", QRMessageData.getQRMessageObject(messageData.fullMessage).toString())
                        bundle.putString("userInfo",(userInfo.toJsonObject()).toString())
                        bundle.putString("profileImage",ImageUtils.convertBitmaptoString(profileImage!!))
                        findNavController().navigate(R.id.attendanceInfoFragment,bundle)
                    }
                    catch(ex:Exception)
                    {
                        progressDialog!!.stop()
                        snackbar("Attendance not longer responsible!")
                        Log.e(TAG,"getMessage Error: $ex")
                    }
                }

            }
        }
    }


}
package com.example.guniattendance.student.studentfragments.ui.attendance

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.guniattendance.R
import com.example.guniattendance.databinding.FragmentAttendanceInfoBinding
import com.example.guniattendance.utils.AccessMapLocation
import com.example.guniattendance.utils.CustomProgressDialog
import com.example.guniattendance.utils.snackbar
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task
import com.guni.uvpce.moodleapplibrary.model.QRMessageData
import com.guni.uvpce.moodleapplibrary.util.Utility
import org.json.JSONObject
import java.text.SimpleDateFormat


class AttendanceInfoFragment : Fragment(R.layout.fragment_attendance_info) {
    private lateinit var binding: FragmentAttendanceInfoBinding
//    private lateinit var qRMsgData: String
    private lateinit var viewModel: AttendanceInfoViewModel
//    lateinit var QRBtn: AppCompatButton
    private var attendanceData:String?=null
    private var qrData: QRMessageData? = null
    private var profileImage: String? = null
    private lateinit var userInfo: JSONObject
    private var progressDialog: CustomProgressDialog? = null
    private val TAG = "AttendanceInfoFragment"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val  txt = arguments?.getString("qrData")
        attendanceData = requireArguments().getString("attendanceData")
        userInfo = JSONObject(requireArguments().getString("userInfo")!!)
        profileImage=requireArguments().getString("profileImage")

        attendanceData.let {
            if (it != "") {
                qrData = QRMessageData.fromJsonObject(it!!)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[AttendanceInfoViewModel::class.java]

        binding = FragmentAttendanceInfoBinding.bind(view)

        /*val callback = object : OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed() {
                findNavController().navigate(AttendanceInfoFragmentDirections.actionAttendanceInfoFragmentToStudentHomeFragment())
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(callback)*/

        if(progressDialog==null)
        {
            progressDialog= CustomProgressDialog(requireContext())
        }
        binding.apply {
//            QRBtn.setOnClickListener{
//                it.findNavController().navigate(R.id.action_attendanceInfoFragment_to_scannerFragment)
//            }
            attendanceBtn.setOnClickListener {
                try{
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
                        progressDialog!!.start("Checking Range....")
                        try{
                            val applicableLocation = AccessMapLocation(requireActivity()).markAttendance(qrData!!.facultyLocationLat.toDouble(),qrData!!.facultyLocationLong.toDouble(),qrData!!.locationRange)
                            progressDialog!!.stop()
                            if(applicableLocation)
                            {
                                if(verifySession(qrData!!))
                                {
                                    val bundle = Bundle()
                                    bundle.putString("profileImage",profileImage)
                                    bundle.putString("userInfo",userInfo.toString())
                                    bundle.putString("attendanceData",attendanceData)
                                    findNavController().navigate(R.id.takeAttendanceFragment, bundle)
                                }
                                else
                                {
                                    val alertDialog = AlertDialog.Builder( requireContext() ).apply {
                                        setTitle( "Session")
                                        setMessage( "Sorry, your attendance response time is over!" )
                                        setCancelable( false )
                                        setPositiveButton( "OK" ) { dialog, _ ->
                                            dialog.dismiss()
                                        }
                                        create()
                                    }
                                    alertDialog.show()
                                }

                            }
                            else{
                                val alertDialog = AlertDialog.Builder( requireContext() ).apply {
                                    setTitle( "Location")
                                    setMessage( "Sorry, you can't mark your attendance because you're outside." )
                                    setCancelable( false )
                                    setPositiveButton( "Retry" ) { dialog, _ ->
                                        dialog.dismiss()
                                        attendanceBtn.performClick()
                                    }
                                    setNegativeButton( "Close" ) { dialog, _ ->
                                        dialog.dismiss()
                                    }
                                    create()
                                }
                                alertDialog.show()
                            }
                        }
                        catch (ex:Exception)
                        {
                            progressDialog!!.stop()
                            snackbar("${ex.message}")
                            Log.e(TAG,ex.message.toString())
                        }
                        progressDialog!!.stop()

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
                catch (e:Exception)
                {
                    progressDialog!!.stop()
                    snackbar("Error in fetch Location")
                    Log.e(ContentValues.TAG,"Location Error: $e")
                }
            }
            txtQRInfo.text = getQRText(qrData!!)
        }
    }
    @SuppressLint("SimpleDateFormat")
    fun getQRText(data: QRMessageData):String{
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        val simpleTimeFormat = SimpleDateFormat("hh:mm a")
        return "Course: ${data.courseName} (" +
                "Group: ${data.groupName})\n\n" +
                "Session Date:${simpleDateFormat.format(data.sessionStartDate*1000)}\n" +
                " From:${simpleTimeFormat.format(data.sessionStartDate*1000)}" +
                " To:${simpleTimeFormat.format(data.sessionEndDate*1000)}\n\n"+
                "Attendance Date:${simpleDateFormat.format(data.attendanceStartDate*1000)}\n" +
                " From:${simpleTimeFormat.format(data.attendanceStartDate*1000)}"+
                " To:${simpleTimeFormat.format(data.attendanceEndDate*1000)}\n" +
                "Duration: ${data.attendanceDuration}mins"
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
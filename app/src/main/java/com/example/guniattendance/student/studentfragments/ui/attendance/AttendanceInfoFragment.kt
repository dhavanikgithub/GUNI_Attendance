package com.example.guniattendance.student.studentfragments.ui.attendance

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.guniattendance.R
import com.example.guniattendance.databinding.FragmentAttendanceInfoBinding
import com.example.guniattendance.utils.AccessMapLocation
import com.example.guniattendance.utils.CustomProgressDialog
import com.example.guniattendance.utils.snackbar
import com.guni.uvpce.moodleapplibrary.model.QRMessageData
import com.guni.uvpce.moodleapplibrary.util.Utility
import org.json.JSONObject
import java.text.SimpleDateFormat


class AttendanceInfoFragment : Fragment(R.layout.fragment_attendance_info) {
    private lateinit var binding: FragmentAttendanceInfoBinding
    private lateinit var qRMsgData: String
    private lateinit var viewModel: AttendanceInfoViewModel
    lateinit var QRBtn: AppCompatButton
    var attendanceData:String?=null
    private var qrData: QRMessageData? = null
    var profileImage: String? = null
    private lateinit var userInfo: JSONObject
    private var progressDialog: CustomProgressDialog? = null
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
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[AttendanceInfoViewModel::class.java]

        binding = FragmentAttendanceInfoBinding.bind(view)

        if(progressDialog==null)
        {
            progressDialog= CustomProgressDialog(requireContext(),requireActivity())
        }
        binding.apply {
            QRBtn.setOnClickListener{
                it.findNavController().navigate(R.id.action_attendanceInfoFragment_to_scannerFragment)
            }
            attendanceBtn.setOnClickListener {
                try{
                    progressDialog!!.start("Checking Range....")
                    Log.i("TAG","Dk3")
                    val applicableLocation = AccessMapLocation(requireActivity()).markAttendance(qrData!!.facultyLocationLat.toDouble(),qrData!!.facultyLocationLong.toDouble())
                    Log.i("TAG","Dk4")
                    progressDialog!!.stop()
                    if(applicableLocation)
                    {
                        Log.i("TAG","Dk1")
                        if(verifySession(qrData!!))
                        {
                            Log.i("TAG","Dk2")
                            val bundle = Bundle()
                            bundle.putString("profileImage",profileImage)
                            bundle.putString("userInfo",userInfo.toString())
                            bundle.putString("attendanceData",attendanceData)
                            it.findNavController().navigate(R.id.action_attendanceInfoFragment_to_takeAttendanceFragment, bundle)
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
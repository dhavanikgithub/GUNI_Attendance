package com.example.guniattendance.student.studentfragments.ui.attendance

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.guniattendance.R
import com.example.guniattendance.authorization.authfragments.ui.apppin.AppPinFragmentArgs
import com.example.guniattendance.databinding.FragmentAttendanceInfoBinding
import com.example.guniattendance.student.studentfragments.ui.scanner.ScannerFragment
import com.example.guniattendance.student.studentfragments.ui.scanner.ScannerFragmentDirections
import com.example.guniattendance.utils.snackbar
import com.uvpce.attendance_moodle_api_library.model.QRMessageData
import org.json.JSONObject
import java.text.SimpleDateFormat


class AttendanceInfoFragment : Fragment(R.layout.fragment_attendance_info) {
    private val args: AppPinFragmentArgs by navArgs()
    private lateinit var binding: FragmentAttendanceInfoBinding
    private lateinit var qRMsgData: String
    private lateinit var viewModel: AttendanceInfoViewModel
    lateinit var QRBtn: AppCompatButton

    private var qrData:QRMessageData? = null
    var userId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val  txt = arguments?.getString("qrData")
        var  txt = requireArguments().getString("msgData")
        userId = arguments?.getString("userId")!!
        txt.let {
            if (it != "") {
                qrData = QRMessageData.fromJsonObject(it!!)
                Toast.makeText(requireContext(), it,Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[AttendanceInfoViewModel::class.java]

        binding = FragmentAttendanceInfoBinding.bind(view)

        binding.apply {
            QRBtn.setOnClickListener{
                it.findNavController().navigate(R.id.action_attendanceInfoFragment_to_scannerFragment)
            }
            attendanceBtn.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("userId",userId)
                it.findNavController().navigate(R.id.action_attendanceInfoFragment_to_takeAttendanceFragment, bundle)
            }
            txtQRInfo.text = getQRText(qrData!!)
        }
    }
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

}
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


class AttendanceInfoFragment : Fragment(R.layout.fragment_attendance_info) {
    private val args: AppPinFragmentArgs by navArgs()
    private lateinit var binding: FragmentAttendanceInfoBinding
    private lateinit var qRMsgData: String
    private lateinit var viewModel: AttendanceInfoViewModel
    lateinit var QRBtn: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val  txt = arguments?.getString("qrData")
        txt.let {
            if (it != null) {
                snackbar(it)
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
                it.findNavController().navigate(AttendanceInfoFragmentDirections.actionAttendanceInfoFragmentToTakeAttendanceFragment())
            }
        }
    }


}
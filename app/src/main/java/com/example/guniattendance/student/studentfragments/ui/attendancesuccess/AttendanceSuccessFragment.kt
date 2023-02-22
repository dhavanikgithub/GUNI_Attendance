package com.example.guniattendance.student.studentfragments.ui.attendancesuccess

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.guniattendance.R
import com.example.guniattendance.databinding.FragmentAttendanceSuccessBinding

class AttendanceSuccessFragment : Fragment(R.layout.fragment_attendance_success) {

    companion object {
        fun newInstance() = AttendanceSuccessFragment()
    }

    private lateinit var viewModel: AttendanceSuccessViewModel
    private lateinit var binding: FragmentAttendanceSuccessBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_attendance_success, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[AttendanceSuccessViewModel::class.java]

        binding = FragmentAttendanceSuccessBinding.bind(view)

        binding.tvSucess.text="Your Attendance Marked Successfully"
    }

}